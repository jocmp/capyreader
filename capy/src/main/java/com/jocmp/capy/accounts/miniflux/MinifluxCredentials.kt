package com.jocmp.capy.accounts.miniflux

import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.baseHttpClient
import com.jocmp.capy.accounts.clientCertAlias
import com.jocmp.minifluxclient.Miniflux

internal data class MinifluxCredentials(
    override val username: String,
    override val secret: String,
    override val url: String,
    override val clientCertAlias: String,
    override val source: Source,
    private val clientCertManager: ClientCertManager,
) : Credentials {
    override suspend fun verify(): Result<Credentials> {
        val client = baseHttpClient()
            .newBuilder()
            .clientCertAlias(clientCertManager, clientCertAlias)
            .build()

        if (source == Source.MINIFLUX_TOKEN) {
            val response = Miniflux.verifyToken(
                token = secret,
                baseURL = url,
                client = client,
            )

            val username = response?.body()?.username

            if (response?.isSuccessful == true && username != null) {
                return Result.success(this.copy(username = username))
            }
        } else {
            val verified = Miniflux.verifyCredentials(
                username = username,
                password = secret,
                baseURL = url,
                client = client,
            )

            if (verified) {
                return Result.success(this)
            }
        }

        return Result.failure(Throwable("Failed to verify Miniflux credentials"))
    }
}
