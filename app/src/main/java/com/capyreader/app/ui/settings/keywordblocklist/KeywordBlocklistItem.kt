package com.capyreader.app.ui.settings.keywordblocklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.capyreader.app.R
import com.capyreader.app.ui.components.DialogCard

@Composable
fun KeywordBlocklistItem() {
    val (isOpen, setOpen) = remember { mutableStateOf(false) }

    val dismiss = { setOpen(false) }

    Box(
        Modifier.clickable {
            setOpen(true)
        }
    ) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.blocked_keywords)) },
            supportingContent = { Text(stringResource(R.string.blocked_keywords_supporting_text)) }
        )
    }

    if (isOpen) {
        val keywords = LocalBlockedKeywords.current

        Dialog(onDismissRequest = dismiss) {
            DialogCard {
                KeywordBlocklistView(
                    keywords = keywords.keywords,
                    onAdd = {
                        keywords.add(it)
                    },
                    onRemove = {
                        keywords.remove(it)
                    }
                )
            }
        }
    }
}
