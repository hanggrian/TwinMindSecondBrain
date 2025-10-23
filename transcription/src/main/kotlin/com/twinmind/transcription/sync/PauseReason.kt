package com.twinmind.transcription.sync

enum class PauseReason(val message: String) {
    NONE("User paused"),
    PHONE_CALL("Phone call"),
    AUDIO_FOCUS_LOST("Audio focus lost"),
}
