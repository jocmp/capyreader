package com.jocmp.googlereaderclient

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

interface GoogleReader {
    
    companion object {
        fun create(
            client: OkHttpClient,
            baseURL: String
        ): GoogleReader {
            val moshi = Moshi.Builder().build()

            return Retrofit.Builder()
                .client(client)
                .baseUrl(baseURL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create()
        }
    }
}
