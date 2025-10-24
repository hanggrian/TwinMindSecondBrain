package com.twinmind.transcription.res

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun TwinMindTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            darkColorScheme(
                primary = Colors.BlueVibrant,
                secondary = Colors.OrangeVibrant,
            ).takeIf { darkTheme }
                ?: lightColorScheme(
                    primary = Colors.Blue,
                    secondary = Colors.Orange,
                ),
        typography = Typography(),
        content = content,
    )

    val view =
        LocalView.current
            .takeIf { !it.isInEditMode }
            ?: return
    SideEffect {
        (view.context as? Activity)?.window?.let {
            WindowCompat
                .getInsetsController(it, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }
}
