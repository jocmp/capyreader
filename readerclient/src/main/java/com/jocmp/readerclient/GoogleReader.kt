package com.jocmp.readerclient

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleReader {
    @GET("reader/api/0/subscription/list")
    suspend fun subscriptionList(
        @Query("output") output: String = "json"
    ): Response<SubscriptionListResult>

    @GET("reader/api/0/stream/items/ids")
    suspend fun streamItemsIDs(
       @Query("s") streamID: String,
       @Query("n") count: Int = 10_000,
       @Query("xt") excludedStreamID: String? = null,
       @Query("output") output: String = "json",
    ): Response<StreamItemIDsResult>

    // use to fetch missing articles
    @POST("reader/api/0/stream/items/contents")
    suspend fun streamItemsContents(
//        @FormUrlEncoded
    )

    @GET("reader/api/0/stream/contents/{streamID}")
    suspend fun streamContents(
        @Path("streamID") streamID: String,
        @Query("n") count: Int = 100,
        /** Epoch timestamp. Items older than this timestamp are filtered out. */
        @Query("ot") since: Long? = null,
        /** A stream ID to exclude from the list. */
        @Query("xt") excludedStreamID: String = Stream.READ.id,
        @Query("c") continuation: String? = null,
        @Query("output") output: String = "json",
    ): Response<StreamContentsResult>

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
