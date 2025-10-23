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
import com.twinmind.transcription.db.schema.Chunk
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("InlinedApi")
class MainService : LifecycleService() {
    @Inject lateinit var recordingRepository: RecordingRepository

    @Inject lateinit var notificationsRepository: NotificationRepository

    @Inject lateinit var sessionRepository: SessionRepository

    @Inject lateinit var audioManager: AudioManager

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private lateinit var request: AudioFocusRequest

    private var recorder: MediaRecorder? = null
    private var job: Job? = null
    private var chunkOrder: Int = 0
    private var chunkPath: String? = null

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            sessionRepository.getSessionFlow().collect {
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

    @Suppress("deprecation")
    private fun startRecording() {
        if (!storageIsSufficient()) {
            stopRecording(GracefulTermination.LOW_STORAGE)
            return
        }
        updateChunkPath()

        audioManager.requestAudioFocus(request)

        recorder =
            MediaRecorder(this)
                .takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
                ?: MediaRecorder()
        recorder?.run {
            setAudioSource(MEDIA_SOURCE)
            setOutputFormat(MEDIA_FORMAT)
            setAudioEncoder(MEDIA_ENCODER)
            setOutputFile(chunkPath)
            try {
                prepare()
                start()
                chunkOrder = 1
                startChunkTimer()
            } catch (e: Exception) {
                Log.e(TwinMindApp.TAG, e.message!!)
                stopRecording(GracefulTermination.UNKNOWN)
                return
            }
        }

        scope.launch {
            notificationsRepository
                .update(notificationsRepository.create(State.RECORDING, PauseReason.NONE))
            sessionRepository.update(
                Session(
                    true,
                    state = State.RECORDING,
                    pauseReason = PauseReason.NONE,
                ),
            )
        }
    }

    private fun pauseRecording(reason: PauseReason) {
        scope.launch {
            val session = sessionRepository.getSessionFlow().first()
            if (session.state != State.RECORDING) {
                return@launch
            }
            sessionRepository.update(session.copy(state = State.PAUSED, pauseReason = reason))
            job?.cancel()

            recorder?.pause()
        }
    }

    private fun resumeRecording() {
        scope.launch {
            val session = sessionRepository.getSessionFlow().first()
            if (session.state != State.PAUSED) {
                return@launch
            }
            sessionRepository
                .update(session.copy(state = State.RECORDING, pauseReason = PauseReason.NONE))

            recorder?.resume()
            startChunkTimer()
        }
    }

    private fun stopRecording(termination: GracefulTermination) {
        scope.launch {
            sessionRepository.update(
                Session(false, state = State.STOPPED, gracefulTermination = termination),
            )
        }

        job?.cancel()

        try {
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
        } catch (e: Exception) {
            Log.e(TwinMindApp.TAG, e.message!!)
        }
        chunkPath = null

        audioManager.abandonAudioFocusRequest(request)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startChunkTimer() {
        job?.cancel()
        job =
            scope.launch {
                delay(CHUNK_STEPS)

                val path = chunkPath ?: return@launch
                recorder?.stop()
                recorder?.reset()
                recordingRepository.saveChunk(
                    Chunk(
                        filePath = path,
                        order = chunkOrder,
                    ),
                )

                updateChunkPath()
                chunkOrder++

                recorder?.run {
                    setOutputFile(chunkPath)
                    try {
                        prepare()
                        start()
                        startChunkTimer()
                    } catch (e: Exception) {
                        Log.e(TwinMindApp.TAG, e.message!!)
                        stopRecording(GracefulTermination.UNKNOWN)
                    }
                }
            }
    }

    private fun storageIsSufficient(): Boolean {
        try {
            val stat = StatFs(filesDir.path)
            val blockSize = stat.blockSizeLong
            val totalSpace = stat.blockCountLong * blockSize
            val usedSpace = totalSpace - (stat.availableBlocksLong * blockSize)
            val usagePercentage = ((usedSpace).toDouble() / totalSpace.toDouble()) * 100
            return usagePercentage < MAX_STORAGE_USAGE_RATIO
        } catch (e: Exception) {
            Log.e(TwinMindApp.TAG, "Storage check failed: ${e.message}", e)
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
    }
}
