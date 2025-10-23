package com.twinmind.transcription.ai

import com.twinmind.transcription.ai.Agent.Companion.DETAIL_LOW
import com.twinmind.transcription.ai.Agent.Companion.DETAIL_MEDIUM

object MockAgent : Agent {
    override val name: String = "Mock"

    override val isEnabled: Boolean = true

    override fun transcribe(level: Float): String {
        val builder =
            StringBuilder(
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
            )
        if (level == DETAIL_LOW) {
            return builder.toString()
        }
        builder
            .append(" Lorem Ipsum has been the industry's standard dummy text ever since the ")
            .append("1500s, when an unknown printer took a galley of type and scrambled it to ")
            .append("make a type specimen book. It has survived not only five centuries, but also ")
            .append("the leap into electronic typesetting, remaining essentially unchanged. It ")
            .append("was popularised in the 1960s with the release of Letraset sheets containing ")
            .append("Lorem Ipsum passages, and more recently with desktop publishing software ")
            .append("like Aldus PageMaker including versions of Lorem Ipsum.")
        if (level == DETAIL_MEDIUM) {
            return builder.toString()
        }
        return builder
            .append("\n\nContrary to popular belief, Lorem Ipsum is not simply random text. It ")
            .append("has roots in a piece of classical Latin literature from 45 BC, making it ")
            .append("over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney ")
            .append("College in Virginia, looked up one of the more obscure Latin words, ")
            .append("consectetur, from a Lorem Ipsum passage, and going through the cites of the ")
            .append("word in classical literature, discovered the undoubtable source. Lorem Ipsum ")
            .append("comes from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" ")
            .append("(The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a ")
            .append("treatise on the theory of ethics, very popular during the Renaissance. The ")
            .append("first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a ")
            .append("line in section 1.10.32.")
            .toString()
    }
}
