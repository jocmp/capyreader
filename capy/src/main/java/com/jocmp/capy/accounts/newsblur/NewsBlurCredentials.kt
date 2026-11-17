package com.jocmp.capy.accounts.newsblur

import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.baseHttpClient
import com.jocmp.capy.accounts.clientCertAlias
import com.jocmp.newsblurclient.NewsBlur

internal data class NewsBlurCredentials(
    override val username: String,
    override val secret: String,
    override val url: String,
    override val clientCertAlias: String = "",
    private val clientCertManager: ClientCertManager = ClientCertManager { builder, _ -> builder },
) : Credentials {
    override val source = Source.NEWSBLUR

    override suspend fun verify(): Result<Credentials> {
        val baseURL = url.ifBlank { NewsBlur.DEFAULT_URL }

        val client = baseHttpClient()
            .newBuilder()
            .clientCertAlias(clientCertManager, clientCertAlias)
            .build()

        return NewsBlur.login(
            username = username,
            password = secret,
            baseURL = baseURL,
            client = client,
        ).map { sessionID ->
            copy(secret = sessionID, url = baseURL)
        }
    }
}
