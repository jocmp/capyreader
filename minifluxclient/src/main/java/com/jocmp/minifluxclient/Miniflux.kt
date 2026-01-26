package com.jocmp.minifluxclient

import com.squareup.moshi.Moshi
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface Miniflux {
    @GET("feeds")
    suspend fun feeds(): Response<List<Feed>>

    @GET("feeds/{feedID}")
    suspend fun feed(@Path("feedID") feedID: Long): Response<Feed>

    @POST("feeds")
    suspend fun createFeed(@Body request: CreateFeedRequest): Response<CreateFeedResponse>

    @PUT("feeds/{feedID}")
    suspend fun updateFeed(
        @Path("feedID") feedID: Long,
        @Body request: UpdateFeedRequest
    ): Response<Feed>

    @DELETE("feeds/{feedID}")
    suspend fun deleteFeed(@Path("feedID") feedID: Long): Response<Unit>

    @GET("feeds/{feedID}/icon")
    suspend fun feedIcon(@Path("feedID") feedID: Long): Response<IconData>

    @PUT("feeds/{feedID}/refresh")
    suspend fun refreshFeed(@Path("feedID") feedID: Long): Response<Unit>

    @PUT("feeds/{feedID}/mark-all-as-read")
    suspend fun markFeedAsRead(@Path("feedID") feedID: Long): Response<Unit>

    @GET("entries")
    suspend fun entries(
        @Query("status") status: String? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("direction") direction: String? = null,
        @Query("before") before: Long? = null,
        @Query("after") after: Long? = null,
        @Query("before_entry_id") beforeEntryId: Long? = null,
        @Query("after_entry_id") afterEntryId: Long? = null,
        @Query("starred") starred: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: Long? = null
    ): Response<EntryResultSet>

    @GET("feeds/{feedID}/entries")
    suspend fun feedEntries(
        @Path("feedID") feedID: Long,
        @Query("status") status: String? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("direction") direction: String? = null,
        @Query("before") before: Long? = null,
        @Query("after") after: Long? = null,
        @Query("starred") starred: Boolean? = null,
        @Query("search") search: String? = null
    ): Response<EntryResultSet>

    @GET("entries/{entryID}")
    suspend fun entry(@Path("entryID") entryID: Long): Response<Entry>

    @PUT("entries")
    suspend fun updateEntries(@Body request: UpdateEntriesRequest): Response<Unit>

    @PUT("entries/{entryID}/bookmark")
    suspend fun toggleBookmark(@Path("entryID") entryID: Long): Response<Unit>

    @GET("categories")
    suspend fun categories(@Query("counts") counts: Boolean? = null): Response<List<Category>>

    @GET("categories/{categoryID}")
    suspend fun category(@Path("categoryID") categoryID: Long): Response<Category>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<Category>

    @PUT("categories/{categoryID}")
    suspend fun updateCategory(
        @Path("categoryID") categoryID: Long,
        @Body request: UpdateCategoryRequest
    ): Response<Category>

    @DELETE("categories/{categoryID}")
    suspend fun deleteCategory(@Path("categoryID") categoryID: Long): Response<Unit>

    @GET("categories/{categoryID}/feeds")
    suspend fun categoryFeeds(@Path("categoryID") categoryID: Long): Response<List<Feed>>

    @GET("categories/{categoryID}/entries")
    suspend fun categoryEntries(
        @Path("categoryID") categoryID: Long,
        @Query("status") status: String? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("order") order: String? = null,
        @Query("direction") direction: String? = null,
        @Query("starred") starred: Boolean? = null,
        @Query("search") search: String? = null
    ): Response<EntryResultSet>

    @PUT("categories/{categoryID}/mark-all-as-read")
    suspend fun markCategoryAsRead(@Path("categoryID") categoryID: Long): Response<Unit>

    @GET("icons/{iconID}")
    suspend fun icon(@Path("iconID") iconID: Long): Response<IconData>

    @GET("me")
    suspend fun me(): Response<User>

    companion object {
        fun create(client: OkHttpClient, baseURL: String): Miniflux {
            val moshi = Moshi.Builder().build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(Miniflux::class.java)
        }

        suspend fun verifyCredentials(
            username: String,
            password: String,
            baseURL: String
        ): Boolean {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Authorization", Credentials.basic(username, password))
                        .build()
                    chain.proceed(request)
                }
                .build()

            val miniflux = create(client, baseURL)

            return try {
                val response = miniflux.me()
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }
    }
}
