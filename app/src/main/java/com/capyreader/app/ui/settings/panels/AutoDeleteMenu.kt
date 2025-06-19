package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.capyreader.app.ui.settings.PreferenceSelect
import com.jocmp.capy.accounts.AutoDelete

@Composable
fun AutoDeleteMenu(
    autoDelete: AutoDelete,
    updateAutoDelete: (interval: AutoDelete) -> Unit,
) {
    val options = AutoDelete.entries

    Column(
        modifier = Modifier.padding(bottom = 16.dp),
    ) {
        PreferenceSelect(
            selected = autoDelete,
            update = { updateAutoDelete(it) },
            options = options,
            label = R.string.settings_auto_delete_articles_title,
            optionText = { translationKey(it) }
        )
    }
}

@Composable
private fun translationKey(autoDelete: AutoDelete): String {
    val resource = when (autoDelete) {
        AutoDelete.DISABLED -> R.string.settings_auto_delete_option_keep_forever
        AutoDelete.WEEKLY -> R.string.settings_auto_delete_option_keep_for_one_week
        AutoDelete.EVERY_TWO_WEEKS -> R.string.settings_auto_delete_option_keep_for_two_week
        AutoDelete.EVERY_MONTH -> R.string.settings_auto_delete_option_keep_for_one_month
        AutoDelete.EVERY_THREE_MONTHS -> R.string.settings_auto_delete_option_keep_for_three_month
    }

    return stringResource(resource)
}

@Preview
@Composable
fun AutoDeletePreview() {
    PreviewKoinApplication {
        Surface {
            AutoDeleteMenu(
                autoDelete = AutoDelete.EVERY_THREE_MONTHS,
                updateAutoDelete = {}
            )
        }
    }
}
