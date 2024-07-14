package com.capyreader.app.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
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
import com.capyreader.app.common.ImagePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewMenu(
    onUpdateImagePreview: (preview: ImagePreview) -> Unit,
    imagePreview: ImagePreview,
) {
    val context = LocalContext.current
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val options = ImagePreview.sorted.map {
        it to context.translationKey(it)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { setExpanded(it) },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = context.translationKey(imagePreview),
            onValueChange = {},
            label = { Text(stringResource(R.string.image_preview_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) }
        ) {
            options.forEach { (option, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onUpdateImagePreview(option)
                        setExpanded(false)
                    }
                )
                if (option == ImagePreview.NONE) {
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun Context.translationKey(option: ImagePreview): String {
    return when (option) {
        ImagePreview.NONE -> getString(R.string.image_preview_menu_option_none)
        ImagePreview.SMALL -> getString(R.string.image_preview_menu_option_small)
        ImagePreview.LARGE -> getString(R.string.image_preview_menu_option_large)
    }
}

@Preview
@Composable
fun ImagePreviewMenuPreview() {
    val (preview, setPreview) = remember {
        mutableStateOf(ImagePreview.SMALL)
    }

    ImagePreviewMenu(
        onUpdateImagePreview = setPreview,
        imagePreview = preview
    )
}
