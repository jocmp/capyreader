package com.jocmp.basilreader.ui.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basil.AccountManager.AccountSummary
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AccountIndexView(
    accountManager: AccountManager = koinInject(),
    onNavigate: (accountID: String) -> Unit
) {
    val composableScope = rememberCoroutineScope()
    val accountsState = remember { mutableStateListOf<AccountSummary>() }.also {
        composableScope.launch {
            it.addAll(accountManager.latestSummaries())
        }
    }

    val accounts = accountsState.toList()

    Column {
        Button(onClick = { accountManager.createAccount() }) {
            Text("+")
        }
        LazyColumn {
            items(accounts, key = { it.id }) { account ->
                Row(modifier = Modifier.clickable { onNavigate(account.id) }) {
                    Text(account.displayName)
                    Button(onClick = { accountManager.removeAccount(account.id) }) {
                        Text("x")
                    }
                }
            }
        }
    }
}
