package com.twinmind.transcription.rest.body

import kotlinx.serialization.Serializable

@Serializable
data class ChatChoice(val index: Int, val message: ChatMessage)
