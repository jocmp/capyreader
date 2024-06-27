package com.jocmp.capyreader.ui.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.capy.accounts.Source

@Composable
fun AddAccountView(
    onSelectLocal: () -> Unit,
    onSelectFeedbin: () -> Unit,
) {
    Column(
        Modifier.verticalScroll(rememberScrollState())
    ) {
        SourceRow(
            source = Source.LOCAL,
            onClick = onSelectLocal
        )
        SourceRow(
            source = Source.FEEDBIN,
            onClick = onSelectFeedbin
        )
    }
}

@Composable
private fun SourceRow(source: Source, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                onClick()
            }
    ) {
        Text(text = source.value.capitalize())
    }
}


@Preview
@Composable
private fun AddAccountViewPreview() {
    AddAccountView(
        onSelectLocal = {},
        onSelectFeedbin = {}
    )
}
