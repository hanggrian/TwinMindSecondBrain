package com.twinmind.transcription.rest

import android.util.Log
import com.twinmind.transcription.BuildConfig
import com.twinmind.transcription.TwinMindApp
import com.twinmind.transcription.rest.body.ChatMessage
import com.twinmind.transcription.rest.body.ChatRequest
import com.twinmind.transcription.rest.body.ChatResponse
import com.twinmind.transcription.rest.body.Whisper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.util.InternalAPI
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(InternalAPI::class)
class OpenAiApi
    @Inject
    constructor(private val client: HttpClient) {
        suspend fun transcribeAudio(file: File): String? =
            try {
                client
                    .submitFormWithBinaryData(
                        TRANSCRIPTION_ENDPOINT,
                        formData {
                            append(
                                "file",
                                file.readBytes(),
                                Headers.build {
                                    append(
                                        HttpHeaders.ContentType,
                                        ContentType("audio", "mp4").toString(),
                                    )
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "form-data; name=\"file\"; filename=\"${file.name}\"",
                                    )
                                },
                            )
                            append("model", TRANSCRIPTION_MODEL)
                        },
                    ) {
                        headers.append(
                            HttpHeaders.Authorization,
                            "Bearer ${BuildConfig.OPENAI_API_KEY}",
                        )
                    }.body<Whisper>()
                    .text
            } catch (e: Exception) {
                Log.e(TwinMindApp.TAG, "Whisper API failed.", e)
                null
            }

        suspend fun summarizeText(text: String): String? =
            try {
                client
                    .post(SUMMARY_ENDPOINT) {
                        setBody(
                            ChatRequest(
                                SUMMARY_MODEL,
                                listOf(
                                    ChatMessage(
                                        "system",
                                        "You are to summarize a transcript generated from voice " +
                                            "recording.",
                                    ),
                                    ChatMessage("user", text),
                                ),
                            ),
                        )
                    }.body<ChatResponse>()
                    .choices
                    .firstOrNull()
                    ?.message
                    ?.content
                    ?.trim()
            } catch (e: Exception) {
                Log.e(TwinMindApp.TAG, "Summarization API failed.", e)
                null
            }

        companion object {
            private const val TRANSCRIPTION_ENDPOINT =
                "https://api.openai.com/v1/audio/transcriptions"

            private const val SUMMARY_ENDPOINT =
                "https://api.openai.com/v1/chat/completions"

            private const val TRANSCRIPTION_MODEL = "whisper-1"
            private const val SUMMARY_MODEL = "gpt-3.5-turbo"

            const val TIMEOUT = 6000L
        }
    }
