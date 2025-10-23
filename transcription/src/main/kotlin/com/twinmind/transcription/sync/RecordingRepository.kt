package com.twinmind.transcription.sync

import com.twinmind.transcription.db.schema.Chunk

interface RecordingRepository {
    suspend fun saveChunk(chunk: Chunk)

    fun getAllChunks(): MutableList<Chunk>
}
