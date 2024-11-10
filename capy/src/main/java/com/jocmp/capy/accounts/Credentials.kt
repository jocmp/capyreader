package com.jocmp.capy.accounts

import com.jocmp.capy.accounts.feedbin.FeedbinCredentials
import com.jocmp.capy.accounts.reader.ReaderCredentials
import com.jocmp.capy.common.optionalURL

interface Credentials {
    val username: String
    val secret: String
    val url: String
    val source: Source

    suspend fun verify(): Result<Credentials>

    companion object {
        fun from(
            source: Source,
            username: String,
            password: String,
            url: String,
        ): Credentials {
            return when (source) {
                Source.FEEDBIN -> FeedbinCredentials(username, password)
                Source.FRESHRSS, Source.READER -> ReaderCredentials(username, password, url = normalizeURL(url))
                Source.LOCAL -> throw UnsupportedOperationException()
            }
        }

        private fun normalizeURL(value: String): String {
            return optionalURL(value)?.toString().orEmpty()
        }
    }
}
