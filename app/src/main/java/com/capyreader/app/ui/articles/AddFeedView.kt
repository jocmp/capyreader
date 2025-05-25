package com.capyreader.app.ui.articles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FeedOption

@Composable
fun AddFeedView(
    feedChoices: List<FeedOption>,
    onAddFeed: (url: String) -> Unit,
    onCancel: () -> Unit = {},
    loading: Boolean,
    error: AddFeedResult.Error?,
    condensed: Boolean = true,
    defaultQueryURL: String = "",
) {
    val (queryURL, setQueryURL) = rememberSaveable { mutableStateOf(defaultQueryURL) }
    val (selectedOption, selectOption) = remember { mutableStateOf<FeedOption?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val isError = error != null

    val addFeed = {
        focusManager.clearFocus()
        keyboard?.hide()

        if (selectedOption != null) {
            onAddFeed(selectedOption.feedURL)
        } else {
            onAddFeed(queryURL)
        }
    }

    Column(Modifier.padding(top = 16.dp)) {
        OutlinedTextField(
            value = queryURL,
            onValueChange = setQueryURL,
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null)
            },
            label = {
                Text(stringResource(id = R.string.add_feed_url_title))
            },
            isError = isError,
            supportingText = {
                error?.let {
                    val resource = when (it) {
                        is AddFeedResult.Error.FeedNotFound -> R.string.add_feed_feed_not_error
                        is AddFeedResult.Error.ConnectionError -> R.string.add_feed_network_error
                        is AddFeedResult.Error.NetworkError -> R.string.add_feed_network_error
                        is AddFeedResult.Error.SaveFailure -> R.string.add_feed_save_error
                    }

                    Text(stringResource(resource))
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { addFeed() }
            ),
            trailingIcon = {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )
        ChoicesRowVisibility(condensed = condensed, visible = feedChoices.isNotEmpty()) {
            Row(
                Modifier
                    .heightIn(max = 300.dp)
            ) {
                FeedOptions(
                    options = feedChoices,
                    selectedOption = selectedOption,
                    onOptionSelect = selectOption
                )
            }
        }
        if (condensed) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
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
                    enabled = !loading,
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text(stringResource(R.string.add_feed_submit))
                }
            }
        } else {
            Button(
                onClick = { addFeed() },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(stringResource(R.string.add_feed_submit))
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
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .selectableGroup()
    ) {
        options.forEach { option ->
            val selected = option == selectedOption
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = selected,
                        onClick = {
                            onOptionSelect(option)
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected, onClick = null)
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = option.title,
                        style = typography.bodyLarge,
                        maxLines = 1
                    )
                    Text(
                        text = option.feedURL,
                        style = typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoicesRowVisibility(
    condensed: Boolean,
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    if (!condensed) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(tween()),
        ) {
            content()
        }
    } else if (visible) {
        content()
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
        onCancel = {},
        loading = false,
        error = null,
    )
}

@Preview
@Composable
fun AddFeedViewPreview_FullScreen() {
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
        onCancel = {},
        loading = false,
        error = null,
        condensed = false
    )
}
