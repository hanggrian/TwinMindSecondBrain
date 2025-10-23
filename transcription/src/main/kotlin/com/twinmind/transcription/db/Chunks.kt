package com.twinmind.transcription.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.twinmind.transcription.db.schema.Chunk

@Dao
interface Chunks {
    @Query("SELECT * FROM chunks")
    fun getAll(): MutableList<Chunk>

    @Insert
    suspend fun insert(chunk: Chunk)
}
