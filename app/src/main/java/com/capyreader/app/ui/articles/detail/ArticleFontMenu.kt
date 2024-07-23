package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import com.jocmp.capy.articles.FontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleFontMenu(
    updateFontFamily: (fontFamily: FontFamily) -> Unit,
    fontFamily: FontFamily,
) {
    val context = LocalContext.current
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val options = FontFamily.sorted.map {
        it to it.slug // context.translationKey(it)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { setExpanded(it) },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            value = fontFamily.slug, // context.translationKey(theme),
            onValueChange = {},
            label = { Text("Font") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) }
        ) {
            options.forEach { (option, text) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = text,
                            fontFamily = findFont(option),
                            fontWeight = FontWeight.Normal
                        )
                    },
                    onClick = {
                        updateFontFamily(option)
                        setExpanded(false)
                    }
                )
            }
        }
    }
}

private fun findFont(fontFamily: FontFamily) = when (fontFamily) {
    FontFamily.SYSTEM_DEFAULT -> null
    FontFamily.POPPINS -> Font(resId = com.jocmp.capy.R.font.poppins)
    FontFamily.ATKINSON_HYPERLEGIBLE -> Font(resId = com.jocmp.capy.R.font.atkinson_hyperlegible)
    FontFamily.VOLLKORN -> Font(resId = com.jocmp.capy.R.font.vollkorn)
}?.toFontFamily()
