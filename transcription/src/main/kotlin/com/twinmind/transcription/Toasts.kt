package com.twinmind.transcription

import android.content.Context
import android.widget.Toast

inline fun Context.toast(text: String): Unit = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

inline fun Context.longToast(text: String): Unit =
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
