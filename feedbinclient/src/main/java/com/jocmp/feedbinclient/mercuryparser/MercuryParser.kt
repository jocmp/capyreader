package com.jocmp.feedbinclient

import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class MercuryParser(
    private val username: String,
    private val secret: String,
    private val httpClient: OkHttpClient,
) {
    private val adapter = ParserResultJsonAdapter(Moshi.Builder().build())

    suspend fun parse(url: String): ParserResult? = withContext(Dispatchers.IO) {
        val signature = hmacSHA1(secret, url)
        val base64Url = Base64.getUrlEncoder().encodeToString(url.toByteArray())
        val request = Request.Builder()
            .url("$EXTRACT_URL/parser/$username/$signature?base64_url=$base64Url")
            .get()
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    adapter.fromJson(body)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun hmacSHA1(key: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key.toByteArray(), "HmacSHA1"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val EXTRACT_URL = "https://extract.feedbin.com"
    }
}
