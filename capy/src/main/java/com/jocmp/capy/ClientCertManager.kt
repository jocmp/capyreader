package com.jocmp.capy

import android.app.Activity
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

interface ClientCertManager {

    fun chooseClientCert(activity: Activity, onAliasChosen: (String) -> Unit)

    data class ClientSSlSocketFactory(
        val sslSocketFactory: SSLSocketFactory,
        val trustManager: X509TrustManager,
    )

    fun buildSslSocketFactory(clientCertAlias: String) : ClientSSlSocketFactory
}