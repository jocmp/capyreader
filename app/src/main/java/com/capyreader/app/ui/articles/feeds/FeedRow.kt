package com.capyreader.app.ui.articles.feeds

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.articles.CountBadge
import com.capyreader.app.ui.articles.FaviconBadge
import com.capyreader.app.ui.articles.ListTitle
import com.capyreader.app.ui.articles.list.FeedActionMenu
import com.capyreader.app.ui.fixtures.FeedSample
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed

@Composable
fun FeedRow(
    selected: Boolean,
    feed: Feed,
    onSelect: (feed: Feed) -> Unit,
    status: ArticleStatus = ArticleStatus.ALL,
) {
    val (showMenu, setShowMenu) = remember { mutableStateOf(false) }

   Box {
       DrawerItem(
           icon = {
               FaviconBadge(url = feed.faviconURL)
           },
           label = { ListTitle(feed.title) },
           badge = {
               CountBadge(count = feed.count, showBadge = feed.showUnreadBadge, status = status)
           },
           selected = selected,
           onClick = {
               onSelect(feed)
           },
           onLongClick = {
               setShowMenu(true)
           }
       )

       FeedActionMenu(
           expanded = showMenu,
           feed = feed,
           onDismissMenuRequest = {
               setShowMenu(false)
           }
       )
   }
}

@Preview
@Composable
fun FeedRowPreview() {
    val feed = FeedSample().values.take(1).first()

    MaterialTheme {
        FeedRow(
            feed = feed,
            onSelect = {},
            selected = false
        )
    }
}
