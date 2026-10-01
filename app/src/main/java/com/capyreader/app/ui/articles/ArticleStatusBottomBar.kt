package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import com.capyreader.app.ui.LocalMarkAllReadButtonPosition
import com.capyreader.app.ui.LocalUnreadCount
import com.capyreader.app.ui.articles.detail.ArticleBarDefaults
import com.capyreader.app.ui.articles.list.MarkAllReadFloatingActionButton
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.ArticleStatus

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArticleStatusBottomBar(
    status: ArticleStatus,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val markReadPosition = LocalMarkAllReadButtonPosition.current
    val unreadCount = LocalUnreadCount.current
    val showMarkAllRead = markReadPosition == MarkReadPosition.FLOATING_ACTION_BUTTON && unreadCount > 0
    val toolbarModifier = Modifier.height(ArticleBarDefaults.FloatingToolbarHeight)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = ArticleBarDefaults.FloatingToolbarBottomGap),
    ) {
        if (showMarkAllRead) {
            HorizontalFloatingToolbar(
                expanded = true,
                floatingActionButton = {
                    MarkAllReadFloatingActionButton()
                },
                modifier = toolbarModifier,
            ) {
                FixedWidthStatusBar(
                    status = status,
                    onSelectStatus = onSelectStatus,
                )
            }
        } else {
            HorizontalFloatingToolbar(
                expanded = true,
                modifier = toolbarModifier,
            ) {
                ArticleStatusBar(
                    status = status,
                    onSelectStatus = onSelectStatus,
                )
            }
        }
    }
}

@Composable
private fun FixedWidthStatusBar(
    status: ArticleStatus,
    onSelectStatus: (status: ArticleStatus) -> Unit,
) {
    Layout(
        content = {
            options.forEach { option ->
                ArticleStatusBar(
                    status = option,
                    onSelectStatus = {},
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
            ArticleStatusBar(
                status = status,
                onSelectStatus = onSelectStatus,
            )
        },
        measurePolicy = WidestVariantMeasurePolicy,
    )
}

private object WidestVariantMeasurePolicy : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val width = measurables.dropLast(1).maxOf { it.measure(constraints).width }
        val bar = measurables.last().measure(constraints)

        return layout(width, bar.height) {
            bar.place(x = (width - bar.width) / 2, y = 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ) = widestVariant(measurables, height)

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ) = widestVariant(measurables, height)

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ) = measurables.last().minIntrinsicHeight(width)

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ) = measurables.last().maxIntrinsicHeight(width)

    private fun widestVariant(measurables: List<IntrinsicMeasurable>, height: Int): Int {
        return measurables.dropLast(1).maxOf { it.maxIntrinsicWidth(height) }
    }
}

@Preview
@Composable
private fun ArticleStatusBottomBarPreview() {
    CapyTheme {
        ArticleStatusBottomBar(
            status = ArticleStatus.UNREAD,
            onSelectStatus = {},
        )
    }
}

@Preview
@Composable
private fun ArticleStatusBottomBarFloatingActionButtonPreview() {
    CapyTheme {
        CompositionLocalProvider(
            LocalMarkAllReadButtonPosition provides MarkReadPosition.FLOATING_ACTION_BUTTON,
            LocalUnreadCount provides 3L,
        ) {
            ArticleStatusBottomBar(
                status = ArticleStatus.UNREAD,
                onSelectStatus = {},
            )
        }
    }
}
