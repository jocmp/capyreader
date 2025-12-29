package com.capyreader.app.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarTooltip(
    message: String,
    content: @Composable () -> Unit,
) {
    val tooltipState = rememberTooltipState()

    TooltipBox(
        positionProvider = rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Below
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
