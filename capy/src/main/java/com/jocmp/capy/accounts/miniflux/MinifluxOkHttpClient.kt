package com.jocmp.capy.accounts.miniflux

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.BasicAuthInterceptor
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.clientCertAlias
import com.jocmp.capy.accounts.httpClientBuilder
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.net.URI

internal object MinifluxOkHttpClient {
    suspend fun forAccount(path: URI, preferences: AccountPreferences, source: Source, clientCertManager: ClientCertManager): OkHttpClient {
        val username = preferences.username.get()
        val password = preferences.password.get()
        val clientCertAliasValue = preferences.clientCertAlias.get()

        return httpClientBuilder(cachePath = path)
            .addInterceptor(authInterceptor(source, username, password))
            .clientCertAlias(clientCertManager, clientCertAliasValue)
            .build()
    }

    private fun authInterceptor(source: Source, username: String, password: String): Interceptor {
        return if (source == Source.MINIFLUX_TOKEN) {
            Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("X-Auth-Token", password)
                    .build()
                chain.proceed(request)
            }
        } else {
            BasicAuthInterceptor {
                Credentials.basic(username, password)
            }
        }
    }
}
