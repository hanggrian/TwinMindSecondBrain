package com.twinmind.transcription.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twinmind.transcription.TwinMindApp
import com.twinmind.transcription.ai.Agent
import com.twinmind.transcription.db.schema.Chunk
import com.twinmind.transcription.sync.RecordingRepository
import com.twinmind.transcription.sync.Session
import com.twinmind.transcription.sync.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val recordingRepository: RecordingRepository,
        sessionRepository: SessionRepository,
    ) : ViewModel() {
        val loadingFlow = MutableStateFlow(false)

        val summaryFlow = MutableStateFlow("")

        val errorFlow = MutableStateFlow("")

        val sessionFlow: StateFlow<Session> =
            sessionRepository
                .getSessionFlow()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = Session(),
                )

        fun startTranscription(agent: String, detail: Float) {
            loadingFlow.value = true
            viewModelScope.launch {
                try {
                    summaryFlow.value = Agent.find(agent).transcribe(detail)
                } catch (e: Exception) {
                    errorFlow.value = e.message!!
                } finally {
                    loadingFlow.value = false
                }
            }
        }

        fun onNewChunkReceived(chunk: Chunk) {
            viewModelScope.launch {
                try {
                    recordingRepository.saveChunk(chunk)
                } catch (e: Exception) {
                    Log.e(TwinMindApp.Companion.TAG, "Failed to insert chunk: '${e.message}'")
                }
            }
        }
    }
