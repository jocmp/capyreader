package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.navigationTitle
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.ArticleStatus
import kotlin.math.roundToInt

/**
 * @param collapsedFraction 0f when fully shown, 1f when scrolled away. The items slide down
 * behind the system navigation bar while the inset itself stays reserved, so the Scaffold's
 * content padding, snackbar and floating action button never fall below it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArticleStatusBottomBar(
    status: ArticleStatus,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    modifier: Modifier = Modifier,
    collapsedFraction: () -> Float = { 0f },
) {
    val isCollapsed by remember {
        derivedStateOf { collapsedFraction() == 1f }
    }
    val containerColor = ShortNavigationBarDefaults.containerColor

    Column(
        modifier = modifier.drawBehind {
            drawRect(color = containerColor.copy(alpha = 1f - collapsedFraction()))
        }
    ) {
        ShortNavigationBar(
            containerColor = containerColor,
            windowInsets = ShortNavigationBarDefaults.windowInsets
                .only(WindowInsetsSides.Horizontal)
                .listPaneInsets(),
            modifier = Modifier
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val height = (placeable.height * (1f - collapsedFraction())).roundToInt()

                    layout(placeable.width, height) {
                        placeable.place(0, height - placeable.height)
                    }
                }
                .then(
                    if (isCollapsed) {
                        Modifier.clearAndSetSemantics {}
                    } else {
                        Modifier
                    }
                ),
        ) {
            options.forEach { option ->
                ShortNavigationBarItem(
                    selected = option == status,
                    onClick = { onSelectStatus(option) },
                    icon = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(24.dp),
                        ) {
                            ArticleStatusIcon(
                                status = option,
                                modifier = Modifier.size(iconSize(option)),
                            )
                        }
                    },
                    label = {
                        Text(text = stringResource(option.navigationTitle))
                    },
                )
            }
        }
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

internal fun iconSize(status: ArticleStatus) = when (status) {
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
private fun ArticleStatusBottomBarPreview() {
    CapyTheme {
        ArticleStatusBottomBar(
            status = ArticleStatus.UNREAD,
            onSelectStatus = {},
        )
    }
}
