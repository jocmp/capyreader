package com.capyreader.app.ui.articles.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.jocmp.capy.Article
import com.jocmp.mallet.LinearArticle

object ArticleDirection {
    const val UPWARD = 1
    const val DOWNWARD = -1
}

private const val UPWARD = ArticleDirection.UPWARD
private const val DOWNWARD = ArticleDirection.DOWNWARD

private data class ArticleState(
    val article: Article,
    val previousId: String?,
    val nextId: String?,
    val contentRevision: Int,
    val flattened: LinearArticle?,
)

@Composable
fun ArticleTransition(
    article: Article,
    enableHorizontalPager: Boolean = false,
    previousArticleId: String? = null,
    nextArticleId: String? = null,
    contentRevision: Int = 0,
    flattened: LinearArticle? = null,
    pendingDirection: Pair<String, Int>? = null,
    content: @Composable (Article, LinearArticle?) -> Unit,
) {
    val (hasShownArticle, setShownArticle) = remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = ArticleState(
            article = article,
            previousId = previousArticleId,
            nextId = nextArticleId,
            contentRevision = contentRevision,
            flattened = flattened,
        ),
        transitionSpec = {
            if (!hasShownArticle) {
                fadeIn(tween(0)) togetherWith fadeOut(tween(0))
            } else if (enableHorizontalPager) {
                fadeIn(tween(100)) togetherWith fadeOut(tween(200))
            } else {
                val targetId = targetState.article.id
                val direction = when {
                    targetId == initialState.article.id -> UPWARD
                    targetId == pendingDirection?.first -> pendingDirection.second
                    targetId == initialState.nextId -> UPWARD
                    targetId == initialState.previousId -> DOWNWARD
                    else -> null
                }

                if (direction == null) {
                    fadeIn(tween(100)) togetherWith fadeOut(tween(200))
                } else {
                    val exitDuration = 100
                    val enterDuration = exitDuration * 2

                    (slideInVertically(
                        initialOffsetY = { (it * 0.2f * direction).toInt() },
                        animationSpec = spring(
                            dampingRatio = .9f,
                            stiffness = Spring.StiffnessLow,
                        ),
                    ) + fadeIn(
                        tween(
                            delayMillis = exitDuration,
                            durationMillis = enterDuration,
                            easing = LinearOutSlowInEasing,
                        )
                    )) togetherWith (slideOutVertically(
                        targetOffsetY = { (it * -0.2f * direction).toInt() },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                    ) + fadeOut(
                        tween(
                            durationMillis = exitDuration,
                            easing = FastOutLinearInEasing,
                        )
                    ))
                }
            }
        },
        contentKey = { it.article.id to it.contentRevision },
        label = "articleTransition"
    ) {
        setShownArticle(true)

        val frozen = remember { it }
        val isCurrent = frozen.article.id == article.id && frozen.contentRevision == contentRevision
        val lastShown = remember { mutableStateOf(frozen.flattened) }

        if (isCurrent) {
            SideEffect { lastShown.value = flattened }
        }

        content(
            if (isCurrent) article else frozen.article,
            if (isCurrent) flattened else lastShown.value,
        )
    }
}
