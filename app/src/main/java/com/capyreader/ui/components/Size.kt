package com.capyreader.ui.components

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Stable
fun Modifier.widthMaxSingleColumn() = then(Modifier.widthIn(max = 600.dp))
