package com.jocmp.basilreader.ui.articles

import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.text.Html.ImageGetter
import android.text.Html.TagHandler
import android.util.Log
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.jocmp.basil.Article
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.components.EmptyView
import com.jocmp.basilreader.ui.components.WebView
import com.jocmp.basilreader.ui.components.rememberWebViewStateWithHTMLData
import org.xml.sax.XMLReader

private const val TAG = "ArticleView"

@Composable
fun ArticleView(
    article: Article?,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit
) {
    if (article != null) {
        ArticleLoadedView(
            article = article,
            onToggleRead = onToggleRead,
            onToggleStar = onToggleStar
        )
    } else {
        EmptyView()
    }

    BackHandler(article != null) {
        onBackPressed()
    }
}

@Composable
fun ArticleLoadedView(
    article: Article,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit
) {
    val state = rememberWebViewStateWithHTMLData(ArticleRenderer(article).render())

    val readIcon = if (article.read) {
        R.drawable.icon_circle_outline
    } else {
        R.drawable.icon_circle_filled
    }

    val starIcon = if (article.starred) {
        R.drawable.icon_star_filled
    } else {
        R.drawable.icon_star_outline
    }

    Scaffold(
        topBar = {
            Row {
                IconButton(onClick = { onToggleRead() }) {
                    Icon(painterResource(id = readIcon), contentDescription = null)
                }
                IconButton(onClick = { onToggleStar() }) {
                    Icon(painterResource(id = starIcon), contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            WebView(state)
        }
    }
}
