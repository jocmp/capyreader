package com.jocmp.feedbinclient.api

import android.content.Context
import com.jocmp.feedbinclient.CredentialsManager
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FeedbinClient {
    @GET("v2/entries.json")
    suspend fun entries(@Query("page") page: String? = null): Response<List<FeedbinEntry>>

    @GET("v2/authentication.json")
    suspend fun authentication(@Header("Authorization") credentials: String): Response<Void>

    @GET("v2/subscriptions.json")
    suspend fun subscriptions(): Response<List<FeedbinSubscription>>

    @GET("v2/taggings.json")
    suspend fun taggings(): Response<List<FeedbinTagging>>

    companion object {
        private const val DEFAULT_URL = "https://api.feedbin.com/"
        private const val CACHE_SIZE: Long = 10 * 1024 * 1024 // 10 MB

        fun create(
            context: Context,
            credentialsManager: CredentialsManager,
            baseURL: String = DEFAULT_URL
        ): FeedbinClient {
            val cache = Cache(context.cacheDir, CACHE_SIZE)

            val client = OkHttpClient.Builder()
                .addInterceptor(BasicAuthInterceptor(credentialsManager = credentialsManager))
                .cache(cache)
                .build()

            val moshi = Moshi.Builder().build()

            return Retrofit.Builder()
                .client(client)
                .baseUrl(baseURL)
                .addConverterFactory(MoshiConverterFactory.create(moshi)).build().create()
        }
    }
}

private class BasicAuthInterceptor(val credentialsManager: CredentialsManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val account = credentialsManager.fetch()
        val credentials = Credentials.basic(account.username, account.password)

        if (request.headers("Authorization").isNullOrEmpty()) {
            val authenticatedRequest = request.newBuilder().header("Authorization", credentials).build()
            return chain.proceed(authenticatedRequest)
        }

        return chain.proceed(request)
    }
}
