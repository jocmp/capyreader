package com.jocmp.capy.accounts.reader

import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.Source
import com.jocmp.readerclient.GoogleReader

data class ReaderCredentials(
    override val username: String,
    override val secret: String,
    val url: String
) : Credentials {
    override val source: Source = Source.FRESHRSS

    override suspend fun verify(): Result<Credentials> {
        val response = GoogleReader.verifyCredentials(
            username = username,
            password = secret,
            baseURL = url
        )

        val responseBody = response.body()

        return if (response.isSuccessful && responseBody != null) {
            parseCredentials(responseBody)
        } else {
            Result.failure(Throwable("Failed with status ${response.message()}"))
        }
    }

    private fun parseCredentials(responseBody: String): Result<Credentials> {
        val auth = findAuth(responseBody)

        return if (auth != null) {
            Result.success(copy(secret = auth))
        } else {
            Result.failure(Throwable("Failed to parse auth"))
        }
    }

    private fun findAuth(responseBody: String): String? {
        return try {
            val entries = responseBody
                .split("\n")
                .map { it.split("=") }
                .associate {
                    it.first() to it.last()
                }

            entries.getOrDefault("Auth", null)
        } catch (e: Throwable) {
            null
        }
    }
}
