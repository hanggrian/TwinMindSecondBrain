package com.twinmind.transcription

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TwinMindApp : MultiDexApplication() {
    companion object {
        const val TAG = "TwinMindTranscription"
    }
}
