package com.jocmp.basilreader.ui.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jocmp.basil.Account
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountIndexView(
    viewModel: AccountIndexViewModel = koinViewModel(),
    onNavigate: (account: Account) -> Unit
) {
    Column {
        Button(onClick = { viewModel.createAccount() }) {
            Text("+")
        }
        LazyColumn {
            items(viewModel.accounts, key = { it.id }) { account ->
                Row(modifier = Modifier.clickable { onNavigate(account) }) {
                    Text(account.id)
                    Button(onClick = { viewModel.removeAccount(account) }) {
                        Text("x")
                    }
                }
            }
        }
    }

}
