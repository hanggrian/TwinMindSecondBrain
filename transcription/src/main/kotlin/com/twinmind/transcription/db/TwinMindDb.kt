package com.twinmind.transcription.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.twinmind.transcription.db.schema.Chunk

@Database(entities = [Chunk::class], version = 1)
abstract class TwinMindDb : RoomDatabase() {
    abstract fun chunks(): Chunks

    companion object {
        const val NAME = "twinmind-db"
    }
}
