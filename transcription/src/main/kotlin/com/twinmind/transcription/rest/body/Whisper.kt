package com.twinmind.transcription.rest.body

import kotlinx.serialization.Serializable

@Serializable
data class Whisper(val text: String? = null)
