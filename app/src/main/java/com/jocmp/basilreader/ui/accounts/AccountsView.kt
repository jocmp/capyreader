package com.jocmp.basilreader.ui.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jocmp.basil.Account
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountsView(
    id: String = "",
    navController: NavController,
) {
    val viewModel = koinViewModel<AccountsViewModel>()

    Column {
        Button(onClick = { viewModel.createAccount() }) {
            Text("+")
        }
//        navController.navigate("accounts?id=${id}")
        LazyColumn {
            items(viewModel.accounts, key = { it.id }) { account ->
                Row {
                    Text(account.id)
                    Button(onClick = { viewModel.removeAccount(account) }) {
                        Text("x")
                    }
                }
            }
        }
    }
}
