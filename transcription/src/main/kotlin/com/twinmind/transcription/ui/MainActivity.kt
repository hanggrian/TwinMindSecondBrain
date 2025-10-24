package com.twinmind.transcription.ui

import android.Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.RECORD_AUDIO
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.twinmind.transcription.longToast
import com.twinmind.transcription.res.TwinMindTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this).get<MainViewModel>()
        setContent {
            TwinMindTheme {
                MainScreen()
            }
        }

        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            ActivityResultCallback {
                if (!it.containsValue(false)) {
                    return@ActivityResultCallback
                }
                longToast("Permission denied.")
                finish()
            },
        ).run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                launch(arrayOf(RECORD_AUDIO, POST_NOTIFICATIONS, FOREGROUND_SERVICE_MICROPHONE))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launch(arrayOf(RECORD_AUDIO, POST_NOTIFICATIONS))
            } else {
                launch(arrayOf(RECORD_AUDIO))
            }
        }
    }
}
