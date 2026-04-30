/*
 * Created by Josiah Campbell.
 */
package com.jocmp.capy.accounts.newsblur

import com.jocmp.capy.accounts.httpClientBuilder
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

internal object NewsBlurOkHttpClient {
    fun forAccount(path: URI): OkHttpClient {
        return httpClientBuilder(cachePath = path)
            .cookieJar(InMemoryCookieJar())
            .build()
    }

    private class InMemoryCookieJar : CookieJar {
        private val store = ConcurrentHashMap<String, MutableList<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            store.getOrPut(url.host) { mutableListOf() }.apply {
                clear()
                addAll(cookies)
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return store[url.host].orEmpty()
        }
    }
}
