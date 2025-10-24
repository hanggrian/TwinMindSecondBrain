package com.twinmind.transcription.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twinmind.transcription.db.schema.Chunk

@Dao
interface Chunks {
    @Query("SELECT * FROM chunks")
    fun getAll(): MutableList<Chunk>

    @Insert
    suspend fun insert(chunk: Chunk)

    @Update
    suspend fun update(chunk: Chunk)

    @Delete
    suspend fun delete(chunk: Chunk)
}
