package com.twinmind.transcription

import com.google.common.truth.Truth.assertThat
import com.twinmind.transcription.ai.Agent
import kotlin.test.Test

class AgentTest {
    @Test
    fun test() {
        val mockAgent = Agent.find("Mock")
        assertThat(mockAgent.transcribe(0f))
            .isEqualTo("Lorem Ipsum is simply dummy text of the printing and typesetting industry.")
        assertThat(mockAgent.transcribe(1f))
            .contains("industry. ")
        assertThat(mockAgent.transcribe(2f))
            .contains("\n\n")
    }
}
