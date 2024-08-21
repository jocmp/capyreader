package com.jocmp.capy.accounts

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.UserAgentInterceptor
import com.jocmp.feedbinclient.BasicAuthInterceptor
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.OkHttpClient
import java.io.File
import java.net.URI

internal object FeedbinOkHttpClient {
    fun forAccount(path: URI, preferences: AccountPreferences): OkHttpClient {
        val basicAuthInterceptor = BasicAuthInterceptor {
            val username = preferences.username.get()
            val password = preferences.password.get()

            Credentials.basic(username, password)
        }

        return OkHttpClient.Builder()
            .addInterceptor(basicAuthInterceptor)
            .addInterceptor(UserAgentInterceptor())
            .cache(
                Cache(
                    directory = File(File(path), "http_cache"),
                    maxSize = 50L * 1024L * 1024L // 50 MiB
                )
            )
            .build()
    }
}
