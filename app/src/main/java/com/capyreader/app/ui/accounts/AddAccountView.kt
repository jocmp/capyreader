package com.capyreader.app.ui.accounts

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
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.CrashReporting
import com.capyreader.app.ui.components.Spacing
import com.capyreader.app.ui.components.safeEdgePadding
import com.capyreader.app.ui.isAtMostMedium
import com.capyreader.app.ui.theme.CapyTheme
import com.capyreader.app.widthMaxSingleColumn
import com.jocmp.capy.accounts.Source
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@Composable
fun AddAccountView(
    onSelectLocal: () -> Unit,
    onSelectService: (source: Source) -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeEdgePadding(),
    ) { padding ->
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
                Row(Modifier.clickable { onSelectLocal() }) {
                    AccountRow(source = Source.LOCAL)
                }
                SyncServiceRow(
                    onSelectService,
                    source = Source.FEEDBIN
                )
                SyncServiceRow(
                    onSelectService,
                    source = Source.FRESHRSS
                )
                SyncServiceRow(
                    onSelectService,
                    source = Source.MINIFLUX
                )
                SyncServiceRow(
                    onSelectService,
                    source = Source.READER
                )
            }
            if (CrashReporting.isAvailable) {
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    MiniSettings()
                }
            }
        }
    }
}

@Composable
fun SyncServiceRow(
    onSelect: (source: Source) -> Unit,
    source: Source,
) {
    Row(Modifier.clickable { onSelect(source) }) {
        AccountRow(source = source)
    }
}

@Composable
private fun contentAlignment(): Alignment {
    return if (isAtMostMedium()) {
        Alignment.TopCenter
    } else {
        Alignment.Center
    }
}

@Composable
private fun titlePadding(): PaddingValues {
    return if (isAtMostMedium()) {
        PaddingValues(top = Spacing.topBarHeight, start = 16.dp, end = 16.dp)
    } else {
        PaddingValues(start = 16.dp, end = 16.dp)
    }
}

@Preview
@Composable
private fun AddAccountViewPreview() {
    val context = LocalContext.current

    KoinApplication(
        application = {
            androidContext(context)
            setupCommonModules()
        }
    ) {
        CapyTheme {
            AddAccountView(
                onSelectLocal = {},
                onSelectService = {}
            )
        }
    }
}

@Preview(device = "id:pixel_fold")
@Composable
private fun AddAccountViewPreview_Tablet() {
    val context = LocalContext.current

    KoinApplication(
        application = {
            androidContext(context)
            setupCommonModules()
        }
    ) {
        CapyTheme {
            AddAccountView(
                onSelectLocal = {},
                onSelectService = {}
            )
        }
    }
}
