package com.jocmp.basilreader.ui.folders

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.jocmp.basilreader.Route
import com.jocmp.feedbinclient.Feed
import com.jocmp.feedbinclient.Section

@Composable
fun FoldersList(navController: NavController, sections: List<Section>) {
    val onSelect = { feed: Feed ->
        navController.navigate("feed/${feed.id}")
    }

    LazyColumn {
        items(sections) { section ->
            when (section) {
                is Section.FolderSection ->
                    Column {
                        Text(text = section.folder.name)
                        section.folder.feeds.forEach {
                            FeedLink(feed = it, onSelect)
                        }
                    }

                is Section.FeedSection ->
                    Column {
                        Text("Feeds")
                        section.feeds.forEach { feed ->
                            FeedLink(feed = feed, onSelect)
                        }
                    }
            }
        }
    }
}

@Composable
fun FeedLink(feed: Feed, onSelect: (feed: Feed) -> Unit) {
    Button(onClick = { onSelect(feed) }) {
        Text(feed.title)
    }
}
