package com.jocmp.capy.accounts.reader

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.BasicAuthInterceptor
import com.jocmp.capy.accounts.clientCertAlias
import com.jocmp.capy.accounts.httpClientBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.net.URI

internal object ReaderOkHttpClient {
    fun forAccount(path: URI, preferences: AccountPreferences, clientCertManager: ClientCertManager): OkHttpClient {
        return httpClientBuilder(cachePath = path)
            .addInterceptor(
                BasicAuthInterceptor {
                    runBlocking { "GoogleLogin auth=${preferences.password.get()}" }
                }
            )
            .clientCertAlias(clientCertManager, runBlocking { preferences.clientCertAlias.get() })
            .build()
    }
}
