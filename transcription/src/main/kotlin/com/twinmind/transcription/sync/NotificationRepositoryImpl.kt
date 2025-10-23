package com.twinmind.transcription.sync

import android.R.attr.action
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.twinmind.transcription.R
import com.twinmind.transcription.TwinMindApp
import com.twinmind.transcription.sync.MainService.Companion.ACTION_PAUSE
import com.twinmind.transcription.sync.MainService.Companion.ACTION_RESUME
import com.twinmind.transcription.sync.MainService.Companion.ACTION_STOP
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.jvm.java

class NotificationRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : NotificationRepository {
        private var channel: NotificationChannel? = null

        override fun create(state: State, pauseReason: PauseReason): Notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setContentTitle(state.message)
                .setContentText(pauseReason.message.takeIf { state == State.PAUSED })
                .setSmallIcon(R.drawable.ic_launcher_dark)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(state != State.STOPPED)
                .apply {
                    if (state == State.RECORDING) {
                        addAction(R.drawable.ic_pause, "Pause", ACTION_PAUSE.pendingIntent)
                        addAction(R.drawable.ic_stop, "Stop", ACTION_STOP.pendingIntent)
                    } else if (state == State.PAUSED) {
                        addAction(R.drawable.ic_play, "Resume", ACTION_RESUME.pendingIntent)
                        addAction(R.drawable.ic_stop, "Stop", ACTION_STOP.pendingIntent)
                    }
                }.build()

        @SuppressLint("NewApi")
        override fun update(notification: Notification, id: Int) {
            if (channel == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel =
                    NotificationChannel(
                        CHANNEL_ID,
                        TwinMindApp.TAG,
                        NotificationManager.IMPORTANCE_DEFAULT,
                    ).apply { description = TwinMindApp.TAG }
            }
            context
                .getSystemService(NotificationManager::class.java)
                .apply { channel?.let { createNotificationChannel(it) } }
                .notify(id, notification)
        }

        private val String.pendingIntent: PendingIntent
            get() =
                PendingIntent.getService(
                    context,
                    action.hashCode(),
                    Intent(context, MainService::class.java).apply {
                        this.action = this@pendingIntent
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )

        companion object {
            private const val CHANNEL_ID = "RECORDING_CHANNEL"
        }
    }
