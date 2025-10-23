package com.twinmind.transcription.ai

object GoogleAgent : Agent {
    override val name: String = "Google Gemini 2.5 Flash"

    override val isEnabled: Boolean = false

    override fun transcribe(level: Float): String = ""
}
