package com.jocmp.basilreader.ui.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.jocmp.basil.AccountManager
import com.jocmp.basil.AccountManager.AccountSummary
import com.jocmp.basilreader.putAccountID
import com.jocmp.basilreader.settings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AccountIndexScreen(
    accountManager: AccountManager = koinInject(),
    onSelect: () -> Unit
) {
    val context = LocalContext.current
    val composableScope = rememberCoroutineScope()
    val accountsState = remember { mutableStateListOf<AccountSummary>() }

    LaunchedEffect(Unit) {
        composableScope.launch {
            accountsState.addAll(accountManager.latestSummaries())
        }
    }

    val accounts = accountsState.toList()

    Column {
        Button(onClick = { accountsState.add(accountManager.createAccount()) }) {
            Text("+")
        }
        LazyColumn {
            items(accounts, key = { it.id }) { account ->
                Row(modifier = Modifier.clickable {
                    composableScope.launch {
                        context.settings.putAccountID(account.id)
                        onSelect()
                    }
                }) {
                    Text(account.displayName)
                    Button(onClick = { accountManager.removeAccount(account.id) }) {
                        Text("x")
                    }
                }
            }
        }
    }
}
