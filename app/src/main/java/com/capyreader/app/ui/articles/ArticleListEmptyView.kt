package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.capyreader.app.ui.isExpanded
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun ArticleListEmptyView() {
    val tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isExpanded()) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_empty_list),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.width(100.dp)
                )
            }
            Text(
                text = stringResource(R.string.article_list_empty),
                color = tint,
                fontStyle = FontStyle.Italic,
            )
        }
    }
}

@Preview
@Composable
private fun ArticleListEmptyViewPreview() {
    PreviewKoinApplication {
        CapyTheme {
            Surface {
                ArticleListEmptyView()
            }
        }
    }
}
