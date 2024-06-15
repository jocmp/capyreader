package com.jocmp.capyreader.ui.articles.detail

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.capyreader.R

@Composable
fun CapyPlaceholder() {
    val tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Icon(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = null,
        tint = tint
    )
}

@Preview
@Composable
private fun CapyPlaceholderPreview() {
    MaterialTheme {
        Surface {
            CapyPlaceholder()
        }
    }
}
