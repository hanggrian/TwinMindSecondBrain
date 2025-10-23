package com.twinmind.transcription.sync

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun update(session: Session)

    fun getSessionFlow(): Flow<Session>
}
