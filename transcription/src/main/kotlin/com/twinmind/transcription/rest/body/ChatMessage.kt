package com.twinmind.transcription.rest.body

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String,
)
