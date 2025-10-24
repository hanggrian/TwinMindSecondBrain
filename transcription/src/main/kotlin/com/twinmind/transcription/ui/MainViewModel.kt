package com.twinmind.transcription.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twinmind.transcription.ai.Agent
import com.twinmind.transcription.db.Chunks
import com.twinmind.transcription.sync.Session
import com.twinmind.transcription.sync.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val chunks: Chunks,
        sessionRepository: SessionRepository,
        private val agentMap: Map<String, @JvmSuppressWildcards Agent>,
    ) : ViewModel() {
        val loadingFlow = MutableStateFlow(false)

        val summaryFlow = MutableStateFlow("")

        val transcriptionFlow = MutableStateFlow("")

        val errorFlow = MutableStateFlow("")

        val elapsedTimeFlow =
            sessionRepository.flow.map {
                val seconds = (it.elapsedTime / 1000) % 60
                val minutes = (it.elapsedTime / (1000 * 60)) % 60
                val hours = (it.elapsedTime / (1000 * 60 * 60))
                if (hours > 0) {
                    "%02d:%02d:%02d".format(hours, minutes, seconds)
                } else {
                    "%02d:%02d".format(minutes, seconds)
                }
            }

        val sessionFlow =
            sessionRepository.flow.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Session(),
            )

        val agents: Collection<Agent> get() = agentMap.values

        fun getAgent(name: String): Agent = agentMap[name] ?: error("Agent not found: $name")

        suspend fun startTranscription(agent: String, detail: Float) {
            val builder = StringBuilder()
            chunks
                .getAll()
                .also {
                    summaryFlow.value =
                        getAgent(agent).transcribe(it, detail, loadingFlow, errorFlow)
                }.forEachIndexed { i, chunk ->
                    chunks.delete(chunk)
                    val transcript = chunk.transcript ?: return@forEachIndexed
                    if (i != 0) {
                        builder.append(' ')
                    }
                    builder.append(transcript)
                }
            transcriptionFlow.value = builder.toString()
        }
    }
