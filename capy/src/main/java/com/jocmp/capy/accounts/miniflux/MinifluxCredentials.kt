package com.jocmp.capy.accounts.miniflux

import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.Source
import com.jocmp.minifluxclient.Miniflux

internal data class MinifluxCredentials(
    override val username: String,
    override val secret: String,
    override val url: String,
    override val source: Source,
) : Credentials {
    override val clientCertAlias: String = ""

    override suspend fun verify(): Result<Credentials> {
        val verified = if (source == Source.MINIFLUX_TOKEN) {
            Miniflux.verifyToken(
                token = secret,
                baseURL = url
            )
        } else {
            Miniflux.verifyCredentials(
                username = username,
                password = secret,
                baseURL = url
            )
        }

        return if (verified) {
            Result.success(this)
        } else {
            Result.failure(Throwable("Failed to verify Miniflux credentials"))
        }
    }
}
