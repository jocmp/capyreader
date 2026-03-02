package com.jocmp.capy.accounts.reader

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.BasicAuthInterceptor
import com.jocmp.capy.accounts.clientCertAlias
import com.jocmp.capy.accounts.httpClientBuilder
import okhttp3.OkHttpClient
import java.net.URI

internal object ReaderOkHttpClient {
    suspend fun forAccount(path: URI, preferences: AccountPreferences, clientCertManager: ClientCertManager): OkHttpClient {
        val secret = preferences.password.get()
        val clientCertAliasValue = preferences.clientCertAlias.get()

        return httpClientBuilder(cachePath = path)
            .addInterceptor(
                BasicAuthInterceptor {
                    "GoogleLogin auth=${secret}"
                }
            )
            .clientCertAlias(clientCertManager, clientCertAliasValue)
            .build()
    }
}
