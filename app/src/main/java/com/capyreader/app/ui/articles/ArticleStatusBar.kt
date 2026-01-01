package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.capy.ArticleStatus

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArticleStatusBar(
    onSelectStatus: (status: ArticleStatus) -> Unit,
    status: ArticleStatus,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, buttonStatus ->
            ToggleButton(
                checked = buttonStatus == status,
                onCheckedChange = {
                    if (buttonStatus != status) {
                        onSelectStatus(buttonStatus)
                    }
                },
                modifier = Modifier.weight(1f),
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
            ) {
                ArticleStatusIcon(status = buttonStatus)
            }
        }
    }
}

val options = listOf(
    ArticleStatus.ALL,
    ArticleStatus.UNREAD,
    ArticleStatus.STARRED,
)

@Composable
@Preview
fun ArticleStatusBarPreview() {
    ArticleStatusBar(
        onSelectStatus = {},
        status = ArticleStatus.UNREAD
    )
}
