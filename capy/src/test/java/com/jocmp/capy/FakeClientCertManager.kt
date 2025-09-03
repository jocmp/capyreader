package com.jocmp.capy

import android.app.Activity
import okhttp3.internal.platform.Platform
import javax.net.ssl.SSLContext

object FakeClientCertManager : ClientCertManager {
    override fun chooseClientCert(activity: Activity, onAliasChosen: (String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun buildSslSocketFactory(clientCertAlias: String): ClientCertManager.ClientSSlSocketFactory {
        val sslContext = SSLContext.getInstance("TLS")
        val trustManager = Platform.get().platformTrustManager()
        sslContext.init(emptyArray(), arrayOf(trustManager), null)
        return ClientCertManager.ClientSSlSocketFactory(
            sslContext.socketFactory,
            trustManager,
        )
    }
}
