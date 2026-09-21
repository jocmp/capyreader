package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import com.jocmp.mallet.LinearArticle

@Composable
fun ArticleBody(
    article: LinearArticle,
    actions: ReaderActions,
    modifier: Modifier = Modifier,
    onElementPositioned: (index: Int, coordinates: LayoutCoordinates) -> Unit = { _, _ -> },
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(paragraphSpacing()),
        modifier = modifier.fillMaxWidth(),
    ) {
        article.elements.forEachIndexed { index, element ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates -> onElementPositioned(index, coordinates) }
            ) {
                ArticleElement(
                    element = element,
                    idToIndex = article.idToIndex,
                    actions = actions,
                    allowHorizontalScroll = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
