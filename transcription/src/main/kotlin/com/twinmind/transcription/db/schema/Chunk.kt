package com.twinmind.transcription.db.schema

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.twinmind.transcription.db.ChunkStatus
import java.io.File

@Entity(tableName = "chunks")
data class Chunk(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "file_path")
    val filePath: String,
    @ColumnInfo(name = "order")
    val order: Int,
    @ColumnInfo(name = "status")
    val status: ChunkStatus = ChunkStatus.PENDING,
    @ColumnInfo(name = "transcript")
    val transcript: String? = null,
) {
    val file: File get() = File(filePath)
}
