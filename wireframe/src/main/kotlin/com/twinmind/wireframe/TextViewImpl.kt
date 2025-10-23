package com.twinmind.wireframe

import android.widget.TextView

class TextViewImpl(val text: TextView) {
    val size: Int get() = text.width * text.height
}
