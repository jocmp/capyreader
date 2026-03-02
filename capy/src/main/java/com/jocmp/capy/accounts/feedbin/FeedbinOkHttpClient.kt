package com.jocmp.capy.accounts.feedbin

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.accounts.httpClientBuilder
import com.jocmp.capy.accounts.BasicAuthInterceptor
import okhttp3.Credentials
import okhttp3.OkHttpClient
import java.net.URI

internal object FeedbinOkHttpClient {
    suspend fun forAccount(path: URI, preferences: AccountPreferences): OkHttpClient {
        val username = preferences.username.get()
        val password = preferences.password.get()

        return httpClientBuilder(cachePath = path)
            .addInterceptor(
                BasicAuthInterceptor {
                    Credentials.basic(username, password)
                }
            )
            .build()
    }
}
