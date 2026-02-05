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
        if (source == Source.MINIFLUX_TOKEN) {
            val response = Miniflux.verifyToken(
                token = secret,
                baseURL = url
            )

            val username = response?.body()?.username

            if (response?.isSuccessful == true && username != null) {
                return Result.success(this.copy(username = username))
            }
        } else {
            val verified = Miniflux.verifyCredentials(
                username = username,
                password = secret,
                baseURL = url
            )

            if (verified) {
                return Result.success(this)
            }
        }

        return Result.failure(Throwable("Failed to verify Miniflux credentials"))
    }
}
