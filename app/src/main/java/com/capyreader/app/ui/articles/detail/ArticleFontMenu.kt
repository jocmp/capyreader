package com.capyreader.app.ui.articles.detail

import android.content.Context
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.sp
import com.capyreader.app.R
import com.jocmp.capy.articles.FontOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleFontMenu(
    updateFontFamily: (fontOption: FontOption) -> Unit,
    fontOption: FontOption,
) {
    val context = LocalContext.current
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val options = FontOption.sorted.map {
        it to  context.translationKey(it)
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
            value = context.translationKey(fontOption),
            onValueChange = {},
            label = { Text(stringResource(R.string.article_font_menu_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
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
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
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

private fun Context.translationKey(option: FontOption): String {
    return when (option) {
        FontOption.SYSTEM_DEFAULT -> getString(R.string.font_option_system_default)
        FontOption.POPPINS -> getString(R.string.font_option_poppins)
        FontOption.ATKINSON_HYPERLEGIBLE -> getString(R.string.font_option_atkinson_hyperlegible)
        FontOption.VOLLKORN -> getString(R.string.font_option_vollkorn)
    }
}

private fun findFont(fontOption: FontOption) = when (fontOption) {
    FontOption.SYSTEM_DEFAULT -> null
    FontOption.POPPINS -> Font(resId = com.jocmp.capy.R.font.poppins)
    FontOption.ATKINSON_HYPERLEGIBLE -> Font(resId = com.jocmp.capy.R.font.atkinson_hyperlegible)
    FontOption.VOLLKORN -> Font(resId = com.jocmp.capy.R.font.vollkorn)
}?.toFontFamily()
