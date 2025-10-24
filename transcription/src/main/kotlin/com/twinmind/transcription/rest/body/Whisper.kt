package com.twinmind.transcription.rest.body

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Whisper(
    @SerialName("text")
    val text: String? = null,
)
