package com.jocmp.feedbinclient

import com.squareup.moshi.Moshi
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface Feedbin {
    @GET("v2/entries.json")
    suspend fun entries(
        @Query("page") page: String? = null,
        @Query("since") since: String? = null,
        @Query("mode") mode: String? = "extended",
        @Query("per_page") perPage: Int? = 100,
        /** Comma delimited string of numbers */
        @Query("ids") ids: String? = null
    ): Response<List<Entry>>

    @GET("v2/authentication.json")
    suspend fun authentication(@Header("Authorization") authentication: String): Response<Void>

    @GET("v2/icons.json")
    suspend fun icons(): Response<List<Icon>>

    @Headers("Cache-Control: no-cache")
    @GET("v2/subscriptions.json")
    suspend fun subscriptions(): Response<List<Subscription>>

    @POST("v2/subscriptions.json")
    suspend fun createSubscription(@Body body: CreateSubscriptionRequest): Response<Subscription>

    @DELETE("v2/subscriptions/{subscriptionID}.json")
    suspend fun deleteSubscription(@Path("subscriptionID") subscriptionID: String): Response<Void>

    @PATCH("v2/subscriptions/{subscriptionID}.json")
    suspend fun updateSubscription(
        @Path("subscriptionID") subscriptionID: String,
        @Body body: UpdateSubscriptionRequest
    ): Response<Subscription>

    @GET("v2/taggings.json")
    suspend fun taggings(): Response<List<Tagging>>

    @POST("v2/taggings.json")
    suspend fun createTagging(@Body body: CreateTaggingRequest): Response<Tagging>

    @DELETE("v2/taggings/{taggingID}.json")
    suspend fun deleteTagging(@Path("taggingID") taggingID: String): Response<Void>

    @GET("v2/starred_entries.json")
    suspend fun starredEntries(): Response<List<Long>>

    @POST("v2/starred_entries.json")
    suspend fun createStarredEntries(@Body body: StarredEntriesRequest): Response<List<Long>>

    @HTTP(method = "DELETE", path = "v2/starred_entries.json", hasBody = true)
    suspend fun deleteStarredEntries(@Body body: StarredEntriesRequest): Response<List<Long>>

    @GET("v2/unread_entries.json")
    suspend fun unreadEntries(): Response<List<Long>>

    @POST("v2/unread_entries.json")
    suspend fun createUnreadEntries(@Body body: UnreadEntriesRequest): Response<List<Long>>

    @HTTP(method = "DELETE", path = "v2/unread_entries.json", hasBody = true)
    suspend fun deleteUnreadEntries(@Body body: UnreadEntriesRequest): Response<List<Long>>

    @GET
    suspend fun fetchExtractedContent(@Url url: String): Response<ExtractedContent>

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
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create()
        }

        suspend fun verifyCredentials(
            username: String,
            password: String,
            client: OkHttpClient = OkHttpClient(),
            baseURL: String = DEFAULT_URL
        ): Boolean {
            val feedbin = create(client = client, baseURL = baseURL)

            val authentication = Credentials.basic(username, password)

            return feedbin
                .authentication(authentication = authentication)
                .isSuccessful
        }
    }
}
