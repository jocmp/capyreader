package com.jocmp.feedbin

import com.squareup.moshi.Moshi
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import java.io.IOException


interface FeedbinClient {
    @GET("v2/authentication.json")
    suspend fun authentication(): Response<Void>

    companion object {
        private const val DEFAULT_URL = "https://api.feedbin.com/"

        fun create(
            username: String,
            password: String,
            baseURL: String = DEFAULT_URL
        ): FeedbinClient {
            val client = OkHttpClient.Builder()
                .addInterceptor(BasicAuthInterceptor(username, password))
                .build()
            val moshi = Moshi.Builder().build()

            return Retrofit.Builder()
                .client(client)
                .baseUrl(baseURL)
                .addConverterFactory(MoshiConverterFactory.create(moshi)).build().create()
        }
    }
}

private class BasicAuthInterceptor(user: String, password: String) : Interceptor {
    private val credentials: String

    init {
        credentials = Credentials.basic(user, password)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder().header("Authorization", credentials).build()
        return chain.proceed(authenticatedRequest)
    }
}