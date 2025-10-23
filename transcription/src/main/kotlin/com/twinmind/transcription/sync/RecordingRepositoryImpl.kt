package com.twinmind.transcription.sync

import com.twinmind.transcription.db.Chunks
import com.twinmind.transcription.db.schema.Chunk
import javax.inject.Inject

class RecordingRepositoryImpl
    @Inject
    constructor(private val chunks: Chunks) :
    RecordingRepository {
        override suspend fun saveChunk(chunk: Chunk) {
            chunks.insert(chunk)
        }

        override fun getAllChunks(): MutableList<Chunk> = chunks.getAll()
    }
