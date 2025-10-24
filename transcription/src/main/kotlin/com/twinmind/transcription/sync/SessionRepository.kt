package com.twinmind.transcription.sync

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val flow: Flow<Session>

    suspend fun update(session: Session)

    suspend fun updateElapsedTime(elapsedTime: Long)
}
