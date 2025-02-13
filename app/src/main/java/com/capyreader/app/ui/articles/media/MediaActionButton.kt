package com.capyreader.app.ui.articles.media

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun MediaActionButton(
    onClick: () -> Unit,
    @StringRes text: Int,
    icon: ImageVector,
) {
    OutlinedButton(
        border = borderStroke(),
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors().copy(
            containerColor = MediaColors.buttonContainerColor,
            contentColor = MediaColors.buttonOutlineColor,
        )
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(stringResource(text))
    }
}

@Composable
private fun borderStroke(): BorderStroke {

    return BorderStroke(
        width = 1.dp,
        color = MediaColors.buttonContentColor
    )
}
