package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capyreader.app.R
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun NewArticlesPill(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 4.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowUpward,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Text(stringResource(R.string.article_list_new_articles_pill), fontSize = 14.sp)
        }
    }
}

@PreviewLightDark
@Composable
private fun NewArticlesPillPreview() {
    CapyTheme {
        NewArticlesPill {}
    }
}
