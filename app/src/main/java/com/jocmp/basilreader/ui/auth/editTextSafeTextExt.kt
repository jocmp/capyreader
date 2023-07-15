package com.jocmp.basilreader.ui.auth

import android.widget.EditText

val EditText?.safeText: String get() =
    this?.editableText?.toString().orEmpty().trim()
