package com.jocmp.feedbinclient.api

import com.jocmp.feedbinclient.Entry
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface Feedbin {
    @GET("v2/entries.json")
    suspend fun entries(@Query("page") page: String? = null): Response<List<Entry>>

    @GET("v2/authentication.json")
    suspend fun authentication(@Header("Authorization") credentials: String): Response<Void>

    @GET("v2/subscriptions.json")
    suspend fun subscriptions(): Response<List<Subscription>>

    @GET("v2/taggings.json")
    suspend fun taggings(): Response<List<Tagging>>

    companion object {
        private const val DEFAULT_URL = "https://api.feedbin.com/"

        fun create(
            client: OkHttpClient,
            baseURL: String = DEFAULT_URL
        ): Feedbin {
            val moshi = Moshi.Builder().build()

            return Retrofit.Builder()
                .client(client)
                .baseUrl(baseURL)
                .addConverterFactory(MoshiConverterFactory.create(moshi)).build().create()
        }
    }
}
