package com.twinmind.transcription.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.twinmind.transcription.BuildConfig
import com.twinmind.transcription.db.ChunkStatus
import com.twinmind.transcription.db.schema.Chunk
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.FileInputStream
import javax.inject.Inject

class GoogleAgent
    @Inject
    constructor() : Agent(NAME) {
        override suspend fun transcribe(
            chunks: Collection<Chunk>,
            detail: Float,
            loading: MutableStateFlow<Boolean>,
            error: MutableStateFlow<String>,
        ): String {
            if (model == null) {
                model = GenerativeModel(MODEL, BuildConfig.GEMINI_API_KEY)
            }
            val transcriptionBuilder = StringBuilder()
            var errorCounter = 0

            loading.value = true
            chunks
                .filter { it.status == ChunkStatus.PENDING }
                .forEachIndexed { i, chunk ->
                    try {
                        val transcription =
                            model!!
                                .generateContent(
                                    content {
                                        blob(
                                            "audio/mp3",
                                            FileInputStream(chunk.file).use { it.readBytes() },
                                        )
                                        text(transcriptionPrompt)
                                    },
                                ).text
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
            val summary =
                model!!
                    .generateContent(getSummaryPrompt(detail) + transcriptionBuilder)
                    .text
                    .orEmpty()
            loading.value = false

            if (errorCounter > 0 || summary.isEmpty()) {
                error.value = "There were errors."
            }
            return summary
        }

        companion object {
            const val NAME = "Google Gemini 2.5 Flash"

            private const val MODEL = "gemini-2.5-flash"

            private var model: GenerativeModel? = null
        }
    }
