package com.jocmp.capy.accounts.reader

import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.baseHttpClient
import com.jocmp.capy.accounts.clientCertAlias
import com.jocmp.readerclient.GoogleReader
import com.jocmp.readerclient.GoogleReader.Companion.UNAUTHORIZED_MESSAGE

data class ReaderCredentials(
    override val username: String,
    override val secret: String,
    override val url: String,
    override val clientCertAlias: String,
    override val source: Source,
    private val clientCertManager: ClientCertManager,
) : Credentials {
    override suspend fun verify(): Result<Credentials> {
        try {
            val response = GoogleReader.verifyCredentials(
                username = username,
                password = secret,
                baseURL = url,
                client = baseHttpClient()
                    .newBuilder()
                    .clientCertAlias(clientCertManager, clientCertAlias)
                    .build()
            )

            val responseBody = response.body()

            return if (response.isSuccessful && responseBody != null) {
                parseCredentials(responseBody)
            } else {
                Result.failure(Throwable("Failed with status ${response.code()} ${response.message()}"))
            }
        } catch (e: Throwable) {
            return Result.failure(e)
        }
    }

    private fun parseCredentials(responseBody: String): Result<Credentials> {
        if (responseBody.contains(UNAUTHORIZED_MESSAGE)) {
            return Result.failure(Throwable(responseBody))
        }

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
