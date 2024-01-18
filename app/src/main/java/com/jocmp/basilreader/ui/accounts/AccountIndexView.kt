package com.jocmp.basilreader.ui.accounts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.jocmp.basilreader.R
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountIndexScreen(
    viewModel: AccountIndexViewModel = koinInject(),
    onSelect: () -> Unit
) {
    val context = LocalContext.current
    val composableScope = rememberCoroutineScope()
    val accounts = viewModel.accounts
    val haptics = LocalHapticFeedback.current

    Column {
        Button(onClick = { viewModel.createAccount() }) {
            Text("+")
        }
        LazyColumn {
            items(accounts, key = { it.id }) { account ->
                Row(modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            composableScope.launch {
                                viewModel.selectAccount(account.id)
                                onSelect()
                            }
                        },
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onLongClickLabel = stringResource(R.string.account_index_settings_options)
                    )
                ) {
                    Text(account.displayName)
                }
            }
        }
    }
}
