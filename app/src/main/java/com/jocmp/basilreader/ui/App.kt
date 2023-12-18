package com.jocmp.basilreader.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.selectAccount
import com.jocmp.basilreader.selectedAccount
import com.jocmp.basilreader.settings
import com.jocmp.basilreader.ui.accounts.accountIndex
import com.jocmp.basilreader.ui.articles.articleGraph
import com.jocmp.basilreader.ui.theme.BasilReaderTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

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
                startDestination = "articles"
            ) {
                accountIndex(
                    onNavigate = {}
                )
                articleGraph(navController = navController)
            }
        }
    }
}
data class AccountState(val id: String)

val LocalAccount = compositionLocalOf<Account?> { null }
