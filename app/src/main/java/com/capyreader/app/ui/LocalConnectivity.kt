package com.capyreader.app.ui

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

val LocalConnectivity = compositionLocalOf { ConnectivityType.NONE }

enum class ConnectivityType {
    NONE,
    ETHERNET,
    WIFI,
    CELLULAR,
}

@Composable
fun rememberLocalConnectivity(): ConnectivityType {
    val context = LocalContext.current
    var connectivity by rememberSaveable { mutableStateOf(ConnectivityType.NONE) }

    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val listener = NetworkListener(connectivityManager) {
            connectivity = it
        }

        connectivityManager.registerDefaultNetworkCallback(listener)

        onDispose {
            connectivityManager.unregisterNetworkCallback(listener)
        }
    }

    return connectivity
}

class NetworkListener(
    private val connectivityManager: ConnectivityManager,
    private val onChange: (connectivity: ConnectivityType) -> Unit
) :
    ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network) {
        update(network)
    }

    override fun onLost(network: Network) {
        super.onLost(network)

        update(network)
    }

    private fun update(network: Network) {
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val connectivity = when {
            capabilities == null || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> ConnectivityType.NONE
            capabilities.hasTransport(TRANSPORT_WIFI) -> ConnectivityType.WIFI
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> ConnectivityType.ETHERNET
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> ConnectivityType.CELLULAR
            else -> ConnectivityType.NONE
        }

        onChange(connectivity)
    }
}
