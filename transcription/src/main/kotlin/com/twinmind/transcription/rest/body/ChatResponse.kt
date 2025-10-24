package com.twinmind.transcription.rest.body

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(val id: String, val choices: List<ChatChoice>, val model: String)
