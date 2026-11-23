package com.jocmp.newsblurclient

import com.squareup.moshi.Moshi
import okhttp3.Cookie
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsBlur {
    @FormUrlEncoded
    @POST("api/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): Response<LoginResponse>

    @GET("reader/feeds")
    suspend fun feeds(
        @Query("flat") flat: Boolean = true,
        @Query("update_counts") updateCounts: Boolean = false,
    ): Response<FeedsResponse>

    @GET("reader/unread_story_hashes")
    suspend fun unreadStoryHashes(): Response<UnreadStoryHashesResponse>

    @GET("reader/starred_story_hashes")
    suspend fun starredStoryHashes(): Response<StarredStoryHashesResponse>

    @GET("reader/feed/{feedID}")
    suspend fun feedStories(
        @Path("feedID") feedID: Long,
        @Query("page") page: Int,
        @Query("read_filter") readFilter: String = "all",
        @Query("include_story_content") includeStoryContent: Boolean = true,
    ): Response<StoriesResponse>

    @GET("reader/river_stories")
    suspend fun riverStories(
        @Query("h") storyHashes: List<String>,
        @Query("include_hidden") includeHidden: Boolean = false,
    ): Response<StoriesResponse>

    @FormUrlEncoded
    @POST("reader/mark_story_hashes_as_read")
    suspend fun markStoryHashesAsRead(
        @Field("story_hash") storyHashes: List<String>,
    ): Response<CodeResponse>

    @FormUrlEncoded
    @POST("reader/mark_story_hash_as_unread")
    suspend fun markStoryHashAsUnread(
        @Field("story_hash") storyHashes: List<String>,
    ): Response<CodeResponse>

    @FormUrlEncoded
    @POST("reader/mark_story_hash_as_starred")
    suspend fun markStoryHashAsStarred(
        @Field("story_hash") storyHashes: List<String>,
    ): Response<CodeResponse>

    @FormUrlEncoded
    @POST("reader/mark_story_hash_as_unstarred")
    suspend fun markStoryHashAsUnstarred(
        @Field("story_hash") storyHashes: List<String>,
    ): Response<CodeResponse>

    @FormUrlEncoded
    @POST("reader/add_url")
    suspend fun addURL(
        @Field("url") url: String,
        @Field("folder") folder: String = "",
    ): Response<AddURLResponse>

    @FormUrlEncoded
    @POST("reader/rename_feed")
    suspend fun renameFeed(
        @Field("feed_id") feedID: Long,
        @Field("feed_title") title: String,
    ): Response<CodeResponse>

    @FormUrlEncoded
    @POST("reader/delete_feed")
    suspend fun deleteFeed(
        @Field("feed_id") feedID: Long,
        @Field("in_folder") inFolder: String = "",
    ): Response<CodeResponse>

    @FormUrlEncoded
    @POST("reader/move_feed_to_folders")
    suspend fun moveFeedToFolders(
        @Field("feed_id") feedID: Long,
        @Field("in_folders") inFolders: List<String>,
        @Field("to_folders") toFolders: List<String>,
    ): Response<CodeResponse>

    @FormUrlEncoded
    @POST("reader/add_folder")
    suspend fun addFolder(
        @Field("folder") folder: String,
    ): Response<CodeResponse>

    @FormUrlEncoded
    @POST("reader/rename_folder")
    suspend fun renameFolder(
        @Field("folder_to_rename") folder: String,
        @Field("new_folder_name") newFolderName: String,
        @Field("in_folder") inFolder: String = "",
    ): Response<CodeResponse>

    @FormUrlEncoded
    @POST("reader/delete_folder")
    suspend fun deleteFolder(
        @Field("folder_to_delete") folder: String,
        @Field("in_folder") inFolder: String = "",
    ): Response<CodeResponse>

    companion object {
        const val DEFAULT_URL = "https://www.newsblur.com/"
        const val SESSION_COOKIE = "newsblur_sessionid"

        fun create(client: OkHttpClient, baseURL: String): NewsBlur {
            val moshi = Moshi.Builder().build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(NewsBlur::class.java)
        }

        suspend fun login(
            username: String,
            password: String,
            baseURL: String,
            client: OkHttpClient = OkHttpClient(),
        ): Result<String> {
            return try {
                val newsBlur = create(client, baseURL)
                val response = newsBlur.login(username = username, password = password)
                val body = response.body()
                val sessionID = sessionID(response)

                if (response.isSuccessful && body?.authenticated == true && sessionID != null) {
                    Result.success(sessionID)
                } else {
                    Result.failure(Throwable(body?.errorMessage ?: "Failed with status ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        private fun sessionID(response: Response<*>): String? {
            val url = response.raw().request.url

            return Cookie.parseAll(url, response.headers())
                .find { it.name == SESSION_COOKIE }
                ?.value
        }
    }
}
