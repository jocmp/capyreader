package com.jocmp.capy.accounts

import com.jocmp.feedbinclient.Feedbin
import com.jocmp.readerclient.GoogleReader

interface Credentials {
    val username: String
    val secret: String

    companion object {
        suspend fun verify(credentials: Credentials): Result<Credentials> =
            when (credentials) {
                is FeedbinCredentials -> verifyFeedbin(credentials)
                is ReaderCredentials -> verifyReader(credentials)
                else -> throw UnsupportedOperationException()
            }

        private suspend fun verifyFeedbin(credentials: FeedbinCredentials): Result<FeedbinCredentials> {
            val response = Feedbin.verifyCredentials(
                username = credentials.username,
                password = credentials.secret
            )

            return if (response.isSuccessful) {
                Result.success(credentials)
            } else {
                Result.failure(Throwable("Failed with status ${response.message()}"))
            }
        }

        private suspend fun verifyReader(credentials: ReaderCredentials): Result<ReaderCredentials> {
            val response = GoogleReader.verifyCredentials(
                username = credentials.username,
                password = credentials.secret,
                baseURL = credentials.url
            )

            val resultCredentials = ReaderCredentials(
                username = "",
                secret = "",
                url = credentials.url,
            )


            return if (response.isSuccessful) {
                Result.success(resultCredentials)
            } else {
                Result.failure(Throwable("Failed with status ${response.message()}"))
            }
        }
    }
}

data class FeedbinCredentials(override val username: String, override val secret: String) :
    Credentials

data class ReaderCredentials(
    override val username: String,
    override val secret: String,
    val url: String
) : Credentials
