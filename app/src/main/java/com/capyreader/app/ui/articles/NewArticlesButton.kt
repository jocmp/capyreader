package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.capyreader.app.R
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun NewArticlesButton(
    onClick: () -> Unit,
) {
    val height = ButtonDefaults.ExtraSmallContainerHeight

    ElevatedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = height),
        shape = ButtonDefaults.shapesFor(height).shape,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        contentPadding = ButtonDefaults.contentPaddingFor(height, hasStartIcon = true),
    ) {
        Icon(
            imageVector = Icons.Rounded.ArrowUpward,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.iconSizeFor(height)),
        )
        Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(height)))
        Text(
            text = stringResource(R.string.article_list_new_articles_pill),
            style = ButtonDefaults.textStyleFor(height),
        )
    }
}

@PreviewLightDark
@Composable
private fun NewArticlesButtonPreview() {
    CapyTheme {
        NewArticlesButton(onClick = {})
    }
}
