package com.twinmind.transcription.ai

import com.twinmind.transcription.db.schema.Chunk
import kotlinx.coroutines.flow.MutableStateFlow

abstract class Agent(val name: String) {
    val transcriptionPrompt = "Transcribe this audio chunk, ignoring any background noise."

    fun getSummaryPrompt(detail: Float): String =
        when (detail) {
            DETAIL_LOW -> "Extract key points to a single sentence:\n\n> "
            DETAIL_MEDIUM -> "Summarize text into a single paragraph:\n\n> "
            DETAIL_HIGH -> "Explain transcription in multiple paragraphs:\n\n> "
            else -> error("Out-of-range detail.")
        }

    open fun isEnabled(): Boolean = true

    abstract suspend fun transcribe(
        chunks: Collection<Chunk>,
        detail: Float,
        loading: MutableStateFlow<Boolean>,
        error: MutableStateFlow<String>,
    ): String

    companion object {
        const val DETAIL_LOW = 0f
        const val DETAIL_MEDIUM = 1f
        const val DETAIL_HIGH = 2f
    }
}
