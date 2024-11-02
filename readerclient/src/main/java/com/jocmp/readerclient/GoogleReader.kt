package com.jocmp.readerclient

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleReader {
    @POST("accounts/ClientLogin")
    suspend fun clientLogin(
        @Query("Email") email: String,
        @Query("Passwd") password: String
    ): Response<String>

    companion object {
        fun create(
            client: OkHttpClient,
            baseURL: String
        ): GoogleReader {
            val moshi = Moshi.Builder().build()

            return Retrofit.Builder()
                .client(client)
                .baseUrl(baseURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create()
        }

        suspend fun verifyCredentials(
            username: String,
            password: String,
            baseURL: String,
            client: OkHttpClient = OkHttpClient(),
        ): Response<String> {
            val googleReader = create(client = client, baseURL = baseURL)

            return googleReader.clientLogin(email = username, password = password)
        }
    }
}
