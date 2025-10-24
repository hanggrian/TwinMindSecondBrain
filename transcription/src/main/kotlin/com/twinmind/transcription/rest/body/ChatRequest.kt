package com.twinmind.transcription.rest.body

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    @SerialName("model")
    val model: String,
    @SerialName("messages")
    val messages: List<ChatMessage>,
    @SerialName("temperature")
    val temperature: Double = 0.7,
)
