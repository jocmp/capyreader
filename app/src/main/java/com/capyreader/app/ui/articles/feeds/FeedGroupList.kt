package com.capyreader.app.ui.articles.feeds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.FeedGroup
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.articles.ListHeadline
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.common.launchIO
import org.koin.compose.koinInject

@Composable
fun FeedGroupList(
    type: FeedGroup,
    appPreferences: AppPreferences = koinInject(),
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val pinPreference = appPreferences.pinFeedGroup(type)
    val expanded by pinPreference.collectChangesWithCurrent()

    val toggle = {
        scope.launchIO {
            pinPreference.set(!expanded)
        }
    }

    Column(
        Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .clickable(
                        onClick = { toggle() },
                        role = Role.Button,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    )
            ) {
                ListHeadline(text = stringResource(type.translationKey))
            }
            IconDropdown(expanded, onClick = { toggle() })
            Spacer(Modifier.width(16.dp))
        }

        AnimatedVisibility(
            expanded,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(animationSpec = tween()),
        ) {
            Column {
                content()
            }
        }
    }
}

private val FeedGroup.translationKey: Int
    get() = when (this) {
        FeedGroup.FEEDS -> R.string.nav_headline_feeds
        FeedGroup.FOLDERS -> R.string.nav_headline_tags
        FeedGroup.SAVED_SEARCHES -> R.string.nav_headline_saved_searches
    }

@Preview
@Composable
fun FeedGroupPreview() {
    PreviewKoinApplication {
        CapyTheme {
            Box(Modifier.width(300.dp)) {
                FeedGroupList(
                    type = FeedGroup.FEEDS,
                ) {
                    Text("One")
                    Text("Two")
                    Text("Three")
                }
            }
        }
    }
}
