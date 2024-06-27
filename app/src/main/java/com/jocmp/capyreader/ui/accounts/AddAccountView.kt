package com.jocmp.capyreader.ui.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.capy.accounts.Source
import com.jocmp.capyreader.R
import com.jocmp.capyreader.ui.LocalWindowWidth
import com.jocmp.capyreader.ui.components.widthMaxSingleColumn
import com.jocmp.capyreader.ui.isCompact
import com.jocmp.capyreader.ui.theme.CapyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountView(
    onSelectLocal: () -> Unit,
    onSelectFeedbin: () -> Unit,
) {
    Scaffold { padding ->
        Box(
            contentAlignment = contentAlignment(),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .widthMaxSingleColumn()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.add_account_title),
                    style = typography.headlineMedium,
                    modifier = Modifier
                        .padding(titlePadding())
                )
                Row(Modifier.clickable { onSelectFeedbin() }) {
                    AccountRow(source = Source.FEEDBIN)
                }
                Row(Modifier.clickable { onSelectLocal() }) {
                    AccountRow(source = Source.LOCAL)
                }
            }
        }
    }
}

@Composable
private fun contentAlignment(): Alignment {
    val width = LocalWindowWidth.current

    return if (width.isCompact) {
        Alignment.TopCenter
    } else {
        Alignment.Center
    }
}

@Composable
private fun titlePadding(): PaddingValues {
    val width = LocalWindowWidth.current

    return if (width.isCompact) {
        PaddingValues(top = 56.dp, start = 16.dp, end = 16.dp)
    } else {
        PaddingValues(start = 16.dp, end = 16.dp)
    }
}

@Preview
@Composable
private fun AddAccountViewPreview() {
    CapyTheme {
        AddAccountView(
            onSelectLocal = {},
            onSelectFeedbin = {}
        )
    }
}

@Preview(device = "id:pixel_fold")
@Composable
private fun AddAccountViewPreview_Tablet() {
    CompositionLocalProvider(LocalWindowWidth provides WindowWidthSizeClass.Medium) {
        CapyTheme {
            AddAccountView(
                onSelectLocal = {},
                onSelectFeedbin = {}
            )
        }
    }
}
