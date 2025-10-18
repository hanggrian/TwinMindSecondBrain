package com.twinmind.brain

import android.widget.TextView

class TextViewImpl(val text: TextView) {
    val size: Int get() = text.width * text.height
}
