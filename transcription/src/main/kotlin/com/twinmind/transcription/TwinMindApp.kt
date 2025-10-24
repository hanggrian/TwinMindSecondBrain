package com.twinmind.transcription

import androidx.multidex.MultiDexApplication
import com.twinmind.transcription.db.Chunks
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Reset data on startup until dashboard is ready. */
@HiltAndroidApp
@OptIn(DelicateCoroutinesApi::class)
class TwinMindApp : MultiDexApplication() {
    @Inject lateinit var chunks: Chunks

    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch(Dispatchers.IO) {
            chunks.getAll().forEach {
                val file = it.file
                if (file.exists()) {
                    file.delete()
                }
                chunks.delete(it)
            }
        }
    }

    companion object {
        const val TAG = "TwinMindTranscription"
    }
}
