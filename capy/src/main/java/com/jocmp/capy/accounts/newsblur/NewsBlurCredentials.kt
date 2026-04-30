/*
 * Created by Josiah Campbell.
 */
package com.jocmp.capy.accounts.newsblur

import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.Source
import com.jocmp.newsblurclient.NewsBlur

internal data class NewsBlurCredentials(
    override val username: String,
    override val secret: String,
    override val url: String = NewsBlur.DEFAULT_URL,
) : Credentials {
    override val clientCertAlias = ""

    override val source: Source = Source.NEWSBLUR

    override suspend fun verify(): Result<Credentials> {
        val response = NewsBlur.verifyCredentials(
            username = username,
            password = secret,
            baseURL = baseURL,
        )

        if (!response.isSuccessful) {
            return Result.failure(
                Throwable("Failed with status ${response.code()} ${response.message()}")
            )
        }

        val body = response.body()
            ?: return Result.failure(Throwable("Empty response from NewsBlur"))

        return if (body.authenticated) {
            Result.success(this)
        } else {
            val message = body.errors?.username?.firstOrNull()
                ?: body.errors?.others?.firstOrNull()
                ?: "Invalid username or password"
            Result.failure(Throwable(message))
        }
    }

    private val baseURL: String
        get() = url.ifBlank { NewsBlur.DEFAULT_URL }
}
