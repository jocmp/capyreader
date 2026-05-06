package com.jocmp.capy.articles

import com.jocmp.capy.common.withIOContext
import net.dankito.readability4j.Readability4J

class Readability4JExtractor : ContentExtractor {
    override suspend fun extract(url: String?, html: String): Result<String> = withIOContext {
        try {
            val readability = Readability4J(url.orEmpty(), html)
            val article = readability.parse()
            val content = article.contentWithUtf8Encoding ?: article.content
            if (content.isNullOrBlank()) {
                Result.failure(Throwable("Readability4J returned empty content"))
            } else {
                Result.success(content)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
