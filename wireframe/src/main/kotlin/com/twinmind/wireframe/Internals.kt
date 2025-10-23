package com.twinmind.wireframe

import android.content.Context
import android.text.Spanned
import android.widget.SimpleAdapter

const val TEXT1 = "text1"
const val TEXT2 = "text2"

typealias Item = Pair<Spanned, Spanned>

fun Context.createAdapter(vararg pairs: Pair<String, String>): SimpleAdapter =
    SimpleAdapter(
        this,
        pairs.map { mapOf(TEXT1 to it.first, TEXT2 to it.second) },
        android.R.layout.simple_list_item_2,
        arrayOf(TEXT1, TEXT2),
        intArrayOf(android.R.id.text1, android.R.id.text2),
    )
