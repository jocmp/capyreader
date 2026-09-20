package com.capyreader.app.ui.articles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capyreader.app.ui.navigationTitle
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.ArticleStatus

@Composable
fun ArticleStatusBar(
    onSelectStatus: (status: ArticleStatus) -> Unit,
    status: ArticleStatus,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { buttonStatus ->
            StatusButton(
                status = buttonStatus,
                selected = buttonStatus == status,
                onSelect = { onSelectStatus(buttonStatus) },
            )
        }
    }
}

@Composable
private fun StatusButton(
    status: ArticleStatus,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val label = stringResource(status.navigationTitle)
    val colors = MaterialTheme.colorScheme

    val containerColor by animateColorAsState(
        targetValue = when {
            selected -> colors.surfaceContainerHighest
            else -> Color.Transparent
        },
        label = "statusContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            selected -> colors.onSurface
            else -> colors.onSurfaceVariant
        },
        label = "statusContent",
    )

    Surface(
        onClick = {
            if (!selected) {
                onSelect()
            }
        },
        selected = selected,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier.semantics { contentDescription = label },
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArticleStatusIcon(
                status = status,
                modifier = Modifier.size(iconSize(status)),
            )

            AnimatedVisibility(visible = selected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 0.8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun iconSize(status: ArticleStatus) = when (status) {
    ArticleStatus.UNREAD -> 12.dp
    else -> 20.dp
}

val options = listOf(
    ArticleStatus.STARRED,
    ArticleStatus.UNREAD,
    ArticleStatus.ALL,
)

@Preview
@Composable
private fun ArticleStatusBarPreview() {
    CapyTheme {
        ArticleStatusBar(
            onSelectStatus = {},
            status = ArticleStatus.UNREAD,
        )
    }
}
