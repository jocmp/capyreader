package com.jocmp.basilreader.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.ui.accounts.accountIndex
import com.jocmp.basilreader.ui.articles.articleGraph
import com.jocmp.basilreader.ui.articles.navigateToArticles
import com.jocmp.basilreader.ui.theme.BasilReaderTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val navController = rememberNavController()

    BasilReaderTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val defaultAccountID = koinInject<AccountManager>().firstAccountID() ?: ""

            NavHost(
                navController = navController,
                startDestination = startDestination(defaultAccountID)
            ) {
                accountIndex(
                    onNavigate = { account ->
                        navController.navigateToArticles(account.id)
                    }
                )
                articleGraph(
                    navController = navController,
                    defaultAccountID = defaultAccountID
                )
            }
        }
    }
}

fun startDestination(defaultAccountID: String?): String {
    if (defaultAccountID.isNullOrEmpty()) {
        return "accounts"
    }
    return "articles?account_id=${defaultAccountID}"
}
