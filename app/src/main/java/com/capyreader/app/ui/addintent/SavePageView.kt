package com.capyreader.app.ui.addintent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun SavePageView(
    pageTitle: String,
    url: String,
    onSavePage: () -> Unit,
    loading: Boolean,
    error: String?,
) {
    Column(
        Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Column(
            Modifier.heightIn(max = 56.dp)
        ) {
            if (pageTitle.isNotBlank()) {
                Text(
                    text = pageTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (url.isNotBlank()) {
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,

                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        error?.let {
            val resource = when (it) {
                "network" -> R.string.save_page_network_error
                else -> R.string.save_page_error
            }
            Text(
                text = stringResource(resource),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Button(
            onClick = onSavePage,
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.save_page_submit))
                }
            }
        }
    }
}

@Preview
@Composable
private fun SavePageViewPreview() {
    CapyTheme {
        SavePageView(
            pageTitle = "How to Build an RSS Reader",
            url = "https://example.com/how-to-build-an-rss-reader",
            onSavePage = {},
            loading = false,
            error = null
        )
    }
}

@Preview
@Composable
private fun SavePageViewLoadingPreview() {
    CapyTheme {
        SavePageView(
            pageTitle = "How to Build an RSS Reader",
            url = "https://example.com/how-to-build-an-rss-reader",
            onSavePage = {},
            loading = true,
            error = null
        )
    }
}

@Preview
@Composable
private fun SavePageViewErrorPreview() {
    CapyTheme {
        SavePageView(
            pageTitle = "How to Build an RSS Reader",
            url = "https://example.com/how-to-build-an-rss-reader",
            onSavePage = {},
            loading = false,
            error = "network"
        )
    }
}
