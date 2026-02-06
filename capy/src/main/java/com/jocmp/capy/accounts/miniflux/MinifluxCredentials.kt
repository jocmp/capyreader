package com.jocmp.capy.accounts.miniflux

import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.Source
import com.jocmp.minifluxclient.Miniflux

internal data class MinifluxCredentials(
    override val username: String,
    override val secret: String,
    override val url: String,
    override val source: Source,
    override val showReadingTime: Boolean = false,
) : Credentials {
    override val clientCertAlias: String = ""

    override suspend fun verify(): Result<Credentials> {
        if (source == Source.MINIFLUX_TOKEN) {
            val response = Miniflux.verifyToken(
                token = secret,
                baseURL = url
            )

            val user = response?.body()

            if (response?.isSuccessful == true && user != null) {
                return Result.success(
                    this.copy(
                        username = user.username,
                        showReadingTime = user.showReadingTime,
                    )
                )
            }
        } else {
            val response = Miniflux.verifyCredentials(
                username = username,
                password = secret,
                baseURL = url
            )

            val user = response?.body()

            if (response?.isSuccessful == true && user != null) {
                return Result.success(
                    this.copy(showReadingTime = user.showReadingTime)
                )
            }
        }

        return Result.failure(Throwable("Failed to verify Miniflux credentials"))
    }
}
