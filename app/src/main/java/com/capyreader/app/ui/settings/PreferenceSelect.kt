package com.capyreader.app.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.capyreader.app.R
import com.capyreader.app.ui.components.DialogCard
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun <T> PreferenceSelect(
    selected: T,
    update: (T) -> Unit,
    options: List<T>,
    disabledOption: T? = null,
    optionText: @Composable (T) -> String,
    @StringRes label: Int,
) {
    val (isOpen, setOpen) = remember { mutableStateOf(false) }

    val dismiss = { setOpen(false) }

    Box(
        Modifier.clickable {
            setOpen(true)
        }
    ) {
        ListItem(
            headlineContent = { Text(stringResource(label)) },
            supportingContent = { Text(optionText(selected)) }
        )
    }

    if (isOpen) {
        Dialog(onDismissRequest = dismiss) {
            DialogCard {
                Column(
                    Modifier.verticalScroll(rememberScrollState())
                ) {
                    options.forEach { option ->
                        val isSelected = selected == option

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        update(option)
                                        dismiss()
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = null)
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(
                                    text = optionText(option),
                                    style = typography.bodyLarge,
                                    maxLines = 1
                                )
                            }
                        }

                        if (option == disabledOption) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreferenceSelectPreview() {
    val options = listOf("Newest First", "Oldest First")

    var selected by remember { mutableStateOf(options[0]) }

    CapyTheme {
        Box(Modifier.fillMaxSize()) {
            PreferenceSelect(
                selected = selected,
                update = { selected = it },
                options = options,
                optionText = { it },
                label = R.string.article_list_unread_sort_title
            )
        }
    }
}
