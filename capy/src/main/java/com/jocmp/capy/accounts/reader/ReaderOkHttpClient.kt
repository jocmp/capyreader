package com.jocmp.capy.accounts.reader

import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.BasicAuthInterceptor
import com.jocmp.capy.accounts.httpClientBuilder
import okhttp3.OkHttpClient
import java.net.URI

internal object ReaderOkHttpClient {
    fun forAccount(path: URI, password: String, clientCertAlias: String, clientCertManager: ClientCertManager): OkHttpClient {
        return httpClientBuilder(cachePath = path)
            .addInterceptor(
                BasicAuthInterceptor {
                    "GoogleLogin auth=${password}"
                }
            )
            .clientCertAlias(clientCertManager, clientCertAlias)
            .build()
    }

    fun OkHttpClient.Builder.clientCertAlias(manager: ClientCertManager, clientCertAlias: String): OkHttpClient.Builder {
        if (clientCertAlias.isNotEmpty()) {
            val clientSSlSocketFactory = manager.buildSslSocketFactory(clientCertAlias)
            sslSocketFactory(clientSSlSocketFactory.sslSocketFactory, clientSSlSocketFactory.trustManager)
        }
        return this
    }
}
