package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.articles.FaviconBadge
import com.capyreader.app.ui.articles.feeds.edit.EditFeedDialog
import com.jocmp.capy.DailyCount
import com.jocmp.capy.FeedStats
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SubscriptionDetailView(
    feedStats: FeedStats?,
    onLoadStats: () -> Unit,
) {
    val (isEditOpen, setEditOpen) = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onLoadStats()
    }

    if (feedStats == null) {
        return
    }

    val feed = feedStats.feed

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FeedHeader(
            feed = feed,
            onEditClick = { setEditOpen(true) },
        )

        Spacer(Modifier.height(16.dp))

        ActivityChart(
            dailyCounts = feedStats.dailyCounts,
            chartDays = feedStats.chartDays,
        )

        Spacer(Modifier.height(24.dp))

        StatsSection(feedStats = feedStats)

        Spacer(Modifier.height(16.dp))
    }

    EditFeedDialog(
        feed = feed,
        isOpen = isEditOpen,
        onDismiss = { setEditOpen(false) },
    )
}

@Composable
private fun FeedHeader(
    feed: com.jocmp.capy.Feed,
    onEditClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                FaviconBadge(url = feed.faviconURL, size = 24.dp)
                Text(
                    text = feed.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedButton(onClick = onEditClick) {
                Text(stringResource(R.string.subscriptions_edit))
            }
        }
    }
}

@Composable
private fun ActivityChart(dailyCounts: List<DailyCount>, chartDays: Long) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val chartColor = MaterialTheme.colorScheme.primary
    val allDays = remember(dailyCounts, chartDays) { buildFullDayRange(dailyCounts, chartDays) }

    LaunchedEffect(dailyCounts) {
        if (allDays.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries {
                    series(allDays.map { it.count })
                }
            }
        }
    }

    val bottomAxisFormatter = remember(allDays) {
        CartesianValueFormatter { _, value, _ ->
            val index = value.toInt()
            if (index >= 0 && index < allDays.size) {
                allDays[index].day.format(DateTimeFormatter.ofPattern("MMM d"))
            } else {
                ""
            }
        }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = Fill(chartColor),
                            thickness = 4.dp,
                        )
                    ),
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = bottomAxisFormatter,
                    itemPlacer = remember(chartDays) {
                        val spacing = (chartDays / 3).coerceAtLeast(1)
                        HorizontalAxis.ItemPlacer.aligned(spacing = { spacing.toInt() })
                    },
                ),
            ),
            modelProducer = modelProducer,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )
    }
}

@Composable
private fun StatsSection(feedStats: FeedStats) {
    Text(
        text = "Stats",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp),
    )

    HorizontalDivider()

    StatRow(
        label = stringResource(R.string.subscriptions_latest_article),
        value = feedStats.latestArticleAt?.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        ) ?: stringResource(R.string.subscriptions_no_articles),
    )

    HorizontalDivider()

    StatRow(
        label = stringResource(R.string.subscriptions_volume),
        value = stringResource(R.string.subscriptions_articles_per_month, feedStats.volume),
    )

    HorizontalDivider()

    if (feedStats.feed.siteURL.isNotBlank()) {
        StatRow(
            label = stringResource(R.string.subscriptions_website),
            value = feedStats.feed.siteURL,
        )
        HorizontalDivider()
    }

    StatRow(
        label = stringResource(R.string.subscriptions_source),
        value = feedStats.feed.feedURL,
    )

    HorizontalDivider()
}

@Composable
private fun StatRow(label: String, value: String) {
    ListItem(
        headlineContent = {
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

private fun buildFullDayRange(dailyCounts: List<DailyCount>, chartDays: Long): List<DailyCount> {
    val end = LocalDate.now()
    val start = end.minusDays(chartDays - 1)
    val countMap = dailyCounts.associate { it.day to it.count }

    return (0L until chartDays).map { offset ->
        val day = start.plusDays(offset)
        DailyCount(day = day, count = countMap[day] ?: 0)
    }
}
