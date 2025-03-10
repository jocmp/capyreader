package com.jocmp.capy.accounts.feedbin

import android.content.Context
import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.Source
import com.jocmp.feedbinclient.Feedbin

internal data class FeedbinCredentials(
    override val username: String,
    override val secret: String,
) : Credentials {
    override val url = ""
    override val clientCertAlias: String = ""

    override val source: Source = Source.FEEDBIN

    override suspend fun verify(context: Context): Result<Credentials> {
        val response = Feedbin.verifyCredentials(
            username = username,
            password = secret
        )

        return if (response.isSuccessful) {
            Result.success(this)
        } else {
            Result.failure(Throwable("Failed with status ${response.code()} ${response.message()}"))
        }
    }
}
