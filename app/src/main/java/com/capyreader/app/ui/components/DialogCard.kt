package com.capyreader.app.ui.components

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DialogCard(content: @Composable () -> Unit) {
    Card(
        Modifier.sizeIn(maxHeight = 600.dp, maxWidth = 360.dp)
    ) {
        content()
    }
}
