package com.capyreader.app.ui.accounts

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.titleKey
import com.capyreader.app.preferences.ThemeMode
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.accounts.Source

@Composable
fun AccountRow(
    source: Source
) {
    val item = buildItem(source = source)

    ListItem(
        headlineContent = {
            Text(text = item.title)
        },
        supportingContent = {
            Text(item.subtitle)
        },
        leadingContent = {
            Image(
                painter = painterResource(id = item.iconID),
                contentDescription = null,
                modifier = Modifier.width(36.dp),
                colorFilter = ColorFilter.tint(colorScheme.onSurface)
            )
        }
    )
}

@Composable
private fun buildItem(source: Source): SourceModel {
    return when (source) {
        Source.LOCAL ->
            SourceModel(
                title = stringResource(source.titleKey),
                subtitle = stringResource(R.string.add_account_local_subtitle),
                iconID = R.drawable.rss_logo
            )

        Source.FEEDBIN ->
            SourceModel(
                title = stringResource(source.titleKey),
                subtitle = stringResource(R.string.add_account_feedbin_subtitle),
                iconID = R.drawable.feedbin_logo
            )

        Source.FRESHRSS ->
            SourceModel(
                title = stringResource(source.titleKey),
                iconID = R.drawable.freshrss_logo,
                subtitle = stringResource(R.string.add_account_freshrss_subtitle),
            )

        Source.MINIFLUX, Source.MINIFLUX_TOKEN ->
            SourceModel(
                title = stringResource(source.titleKey),
                iconID = R.drawable.miniflux_logo,
                subtitle = stringResource(R.string.add_account_miniflux_subtitle)
            )

        Source.READER ->
            SourceModel(
                title = stringResource(source.titleKey),
                iconID = R.drawable.rss_logo,
                subtitle = stringResource(R.string.add_account_reader_subtitle),
            )
    }
}

private data class SourceModel(
    val title: String,
    val subtitle: String,
    @DrawableRes val iconID: Int,
)

@Preview
@Composable
private fun AccountRowPreview() {
    CapyTheme {
        Column {
            Source.entries.forEach {
                AccountRow(source = it)
            }
        }
    }
}

@Preview
@Composable
private fun AccountRowPreview_Dark() {
    PreviewKoinApplication {
        CapyTheme(themeMode = ThemeMode.DARK) {
            Column {
                Source.entries.forEach {
                    AccountRow(source = it)
                }
            }
        }
    }
}
