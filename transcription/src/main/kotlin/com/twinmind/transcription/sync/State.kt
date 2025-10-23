package com.twinmind.transcription.sync

enum class State(val message: String) {
    IDLE("Ready"),
    RECORDING("Recording..."),
    PAUSED("Paused"),
    STOPPED("Stopped"),
}
