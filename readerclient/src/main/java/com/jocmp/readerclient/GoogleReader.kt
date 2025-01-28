package com.jocmp.readerclient

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleReader {
    @GET("reader/api/0/subscription/list")
    suspend fun subscriptionList(
        @Query("output") output: String = "json"
    ): Response<SubscriptionListResult>

    @GET("reader/api/0/tag/list")
    suspend fun tagList(
        @Query("output") output: String = "json"
    ): Response<TagListResult>

    @GET("reader/api/0/stream/items/ids")
    suspend fun streamItemsIDs(
        @Query("s") streamID: String,
        /** Epoch timestamp. Items older than this timestamp are filtered out. */
        @Query("ot") since: Long? = null,
        @Query("c") continuation: String? = null,
        @Query("n") count: Int = 10_000,
        /** A stream ID to exclude from the list. */
        @Query("xt") excludedStreamID: String? = null,
        @Query("output") output: String = "json",
    ): Response<StreamItemIDsResult>

    @FormUrlEncoded
    @POST("reader/api/0/stream/items/contents")
    suspend fun streamItemsContents(
        @Field("i") ids: List<String>,
        @Field("T") postToken: String?,
        @Query("output") output: String = "json",
    ): Response<StreamItemsContentsResult>

    @FormUrlEncoded
    @POST("reader/api/0/edit-tag")
    suspend fun editTag(
        @Field("i") ids: List<String>,
        @Field("T") postToken: String?,
        @Field("a") addTag: String? = null,
        @Field("r") removeTag: String? = null,
        @Query("output") output: String = "json",
    ): Response<String>

    @FormUrlEncoded
    @POST("reader/api/0/subscription/quickadd")
    suspend fun quickAddSubscription(
        @Field("quickadd") url: String,
        @Field("T") postToken: String?,
        @Query("output") output: String = "json"
    ): Response<SubscriptionQuickAddResult>

    @FormUrlEncoded
    @POST("reader/api/0/subscription/edit")
    suspend fun editSubscription(
        @Field("s") id: String,
        @Field("ac") actionID: String,
        @Field("a") addCategoryID: String? = null,
        @Field("t") title: String? = null,
        @Field("T") postToken: String?,
    ): Response<String>

    @POST("accounts/ClientLogin")
    suspend fun clientLogin(
        @Query("Email") email: String,
        @Query("Passwd") password: String
    ): Response<String>

    @GET("reader/api/0/token")
    suspend fun token(): Response<String>

    companion object {
        const val BAD_TOKEN_HEADER_KEY = "X-Reader-Google-Bad-Token"

        const val UNAUTHORIZED_MESSAGE = "Unauthorized"

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
