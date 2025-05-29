package com.capyreader.app.ui.settings.keywordblocklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun KeywordBlocklistView(
    onAdd: (keyword: String) -> Unit,
    onRemove: (keyword: String) -> Unit,
    keywords: List<String>,
) {
    val containerColor = CardDefaults.cardColors().containerColor

    val (text, updateText) = remember { mutableStateOf("") }

    val addKeyword = {
        if (text.isNotBlank()) {
            onAdd(text)
            updateText("")
        }
    }

    Column {
        Text(
            stringResource(R.string.blocked_keywords),
            style = typography.headlineSmall,
            modifier = Modifier
                .padding(top = 24.dp, bottom = 8.dp)
                .padding(horizontal = 16.dp)
        )
        Column(
            Modifier
                .background(containerColor)
                .heightIn(max = 450.dp)
                .imePadding()
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = 200.dp)
                    .weight(0.1f)
            ) {
                keywords.forEach { keyword ->
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = containerColor),
                        headlineContent = { Text(keyword) },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    onRemove(keyword)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.blocked_keywords_remove_keyword)
                                )
                            }
                        }
                    )
                }
            }
            HorizontalDivider()
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    top = 16.dp,
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 8.dp
                )
            ) {
                OutlinedTextField(
                    value = text,
                    singleLine = true,
                    onValueChange = updateText,
                    placeholder = { Text(stringResource(R.string.blocked_keywords_add_keyword)) },
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Default,
                    ),
                    keyboardActions = KeyboardActions(
                        onAny = {
                            addKeyword()
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                IconButton(
                    onClick = {
                        addKeyword()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.blocked_keywords_add_keyword)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun KeywordBlocklistViewPreview() {
    val keywords = remember { mutableSetOf("Advertisement", "Sponsored Post") }

    CapyTheme {
        KeywordBlocklistView(
            onAdd = {
                keywords.add(it)
            },
            onRemove = {
                keywords.remove(it)
            },
            keywords = keywords.toList()
        )
    }
}
