package com.capyreader.app.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.glance.LocalContext
import com.capyreader.app.common.openLink
import com.capyreader.app.preferences.AppPreferences
import org.koin.compose.koinInject

val LocalLinkOpener = compositionLocalOf { LinkOpener() }

/**
 * https://developer.android.com/develop/ui/compose/layouts/adaptive/support-different-display-sizes#explicit-layout-changes
 */
@Composable
fun provideLinkOpener(appPreferences: AppPreferences = koinInject()): LinkOpener {
    val openInternally by appPreferences.openLinksInternally.collectChangesWithDefault()
    val openExternalAdjacent = !isCompact()
    val context = LocalContext.current

    return remember(openInternally, openExternalAdjacent) {
        LinkOpener { url ->
            openLink(
                context,
                url = url,
                openExternalAdjacent = openExternalAdjacent,
                openInternally = openInternally,
            )
        }
    }
}

data class LinkOpener(
    val open: (url: Uri) -> Unit = {},
)
