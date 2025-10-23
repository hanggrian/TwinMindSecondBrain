package com.twinmind.transcription.sync

import android.app.Notification

interface NotificationRepository {
    fun create(state: State, pauseReason: PauseReason = PauseReason.NONE): Notification

    fun update(notification: Notification, id: Int = MainService.NOTIFICATION_ID)
}
