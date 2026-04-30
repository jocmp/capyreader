/*
 * Created by Josiah Campbell.
 */
package com.jocmp.newsblurclient

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface NewsBlur {
    @FormUrlEncoded
    @POST("api/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): Response<NewsBlurLoginResponse>

    companion object {
        const val DEFAULT_URL = "https://newsblur.com/"

        fun create(
            client: OkHttpClient,
            baseURL: String = DEFAULT_URL,
        ): NewsBlur {
            val moshi = Moshi.Builder().build()

            return Retrofit.Builder()
                .client(client)
                .baseUrl(baseURL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create()
        }

        suspend fun verifyCredentials(
            username: String,
            password: String,
            client: OkHttpClient = OkHttpClient(),
            baseURL: String = DEFAULT_URL,
        ): Response<NewsBlurLoginResponse> {
            val newsblur = create(client = client, baseURL = baseURL)

            return newsblur.login(username = username, password = password)
        }
    }
}
