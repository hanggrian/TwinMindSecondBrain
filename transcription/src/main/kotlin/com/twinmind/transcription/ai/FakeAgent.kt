package com.twinmind.transcription.ai

import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.twinmind.transcription.db.ChunkStatus
import com.twinmind.transcription.db.schema.Chunk
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class FakeAgent
    @Inject
    constructor() : Agent(NAME) {
        override suspend fun transcribe(
            chunks: Collection<Chunk>,
            detail: Float,
            loading: MutableStateFlow<Boolean>,
            error: MutableStateFlow<String>,
        ): String {
            chunks
                .filter { it.status == ChunkStatus.PENDING }
                .forEach {
                    it.status = ChunkStatus.TRANSCRIBED
                    it.transcript = LoremIpsum().values.joinToString(" ").replace("\n", "")
                }
            return buildString {
                append("Lorem Ipsum is simply dummy text of the printing and typesetting industry.")
                if (detail == DETAIL_LOW) {
                    return toString()
                }
                append(" Lorem Ipsum has been the industry's standard dummy text ever since the ")
                append("1500s, when an unknown printer took a galley of type and scrambled it to ")
                append("make a type specimen book. It has survived not only five centuries, but ")
                append("also the leap into electronic typesetting, remaining essentially ")
                append("unchanged. It was popularised in the 1960s with the release of Letraset ")
                append("sheets containing Lorem Ipsum passages, and more recently with desktop ")
                append("publishing software like Aldus PageMaker including versions of Lorem")
                append(" Ipsum.")
                if (detail == DETAIL_MEDIUM) {
                    return toString()
                }
                appendLine()
                appendLine()
                append("Contrary to popular belief, Lorem Ipsum is not simply random text. It ")
                append("has roots in a piece of classical Latin literature from 45 BC, making it ")
                append("over 2000 years old. Richard McClintock, a Latin professor at ")
                append("Hampden-Sydney College in Virginia, looked up one of the more obscure ")
                append("Latin words, consectetur, from a Lorem Ipsum passage, and going through ")
                append("the cites of the word in classical literature, discovered the ")
                append("undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 ")
                append("of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by ")
                append("Cicero, written in 45 BC. This book is a treatise on the theory of ")
                append("ethics, very popular during the Renaissance. The first line of Lorem ")
                append("Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section ")
                append("1.10.32.")
            }
        }

        companion object {
            const val NAME = "Fake"
        }
    }
