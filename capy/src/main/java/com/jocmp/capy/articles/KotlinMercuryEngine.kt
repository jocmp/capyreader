package com.jocmp.capy.articles

import com.jocmp.mercury.ContentType
import com.jocmp.mercury.Mercury
import com.jocmp.mercury.ParseOptions

object KotlinMercuryEngine : MercuryEngine {
    override suspend fun parse(url: String, html: String): MercuryEngine.Result {
        val result = Mercury.parse(
            url = url,
            options = ParseOptions(
                html = html,
                contentType = ContentType.HTML,
            ),
        )

        return MercuryEngine.Result(
            content = result.content,
            leadImageUrl = result.leadImageUrl,
            error = result.error,
            message = result.message,
        )
    }
}
