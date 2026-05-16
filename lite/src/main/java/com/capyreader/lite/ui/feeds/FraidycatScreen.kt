package com.capyreader.lite.ui.feeds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.lite.R
import com.jocmp.capy.FeedImportance
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FraidycatScreen(
    onSelectFeed: (String) -> Unit,
    viewModel: FraidycatViewModel = koinInject(),
) {
    val buckets by viewModel.buckets.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { padding ->
        if (buckets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.feed_empty))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            buckets.forEach { bucket ->
                item(key = "h-${bucket.importance.name}") {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = bucket.importance.label(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        HorizontalDivider()
                    }
                }
                items(bucket.feeds, key = { it.id }) { feed ->
                    ListItem(
                        headlineContent = { Text(feed.title) },
                        supportingContent = if (feed.siteURL.isNotBlank()) {
                            { Text(feed.siteURL) }
                        } else null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onSelectFeed(feed.id) }
                            .padding(horizontal = 0.dp),
                        leadingContent = {
                            // Importance indicator dot could go here.
                        },
                        trailingContent = {
                            if (feed.count > 0) {
                                Text(feed.count.toString())
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedImportance.label(): String = when (this) {
    FeedImportance.REAL_TIME -> stringResource(R.string.importance_real_time)
    FeedImportance.DAILY -> stringResource(R.string.importance_daily)
    FeedImportance.NORMAL -> stringResource(R.string.importance_normal)
    FeedImportance.WEEKLY -> stringResource(R.string.importance_weekly)
    FeedImportance.MONTHLY -> stringResource(R.string.importance_monthly)
    FeedImportance.YEARLY -> stringResource(R.string.importance_yearly)
}
