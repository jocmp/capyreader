package com.jocmp.capy.accounts

import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.accounts.miniflux.MinifluxOkHttpClient
import com.jocmp.minifluxclient.Miniflux
import java.net.URI


fun Miniflux.Companion.forAccount(path: URI, preferences: AccountPreferences) =
    create(
        client = MinifluxOkHttpClient.forAccount(path, preferences),
        baseURL = preferences.url.get()
    )

fun withMinifluxPath(url: String): String {
    return if (url.endsWith("/v1") || url.endsWith("/v1/")) {
        url
    } else {
        url.trimEnd('/') + "/v1/"
    }
}
