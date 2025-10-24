package com.twinmind.transcription.rest.body

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(val role: String, val content: String)
