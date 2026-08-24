package com.jocmp.capy.accounts

import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.accounts.feedbin.FeedbinCredentials
import com.jocmp.capy.accounts.miniflux.MinifluxCredentials
import com.jocmp.capy.accounts.reader.ReaderCredentials
import com.jocmp.capy.common.optionalURL

interface Credentials {
    val username: String
    val secret: String
    val url: String
    val clientCertAlias: String
    val customHeaders: Map<String, String>
    val source: Source

    suspend fun verify(): Result<Credentials>

    companion object {
        fun from(
            source: Source,
            username: String,
            password: String,
            url: String,
            clientCertAlias: String = "",
            customHeaders: Map<String, String> = emptyMap(),
            clientCertManager: ClientCertManager = ClientCertManager { builder, _ -> builder },
        ): Credentials {
            return when (source) {
                Source.FEEDBIN -> FeedbinCredentials(username, password)
                Source.MINIFLUX, Source.MINIFLUX_TOKEN -> MinifluxCredentials(
                    username = username,
                    secret = password,
                    url = normalizeURL(url),
                    clientCertAlias = clientCertAlias,
                    customHeaders = customHeaders,
                    source = source,
                    clientCertManager = clientCertManager,
                )
                Source.FRESHRSS,
                Source.READER -> ReaderCredentials(
                    username = username,
                    secret = password,
                    url = normalizeURL(url),
                    clientCertAlias = clientCertAlias,
                    customHeaders = customHeaders,
                    source = source,
                    clientCertManager = clientCertManager,
                )

                Source.LOCAL -> throw UnsupportedOperationException()
            }
        }

        private fun normalizeURL(value: String): String {
            return optionalURL(value)?.toString().orEmpty()
        }
    }
}
