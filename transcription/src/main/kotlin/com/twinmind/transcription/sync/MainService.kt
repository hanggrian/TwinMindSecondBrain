package com.twinmind.transcription.sync

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.StatFs
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import com.twinmind.transcription.TwinMindApp
import com.twinmind.transcription.db.Chunks
import com.twinmind.transcription.db.schema.Chunk
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("InlinedApi")
class MainService : LifecycleService() {
    @Inject lateinit var chunks: Chunks

    @Inject lateinit var notificationsRepository: NotificationRepository

    @Inject lateinit var sessionRepository: SessionRepository

    @Inject lateinit var audioManager: AudioManager

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private lateinit var request: AudioFocusRequest

    private var recorder: MediaRecorder? = null
    private var chunkJob: Job? = null // for entity
    private var totalJob: Job? = null // for timer
    private var chunkOrder: Int = 0
    private var chunkPath: String? = null

    @Suppress("deprecation")
    private fun createRecorder(): MediaRecorder {
        val recorder =
            MediaRecorder(this)
                .takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
                ?: MediaRecorder()
        return recorder.apply {
            setAudioSource(MEDIA_SOURCE)
            setOutputFormat(MEDIA_FORMAT)
            setAudioEncoder(MEDIA_ENCODER)
        }
    }

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            sessionRepository.flow.collect {
                notificationsRepository
                    .update(notificationsRepository.create(it.state, it.pauseReason))
            }
        }

        request =
            AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                ).build()

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notificationsRepository.create(State.IDLE),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                .takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R }
                ?: 0,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_PAUSE -> pauseRecording(PauseReason.NONE)
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecording(GracefulTermination.DEFAULT)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun startRecording() {
        if (!isStorageSufficient()) {
            stopRecording(GracefulTermination.LOW_STORAGE)
            return
        }
        updateChunkPath()

        audioManager.requestAudioFocus(request)

        recorder = createRecorder()

        recorder?.run {
            setOutputFile(chunkPath)
            try {
                prepare()
                start()
                chunkOrder = 1
            } catch (e: Exception) {
                Log.e(TwinMindApp.TAG, "Error starting first recorder.", e)
                stopRecording(GracefulTermination.UNKNOWN)
                return
            }
        }

        scope.launch {
            notificationsRepository
                .update(notificationsRepository.create(State.RECORDING, PauseReason.NONE))
            sessionRepository.update(
                Session(
                    isRecording = true,
                    state = State.RECORDING,
                    elapsedTime = 0L,
                    pauseReason = PauseReason.NONE,
                ),
            )
            startTotalTimer()
        }
        startChunkTimer()
    }

    private fun pauseRecording(reason: PauseReason) {
        scope.launch {
            val session = sessionRepository.flow.first()
            if (session.state != State.RECORDING) {
                return@launch
            }
            sessionRepository.update(session.copy(state = State.PAUSED, pauseReason = reason))
            chunkJob?.cancel()
            totalJob?.cancel()

            recorder?.pause()
        }
    }

    private fun resumeRecording() {
        scope.launch {
            val session = sessionRepository.flow.first()
            if (session.state != State.PAUSED) {
                return@launch
            }
            sessionRepository
                .update(session.copy(state = State.RECORDING, pauseReason = PauseReason.NONE))

            recorder?.resume()
            startChunkTimer()
            startTotalTimer()
        }
    }

    private fun stopRecording(termination: GracefulTermination) {
        chunkJob?.cancel()
        totalJob?.cancel()

        scope.launch(Dispatchers.IO) {
            val currentChunkPath = chunkPath
            if (currentChunkPath != null && recorder != null) {
                try {
                    recorder?.stop()
                    recorder?.release()

                    chunks.insert(
                        Chunk(
                            filePath = currentChunkPath,
                            order = chunkOrder,
                        ),
                    )
                    Log.i(TwinMindApp.TAG, "Final Chunk #$chunkOrder inserted.")
                } catch (e: Exception) {
                    Log.e(TwinMindApp.TAG, "Error stopping recorder.", e)
                } finally {
                    recorder = null
                }
            }

            chunkPath = null
            audioManager.abandonAudioFocusRequest(request)

            sessionRepository.update(
                sessionRepository.flow.first().copy(
                    isRecording = false,
                    state = State.STOPPED,
                    gracefulTermination = termination,
                ),
            )

            withContext(Dispatchers.Main) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun startChunkTimer() {
        chunkJob?.cancel()
        chunkJob =
            scope.launch(Dispatchers.IO) {
                while (true) {
                    delay(CHUNK_STEPS)

                    try {
                        recorder?.stop()
                        recorder?.release()

                        val pathForInsert = chunkPath
                        if (pathForInsert != null) {
                            chunks.insert(
                                Chunk(
                                    filePath = pathForInsert,
                                    order = chunkOrder,
                                ),
                            )
                            Log.i(TwinMindApp.TAG, "Chunk #$chunkOrder inserted.")
                        } else {
                            Log.e(TwinMindApp.TAG, "chunkPath was null. Stopping chunking job.")
                            break
                        }

                        updateChunkPath()
                        chunkOrder++

                        recorder = createRecorder()
                        recorder?.run {
                            setOutputFile(chunkPath)
                            prepare()
                            start()
                            Log.d(
                                TwinMindApp.TAG,
                                "Recorder started for next Chunk #$chunkOrder to $chunkPath.",
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TwinMindApp.TAG, "FATAL", e)
                        stopRecording(GracefulTermination.UNKNOWN)
                        break
                    }
                }
            }
    }

    private fun startTotalTimer() {
        totalJob?.cancel()
        totalJob =
            scope.launch(Dispatchers.IO) {
                var current = sessionRepository.flow.first().elapsedTime
                while (true) {
                    current += DEFAULT_STEPS
                    delay(DEFAULT_STEPS)
                    sessionRepository.updateElapsedTime(current)
                }
            }
    }

    private fun isStorageSufficient(): Boolean {
        try {
            val stat = StatFs(filesDir.path)
            val blockSize = stat.blockSizeLong
            val totalSpace = stat.blockCountLong * blockSize
            val usedSpace = totalSpace - (stat.availableBlocksLong * blockSize)
            val usagePercentage = ((usedSpace).toDouble() / totalSpace.toDouble()) * 100
            return usagePercentage < MAX_STORAGE_USAGE_RATIO
        } catch (e: Exception) {
            Log.e(TwinMindApp.TAG, "Storage check failed.", e)
            return false
        }
    }

    private fun updateChunkPath() {
        chunkPath = File(filesDir, "chunk_${System.currentTimeMillis()}.mp4").absolutePath
    }

    companion object {
        const val NOTIFICATION_ID = 101

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"

        private const val MEDIA_FORMAT: Int = MediaRecorder.OutputFormat.MPEG_4
        private const val MEDIA_ENCODER: Int = MediaRecorder.AudioEncoder.AAC
        private const val MEDIA_SOURCE: Int = MediaRecorder.AudioSource.MIC

        private const val MAX_STORAGE_USAGE_RATIO = 90.0

        private const val CHUNK_STEPS = 30000L
        private const val DEFAULT_STEPS = 1000L
    }
}
