package com.capyreader.app.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.ThemeOption
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.accounts.AutoDelete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoDeleteMenu(
    autoDelete: AutoDelete,
    updateAutoDelete: (interval: AutoDelete) -> Unit,
) {
    val context = LocalContext.current
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val options = AutoDelete.entries.map {
        it to context.translationKey(it)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { setExpanded(it) },
        ) {
            TextField(
                modifier = Modifier
                    .menuAnchor(PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                value = context.translationKey(autoDelete),
                onValueChange = {},
                label = { Text(stringResource(R.string.settings_auto_delete_articles_title)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { setExpanded(false) }
            ) {
                options.forEach { (interval, text) ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = {
                            updateAutoDelete(interval)
                            setExpanded(false)
                        }
                    )
                    if (interval == AutoDelete.DISABLED) {
                        HorizontalDivider()
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.settings_auto_delete_disclaimer),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun Context.translationKey(autoDelete: AutoDelete): String {
    return when (autoDelete) {
        AutoDelete.DISABLED -> getString(R.string.settings_auto_delete_option_keep_forever)
        AutoDelete.WEEKLY -> getString(R.string.settings_auto_delete_option_keep_for_one_week)
        AutoDelete.EVERY_TWO_WEEKS -> getString(R.string.settings_auto_delete_option_keep_for_two_week)
        AutoDelete.EVERY_MONTH -> getString(R.string.settings_auto_delete_option_keep_for_one_month)
        AutoDelete.EVERY_THREE_MONTHS -> getString(R.string.settings_auto_delete_option_keep_for_three_month)
    }
}

@Preview
@Composable
fun AutoDeletePreview() {
    CapyTheme(theme = ThemeOption.DARK) {
        Surface {
            AutoDeleteMenu(
                autoDelete = AutoDelete.EVERY_THREE_MONTHS,
                updateAutoDelete = {}
            )
        }
    }
}
