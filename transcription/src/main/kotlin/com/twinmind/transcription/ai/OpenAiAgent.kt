package com.twinmind.transcription.ai

object OpenAiAgent : Agent {
    override val name: String = "OpenAI Whisper"

    override val isEnabled: Boolean = false

    override fun transcribe(level: Float): String = ""
}
