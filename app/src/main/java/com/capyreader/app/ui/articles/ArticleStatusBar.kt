package com.capyreader.app.ui.articles

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.capy.ArticleStatus

@Composable
fun ArticleStatusBar(
    onSelectStatus: (status: ArticleStatus) -> Unit,
    status: ArticleStatus,
) {
    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, buttonStatus ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = {
                    if (buttonStatus != status) {
                        onSelectStatus(buttonStatus)
                    }
                },
                icon = {},
                selected = buttonStatus == status
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
