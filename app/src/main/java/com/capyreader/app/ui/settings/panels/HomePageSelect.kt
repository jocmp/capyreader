package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.preferences.HomePage
import com.capyreader.app.ui.settings.PreferenceSelect

@Composable
fun HomePageSelect(
    selected: HomePage,
    update: (HomePage) -> Unit,
    readLaterFeedID: String? = null,
) {
    val options = remember(readLaterFeedID) {
        buildList {
            add(HomePage.Today)
            add(HomePage.Unread)
            add(HomePage.Starred)
            if (readLaterFeedID != null) {
                add(HomePage.ReadLater(feedID = readLaterFeedID))
            }
        }
    }

    PreferenceSelect(
        selected = selected,
        update = update,
        options = options,
        optionText = { homePageLabel(it) },
        label = R.string.settings_home_page,
    )
}

@Composable
private fun homePageLabel(homePage: HomePage): String {
    return when (homePage) {
        is HomePage.Today -> stringResource(R.string.filter_today)
        is HomePage.Unread -> stringResource(R.string.filter_unread)
        is HomePage.Starred -> stringResource(R.string.filter_starred)
        is HomePage.ReadLater -> stringResource(R.string.filter_read_later)
    }
}
