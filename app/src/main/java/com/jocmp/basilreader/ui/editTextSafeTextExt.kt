package com.jocmp.basilreader.ui

import android.widget.EditText

val EditText?.safeText: String get() =
    this?.editableText?.toString().orEmpty().trim()
