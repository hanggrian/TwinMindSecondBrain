package com.twinmind.transcription.rest.body

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    @SerialName("id")
    val id: String,
    @SerialName("choices")
    val choices: List<ChatChoice>,
    @SerialName("model")
    val model: String,
)
