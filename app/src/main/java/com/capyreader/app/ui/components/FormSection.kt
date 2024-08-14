package com.capyreader.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun FormSection(
    title: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (title != null) {
            Text(
                text = title,
                style = typography.labelMedium,
                color = colorScheme.surfaceTint,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        content()
    }
}

@Preview
@Composable
private fun FormSectionPreview() {
    CapyTheme {
        FormSection(title = "My Title") {
            Text("My content")
        }
    }
}
