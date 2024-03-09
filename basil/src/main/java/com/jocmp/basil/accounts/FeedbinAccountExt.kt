package com.jocmp.basil.accounts

import com.jocmp.basil.Account
import com.jocmp.feedbinclient.BasicAuthInterceptor
import com.jocmp.feedbinclient.Feedbin
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.OkHttpClient
import java.io.File

internal fun Feedbin.Companion.forAccount(
    account: Account,
): Feedbin {
    val basicAuthInterceptor = BasicAuthInterceptor {
        val username = account.preferences.username.get()
        val password = account.preferences.password.get()

        Credentials.basic(username, password)
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(basicAuthInterceptor)
        .cache(
            Cache(
                directory = File(File(account.path), "http_cache"),
                maxSize = 50L * 1024L * 1024L // 50 MiB
            )
        )
        .build()

    return create(client = client)
}
