package com.twinmind.transcription.ai

import com.twinmind.transcription.db.ChunkStatus
import com.twinmind.transcription.db.schema.Chunk
import com.twinmind.transcription.rest.OpenAiApi
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlin.text.trim

class OpenAiAgent
    @Inject
    constructor(private val api: OpenAiApi) : Agent(NAME) {
        override suspend fun transcribe(
            chunks: Collection<Chunk>,
            detail: Float,
            loading: MutableStateFlow<Boolean>,
            error: MutableStateFlow<String>,
        ): String {
            val transcriptionBuilder = StringBuilder()
            var errorCounter = 0

            loading.value = true
            chunks.forEachIndexed { i, chunk ->
                if (chunk.status != ChunkStatus.PENDING) {
                    return@forEachIndexed
                }
                try {
                    val transcription = api.transcribeAudio(chunk.file)
                    if (!transcription.isNullOrBlank()) {
                        if (i != 0) {
                            transcriptionBuilder.append(' ')
                        }
                        transcriptionBuilder.append(transcription)
                        chunk.transcript = transcription.trim()
                        chunk.status = ChunkStatus.TRANSCRIBED
                    } else {
                        chunk.status = ChunkStatus.FAILED
                    }
                } catch (_: Exception) {
                    errorCounter++
                    chunk.status = ChunkStatus.FAILED
                }
            }
            loading.value = false
            val summary =
                api
                    .summarizeText(getSummaryPrompt(detail) + transcriptionBuilder)
                    .orEmpty()

            if (errorCounter > 0 || summary.isEmpty()) {
                error.value = "There were errors."
            }
            return api
                .summarizeText(getSummaryPrompt(detail) + transcriptionBuilder)
                .orEmpty()
        }

        companion object {
            const val NAME = "OpenAI Whisper 1"
        }
    }
