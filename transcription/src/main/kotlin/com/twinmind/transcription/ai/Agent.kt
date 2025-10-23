package com.twinmind.transcription.ai

interface Agent {
    val name: String

    val isEnabled: Boolean

    fun transcribe(level: Float): String

    companion object {
        const val DETAIL_LOW = 0f
        const val DETAIL_MEDIUM = 1f
        const val DETAIL_HIGH = 2f

        private val AGENTS =
            mapOf(
                OpenAiAgent.name to OpenAiAgent,
                GoogleAgent.name to GoogleAgent,
                MockAgent.name to MockAgent,
            )

        fun all(): Collection<Agent> = AGENTS.values

        fun find(name: String): Agent = AGENTS[name]!!
    }
}
