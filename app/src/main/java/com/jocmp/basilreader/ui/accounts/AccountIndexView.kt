package com.jocmp.basilreader.ui.accounts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.jocmp.basilreader.R
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountIndexScreen(
    viewModel: AccountIndexViewModel = koinInject(),
    onSelect: () -> Unit,
    onSettingsSelect: (accountID: String) -> Unit
) {
    val composableScope = rememberCoroutineScope()
    val accounts = viewModel.accounts
    val haptics = LocalHapticFeedback.current

    val (menuAccountID, setMenuAccountID) = remember { mutableStateOf<String>("") }

    val resetMenu = {
        setMenuAccountID("")
    }

    Column {
        Button(onClick = { viewModel.createAccount() }) {
            Text("+")
        }
        LazyColumn {
            items(accounts, key = { it.id }) { account ->
                Row(
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {
                                composableScope.launch {
                                    viewModel.selectAccount(account.id)
                                    onSelect()
                                }
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                setMenuAccountID(account.id)
                            },
                            onLongClickLabel = stringResource(R.string.account_index_settings_options)
                        )
                ) {
                    Text(account.displayName.get())
                    DropdownMenu(
                        expanded = menuAccountID == account.id,
                        onDismissRequest = { resetMenu() }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.account_index_menu_settings))
                            },
                            onClick = {
                                resetMenu()
                                onSettingsSelect(account.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
