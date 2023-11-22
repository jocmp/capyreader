package com.jocmp.basilreader.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jocmp.basilreader.ui.accounts.AccountsView
import com.jocmp.basilreader.ui.theme.BasilReaderTheme

@Composable
fun App() {
    val navController = rememberNavController()

    BasilReaderTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = "accounts"
            ) {
                composable("accounts?id={id}") { backStackEntry ->
                    AccountsView(
                        id = backStackEntry.arguments?.getString("id") ?: "",
                        navController,
                    )
                }
            }
        }
    }
}
