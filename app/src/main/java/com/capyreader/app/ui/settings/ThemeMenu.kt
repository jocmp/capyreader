package com.capyreader.app.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.common.ThemeOption
import com.capyreader.app.refresher.RefreshInterval.MANUALLY_ONLY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeMenu(
    onUpdateTheme: (theme: ThemeOption) -> Unit,
    theme: ThemeOption,
) {
    val context = LocalContext.current
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val options = ThemeOption.sorted.map {
        it to context.translationKey(it)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { setExpanded(it) },
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = context.translationKey(theme),
            onValueChange = {},
            label = { Text(stringResource(R.string.theme_menu_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) }
        ) {
            options.forEach { (option, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onUpdateTheme(option)
                        setExpanded(false)
                    }
                )
            }
        }
    }
}

private fun Context.translationKey(option: ThemeOption): String {
    return when (option) {
        ThemeOption.LIGHT -> getString(R.string.theme_menu_option_light)
        ThemeOption.DARK -> getString(R.string.theme_menu_option_dark)
        ThemeOption.SYSTEM_DEFAULT -> getString(R.string.theme_menu_option_system_default)
    }
}

@Preview
@Composable
fun ThemeMenuPreview() {
    ThemeMenu(
        onUpdateTheme = {},
        theme = ThemeOption.SYSTEM_DEFAULT,
    )
}
