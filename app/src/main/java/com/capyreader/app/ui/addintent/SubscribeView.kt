package com.capyreader.app.ui.addintent

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.translationKey
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FeedOption

@Composable
fun SubscribeView(
    feedChoices: List<FeedOption>,
    onSubscribe: (url: String) -> Unit,
    loading: Boolean,
    error: AddFeedResult.Error?,
) {
    val (userSelection, selectOption) = remember { mutableStateOf<FeedOption?>(null) }
    val selectedOption = when {
        feedChoices.size == 1 -> feedChoices.first()
        else -> userSelection
    }

    Column(Modifier.padding(top = 16.dp)) {
        error?.let {
            Text(
                text = stringResource(it.translationKey),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        AnimatedVisibility(
            visible = feedChoices.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(tween()),
        ) {
            Row(
                Modifier.heightIn(max = 300.dp)
            ) {
                FeedChoices(
                    options = feedChoices,
                    selectedOption = selectedOption,
                    onOptionSelect = selectOption,
                )
            }
        }

        Button(
            onClick = {
                val url = selectedOption?.feedURL ?: return@Button
                onSubscribe(url)
            },
            enabled = !loading && selectedOption != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.add_link_subscribe))
                }
            }
        }
    }
}

@Composable
private fun FeedChoices(
    options: List<FeedOption>,
    selectedOption: FeedOption?,
    onOptionSelect: (option: FeedOption) -> Unit,
) {
    val singleOption = options.size == 1

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .then(if (singleOption) Modifier else Modifier.selectableGroup())
    ) {
        options.forEach { option ->
            val selected = option == selectedOption
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 56.dp)
                    .then(
                        if (singleOption) {
                            Modifier
                        } else {
                            Modifier.selectable(
                                selected = selected,
                                onClick = { onOptionSelect(option) },
                                role = Role.RadioButton
                            )
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (!singleOption) {
                        RadioButton(selected = selected, onClick = null)
                    }
                    Column(modifier = Modifier.padding(start = if (singleOption) 0.dp else 16.dp)) {
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = option.feedURL,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SubscribeViewPreview() {
    SubscribeView(
        feedChoices = listOf(
            FeedOption(feedURL = "https://example.com/feed", title = "Example Feed"),
        ),
        onSubscribe = {},
        loading = false,
        error = null,
    )
}

@Preview
@Composable
private fun SubscribeViewMultiplePreview() {
    SubscribeView(
        feedChoices = listOf(
            FeedOption(feedURL = "https://example.com/feed", title = "Example Feed"),
            FeedOption(feedURL = "https://example.com/comments", title = "Example Comments"),
        ),
        onSubscribe = {},
        loading = false,
        error = null,
    )
}
