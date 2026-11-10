package com.capyreader.app.ui.articles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.LocalMarkAllReadButtonPosition
import com.capyreader.app.ui.LocalUnreadCount
import com.capyreader.app.ui.articles.list.MarkAllReadFloatingActionButton

/**
 * Lays out the article list's floating actions as `[ player ][ fab ]`.
 * Without the mark all read FAB, the player takes the full width.
 *
 * Meant for a Scaffold's center-positioned FAB slot.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArticleListFloatingActions(
    modifier: Modifier = Modifier,
    player: (@Composable RowScope.() -> Unit)? = null,
) {
    val markReadPosition = LocalMarkAllReadButtonPosition.current
    val unreadCount = LocalUnreadCount.current
    val showMarkAllRead =
        markReadPosition == MarkReadPosition.FLOATING_ACTION_BUTTON && unreadCount > 0

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FabSpacing),
    ) {
        if (player != null) {
            player()
        }

        AnimatedVisibility(
            visible = showMarkAllRead,
            enter = expandHorizontally(
                animationSpec = motionScheme.fastSpatialSpec(),
                expandFrom = Alignment.Start,
                clip = false,
            ) + scaleIn(
                animationSpec = motionScheme.fastSpatialSpec(),
                initialScale = FabHiddenScale,
            ) + fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
            exit = shrinkHorizontally(
                animationSpec = motionScheme.fastSpatialSpec(),
                shrinkTowards = Alignment.Start,
                clip = false,
            ) + scaleOut(
                animationSpec = motionScheme.fastSpatialSpec(),
                targetScale = FabHiddenScale,
            ) + fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (player != null) {
                    Spacer(modifier = Modifier.width(PlayerToFabGap))
                }
                MarkAllReadFloatingActionButton()
            }
        }
    }
}

private const val FabHiddenScale = 0.2f

private val FabSpacing = 16.dp

private val PlayerToFabGap = 12.dp
