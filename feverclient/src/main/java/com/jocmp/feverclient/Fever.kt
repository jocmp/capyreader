package com.jocmp.feverclient

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.POST

interface Fever {
    @POST("fever?api&groups")
    fun groups(): GroupsResult

    @POST("fever?api&feeds")
    fun feeds(): FeedsResult

    @POST("fever?api&favicons")
    fun favicons(): FaviconsResult

    fun create(
        client: OkHttpClient,
        baseURL: String
    ): Fever {
        val moshi = Moshi.Builder()
            .add(createEnumJsonAdapter<MarkState>())
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(baseURL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create()
    }
}
