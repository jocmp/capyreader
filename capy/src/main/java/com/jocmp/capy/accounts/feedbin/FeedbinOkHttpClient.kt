package com.jocmp.capy.accounts.feedbin

import com.jocmp.capy.accounts.httpClientBuilder
import com.jocmp.capy.accounts.BasicAuthInterceptor
import okhttp3.Credentials
import okhttp3.OkHttpClient
import java.net.URI

internal object FeedbinOkHttpClient {
    fun forAccount(path: URI, username: String, password: String): OkHttpClient {
        return httpClientBuilder(cachePath = path)
            .addInterceptor(
                BasicAuthInterceptor {
                    Credentials.basic(username, password)
                }
            )
            .build()
    }
}
