package com.jocmp.basilreader

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.jocmp.basil.AccountManager

@Composable
fun AccountsView() {
    val context = LocalContext.current

    LaunchedEffect(true) {
        AccountManager(context)
    }

    Text("Hello Moto")
}