package com.jocmp.capy.accounts.miniflux

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.accounts.BasicAuthInterceptor
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.httpClientBuilder
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.net.URI

internal object MinifluxOkHttpClient {
    fun forAccount(path: URI, preferences: AccountPreferences, source: Source): OkHttpClient {
        return httpClientBuilder(cachePath = path)
            .addInterceptor(authInterceptor(source, preferences))
            .build()
    }

    private fun authInterceptor(source: Source, preferences: AccountPreferences): Interceptor {
        return if (source == Source.MINIFLUX_TOKEN) {
            Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("X-Auth-Token", preferences.password.get())
                    .build()
                chain.proceed(request)
            }
        } else {
            BasicAuthInterceptor {
                val username = preferences.username.get()
                val password = preferences.password.get()

                Credentials.basic(username, password)
            }
        }
    }
}
