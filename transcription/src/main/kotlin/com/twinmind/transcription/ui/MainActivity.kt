package com.twinmind.transcription.ui

import android.Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
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

        registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                return@registerForActivityResult
            }
            longToast("Permission denied.")
            finish()
        }.run {
            request(RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                request(POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                request(FOREGROUND_SERVICE_MICROPHONE)
            }
        }
    }

    private fun ActivityResultLauncher<String>.request(permission: String) {
        if (checkSelfPermission(permission) == PERMISSION_DENIED) {
            launch(permission)
        }
    }
}
