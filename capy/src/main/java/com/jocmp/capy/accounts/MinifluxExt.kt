package com.jocmp.capy.accounts

import com.jocmp.capy.accounts.miniflux.MinifluxOkHttpClient
import com.jocmp.minifluxclient.Miniflux
import java.net.URI


fun Miniflux.Companion.forAccount(path: URI, baseURL: String, username: String, password: String, source: Source) =
    create(
        client = MinifluxOkHttpClient.forAccount(path, username, password, source),
        baseURL = baseURL
    )

fun withMinifluxPath(url: String): String {
    return if (url.endsWith("/v1") || url.endsWith("/v1/")) {
        url
    } else {
        url.trimEnd('/') + "/v1/"
    }
}
