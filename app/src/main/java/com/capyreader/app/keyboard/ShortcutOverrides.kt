package com.capyreader.app.keyboard

import kotlinx.serialization.Serializable

@Serializable
data class ShortcutOverrides(
    val bindings: Map<ShortcutAction, List<ShortcutKey>> = emptyMap()
)
