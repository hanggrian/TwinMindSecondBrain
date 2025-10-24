package com.twinmind.transcription.sync

data class Session(
    val isRecording: Boolean = false,
    val state: State = State.IDLE,
    val elapsedTime: Long = 0L,
    val pauseReason: PauseReason = PauseReason.NONE,
    val gracefulTermination: GracefulTermination = GracefulTermination.DEFAULT,
)
