package com.jocmp.capy.accounts.feedbin

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.accounts.BasicAuthInterceptor
import com.jocmp.capy.accounts.httpClientBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.Credentials
import okhttp3.OkHttpClient
import java.net.URI

internal object FeedbinOkHttpClient {
    fun forAccount(path: URI, preferences: AccountPreferences): OkHttpClient {
        return httpClientBuilder(cachePath = path)
            .addInterceptor(
                BasicAuthInterceptor {
                    val username = runBlocking { preferences.username.get() }
                    val password = runBlocking { preferences.password.get() }

                    Credentials.basic(username, password)
                }
            )
            .build()
    }
}
