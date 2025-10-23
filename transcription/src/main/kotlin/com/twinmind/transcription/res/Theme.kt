package com.twinmind.transcription.res

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun TwinMindTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let {
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme =
            darkColorScheme(
                primary = Color(0xFF5BA3D0),
                secondary = Color(0xFFFF9A5C),
            ).takeIf { darkTheme }
                ?: lightColorScheme(
                    primary = COLOR_BLUE,
                    secondary = COLOR_ORANGE,
                ),
        typography = Typography(),
        content = content,
    )
}
