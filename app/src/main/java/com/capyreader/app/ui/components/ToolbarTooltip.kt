package com.capyreader.app.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarTooltip(
    message: String,
    content: @Composable () -> Unit,
) {
    val tooltipState = rememberTooltipState()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(
            spacingBetweenTooltipAndAnchor = 16.dp
        ),
        tooltip = {
            PlainTooltip {
                Text(message)
            }
        },
        state = tooltipState
    ) {
        content()
    }
}
