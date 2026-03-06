package com.jocmp.capy.accounts.miniflux

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.BasicAuthInterceptor
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.clientCertAlias
import com.jocmp.capy.accounts.httpClientBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.net.URI

internal object MinifluxOkHttpClient {
    fun forAccount(path: URI, preferences: AccountPreferences, source: Source, clientCertManager: ClientCertManager): OkHttpClient {
        return httpClientBuilder(cachePath = path)
            .addInterceptor(authInterceptor(source, preferences))
            .clientCertAlias(clientCertManager, runBlocking { preferences.clientCertAlias.get() })
            .build()
    }

    private fun authInterceptor(source: Source, preferences: AccountPreferences): Interceptor {
        return if (source == Source.MINIFLUX_TOKEN) {
            Interceptor { chain ->
                val password = runBlocking { preferences.password.get() }
                val request = chain.request().newBuilder()
                    .header("X-Auth-Token", password)
                    .build()
                chain.proceed(request)
            }
        } else {
            BasicAuthInterceptor {
                runBlocking {
                    Credentials.basic(
                        preferences.username.get(),
                        preferences.password.get(),
                    )
                }
            }
        }
    }
}
