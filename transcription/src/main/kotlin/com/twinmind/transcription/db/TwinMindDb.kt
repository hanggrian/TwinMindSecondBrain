package com.twinmind.transcription.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.twinmind.transcription.db.schema.Chunk

@Database(entities = [Chunk::class], version = 1)
abstract class TwinMindDb : RoomDatabase() {
    abstract fun chunks(): Chunks

    companion object {
        private const val NAME: String = "twinmind-db"

        fun from(context: Context, test: Boolean = false): TwinMindDb {
            var builder = Room.databaseBuilder(context, TwinMindDb::class.java, NAME)
            if (test) {
                builder = builder.allowMainThreadQueries()
            }
            return builder.build()
        }
    }
}
