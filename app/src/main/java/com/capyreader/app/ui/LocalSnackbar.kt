package com.capyreader.app.ui

import androidx.compose.runtime.compositionLocalOf

val LocalSnackbar = compositionLocalOf { SnackbarState {} }

data class SnackbarState(
    val showMessage: (message: String) -> Unit,
) : CapySnackbar {
    override fun show(message: String) {
        showMessage(message)
    }
}
