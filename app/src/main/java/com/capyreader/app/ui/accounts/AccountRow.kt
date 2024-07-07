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
import com.jocmp.capy.accounts.Source
import com.capyreader.app.R
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun AccountRow(
    source: Source
) {
    val item = buildItem(source = source)

    ListItem(
        headlineContent = {
            Text(text = item.title)
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
                title = stringResource(R.string.account_source_local),
                iconID = R.drawable.rss_logo
            )

        Source.FEEDBIN ->
            SourceModel(
                title = stringResource(R.string.account_source_feedbin),
                iconID = R.drawable.feedbin_logo
            )
    }
}

private data class SourceModel(
    val title: String,
    @DrawableRes val iconID: Int,
)

@Preview
@Composable
private fun FeedbinAccountRow() {
    CapyTheme {
        Column {
            AccountRow(source = Source.FEEDBIN)
            AccountRow(source = Source.LOCAL)
        }
    }
}
