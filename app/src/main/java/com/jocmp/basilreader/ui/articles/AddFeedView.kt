package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.accounts.FeedOption
import com.jocmp.basilreader.R

@Composable
fun AddFeedView(
    feedChoices: List<FeedOption>,
    onAddFeed: (url: String) -> Unit,
    onCancel: () -> Unit,
) {
    val (queryURL, setQueryURL) = remember { mutableStateOf("") }
    val (selectedOption, selectOption) = remember { mutableStateOf(feedChoices.firstOrNull()) }

    val addFeed = {
        if (selectedOption != null) {
            onAddFeed(selectedOption.feedURL)
        } else {
            onAddFeed(queryURL)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            TextField(
                value = queryURL,
                onValueChange = setQueryURL,
                label = {
                    Text(stringResource(id = R.string.add_feed_url_title))
                },
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
            )
            if (feedChoices.isNotEmpty()) {
                FeedOptions(
                    options = feedChoices,
                    selectedOption = selectedOption,
                    onOptionSelect = selectOption
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = { onCancel() },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(stringResource(R.string.feed_form_cancel))
                }
                TextButton(
                    onClick = { addFeed() },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(stringResource(R.string.add_feed_submit))
                }
            }
        }
    }
}

@Composable
fun FeedOptions(
    options: List<FeedOption>,
    selectedOption: FeedOption?,
    onOptionSelect: (option: FeedOption) -> Unit,
) {
    Column(Modifier.selectableGroup()) {
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = option == selectedOption,
                        onClick = {
                            onOptionSelect(option)
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = false, onClick = null)
                Text(
                    text = option.title,
                    style = typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun AddFeedViewPreview() {
    AddFeedView(
        feedChoices = listOf(
            FeedOption(
                feedURL = "9to5google.com/feed/index",
                title = "The Verge - All Feeds"
            ),
            FeedOption(
                feedURL = "9to5google.com/feed/comments",
                title = "9to5Google - Comments"
            ),
        ),
        onAddFeed = {},
        onCancel = {}
    )
}
