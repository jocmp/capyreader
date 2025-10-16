package com.jocmp.capy.accounts.miniflux

import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.Source
import com.jocmp.minifluxclient.Miniflux

internal data class MinifluxCredentials(
    override val username: String,
    override val secret: String,
    override val url: String,
) : Credentials {
    override val clientCertAlias: String = ""

    override val source: Source = Source.MINIFLUX

    override suspend fun verify(): Result<Credentials> {
        val verified = Miniflux.verifyCredentials(
            username = username,
            password = secret,
            baseURL = url
        )

        return if (verified) {
            Result.success(this)
        } else {
            Result.failure(Throwable("Failed to verify Miniflux credentials"))
        }
    }
}
