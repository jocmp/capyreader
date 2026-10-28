package com.jocmp.capy.accounts.newsblur

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.clientCertAlias
import com.jocmp.capy.accounts.httpClientBuilder
import com.jocmp.newsblurclient.NewsBlur
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.net.URI

internal object NewsBlurOkHttpClient {
    fun forAccount(
        path: URI,
        preferences: AccountPreferences,
        clientCertManager: ClientCertManager,
    ): OkHttpClient {
        return httpClientBuilder(cachePath = path)
            .addInterceptor(sessionInterceptor(preferences))
            .clientCertAlias(clientCertManager, preferences.clientCertAlias.get())
            .build()
    }

    private fun sessionInterceptor(preferences: AccountPreferences): Interceptor {
        return Interceptor { chain ->
            val sessionID = preferences.password.get()

            val request = chain.request().newBuilder()
                .header("Cookie", "${NewsBlur.SESSION_COOKIE}=$sessionID")
                .build()

            chain.proceed(request)
        }
    }
}
