package com.capyreader.app.ui.articles.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.jocmp.capy.Article

@Composable
fun ArticleTransition(
    article: Article,
    content: @Composable (Article) -> Unit,
) {
    val (hasShownArticle, setShownArticle) = remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = article,
        transitionSpec = {
//            if (hasShownArticle) {
//                fadeIn(tween()) togetherWith fadeOut(tween(100))
//            } else {
                fadeIn(tween(0)) togetherWith fadeOut(tween(0))
//            }
        },
        contentKey = { it.id },
        label = "articleTransition"
    ) { targetArticle ->
        setShownArticle(true)
        content(targetArticle)
    }
}
