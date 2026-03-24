package com.capyreader.app.preferences

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.ui.graphics.vector.ImageVector
import com.capyreader.app.R
import com.jocmp.capy.articles.TextAlignment

val TextAlignment.icon: ImageVector
    get() = when (this) {
        TextAlignment.LEFT -> Icons.AutoMirrored.Outlined.FormatAlignLeft
        TextAlignment.CENTER -> Icons.Outlined.FormatAlignCenter
    }

val TextAlignment.translationKey: Int
    get() = when (this) {
        TextAlignment.LEFT -> R.string.title_alignment_left
        TextAlignment.CENTER -> R.string.title_alignment_center
    }
