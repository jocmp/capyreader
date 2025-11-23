package com.capyreader.app.ui.articles.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset

enum class ArticleDirection {
    UPWARD,
    DOWNWARD,
    NONE
}

@Composable
fun ArticleTransition(
    article: ArticleTransitionState,
    content: @Composable (ArticleTransitionState) -> Unit,
) {
    AnimatedContent(
        targetState = article,
        transitionSpec = {
            val direction = when {
                initialState.nextArticleId == targetState.articleId -> ArticleDirection.UPWARD
                initialState.previousArticleId == targetState.articleId -> ArticleDirection.DOWNWARD
                else -> ArticleDirection.NONE
            }

            val directionMultiplier = when (direction) {
                ArticleDirection.UPWARD -> 1
                ArticleDirection.DOWNWARD -> -1
                ArticleDirection.NONE -> 0
            }

            val exitDuration = 100
            val enterDuration = exitDuration * 2

            (slideInVertically(
                initialOffsetY = { (it * 0.2f * directionMultiplier).toInt() },
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessLow,
                ),
            ) + fadeIn(
                animationSpec = tween(enterDuration, delayMillis = exitDuration / 2),
                initialAlpha = 0.3f,
            )) togetherWith (slideOutVertically(
                targetOffsetY = { -(it * 0.2f * directionMultiplier).toInt() },
                animationSpec = tween(exitDuration)
            ) + fadeOut(
                animationSpec = tween(exitDuration)
            ))
        },
        label = "article_transition"
    ) { state ->
        content(state)
    }
}

data class ArticleTransitionState(
    val articleId: String,
    val previousArticleId: String?,
    val nextArticleId: String?,
)
