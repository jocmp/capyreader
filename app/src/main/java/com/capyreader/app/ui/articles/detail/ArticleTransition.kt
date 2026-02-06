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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.jocmp.capy.Article

private const val UPWARD = 1
private const val DOWNWARD = -1

private data class ArticleState(
    val article: Article,
    val previousId: String?,
    val nextId: String?,
)

@Composable
fun ArticleTransition(
    article: Article,
    previousArticleId: String? = null,
    nextArticleId: String? = null,
    content: @Composable (Article) -> Unit,
) {
    val (hasShownArticle, setShownArticle) = remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = ArticleState(
            article = article,
            previousId = previousArticleId,
            nextId = nextArticleId,
        ),
        transitionSpec = {
            if (!hasShownArticle) {
                fadeIn(tween(0)) togetherWith fadeOut(tween(0))
            } else {
                val direction = when (targetState.article.id) {
                    initialState.nextId -> UPWARD
                    initialState.previousId -> DOWNWARD
                    else -> UPWARD
                }

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
        },
        contentKey = { it.article.id },
        label = "articleTransition"
    ) {
        setShownArticle(true)

        remember { it }
            .run {
                content(it.article)
            }
    }
}
