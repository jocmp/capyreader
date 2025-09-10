package com.capyreader.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capyreader.app.preferences.ThemeMode
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun FormSection(
    modifier: Modifier = Modifier,
    labelStyle: LabelStyle = LabelStyle.LARGE,
    title: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (title != null) {
            Text(
                text = title,
                style = labelStyle.textStyle(),
                color = colorScheme.surfaceTint,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        content()
    }
}

enum class LabelStyle {
    LARGE,
    COMPACT;
}

@Composable
private fun LabelStyle.textStyle() = when (this) {
    LabelStyle.LARGE -> typography.labelLarge
    LabelStyle.COMPACT -> TextStyle(fontSize = 12.sp)
}

@Preview
@Composable
private fun FormSectionPreview() {
    CapyTheme(themeMode = ThemeMode.DARK) {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormSection(title = "My Title") {
                    Text("My content")
                }
                FormSection(
                    labelStyle = LabelStyle.COMPACT,
                    title = "My Small Title"
                ) {
                    Text("My content")
                }
            }
        }
    }
}
