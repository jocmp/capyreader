package com.capyreader.app.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RowItem(
    content: @Composable () -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        content()
    }
}
