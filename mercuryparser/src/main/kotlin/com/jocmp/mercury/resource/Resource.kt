package com.jocmp.mercury.resource

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.resource.utils.dom.clean
import com.jocmp.mercury.resource.utils.dom.convertLazyLoadedImages
import com.jocmp.mercury.resource.utils.dom.normalizeMetaTags
import com.jocmp.mercury.resource.utils.fetchResource
import com.jocmp.mercury.utils.text.getEncoding
import okhttp3.OkHttpClient
import java.net.URI
import java.nio.charset.Charset

object Resource {
    // Create a Resource.
    //
    // :param url: The URL for the document we should retrieve.
    // :param response: If set, use as the response rather than
    //                  attempting to fetch it ourselves. Expects a
    //                  string.
    // :param headers: Custom headers to be included in the request
    suspend fun create(
        url: String,
        preparedResponse: String? = null,
        parsedUrl: URI? = null,
        headers: Map<String, String> = emptyMap(),
        client: OkHttpClient? = null,
    ): Doc {
        val result: FetchedContent =
            if (preparedResponse != null) {
                FetchedContent(
                    body = preparedResponse.toByteArray(Charsets.UTF_8),
                    contentType = "text/html",
                    alreadyDecoded = true,
                )
            } else {
                val fetched = fetchResource(url, parsedUrl, headers, client)
                if (fetched.failed) {
                    throw IllegalStateException(fetched.message ?: "Failed to fetch resource")
                }
                FetchedContent(
                    body = fetched.body ?: ByteArray(0),
                    contentType = fetched.response?.header("content-type") ?: "",
                    alreadyDecoded = false,
                )
            }

        return generateDoc(result)
    }

    internal fun generateDoc(fetched: FetchedContent): Doc {
        val contentType = fetched.contentType

        // TODO: Implement is_text function from
        // https://github.com/ReadabilityHoldings/readability/blob/8dc89613241d04741ebd42fa9fa7df1b1d746303/readability/utils/text.py#L57
        if (!contentType.contains("html") && !contentType.contains("text")) {
            throw IllegalStateException("Content does not appear to be text.")
        }

        var doc = encodeDoc(fetched)

        if (doc("body").children().length == 0 && doc("body").text().trim().isEmpty()) {
            throw IllegalStateException("No children, likely a bad parse.")
        }

        doc = normalizeMetaTags(doc)
        doc = convertLazyLoadedImages(doc)
        doc = clean(doc)
        return doc
    }

    internal fun encodeDoc(fetched: FetchedContent): Doc {
        if (fetched.alreadyDecoded) {
            return Doc.load(String(fetched.body, Charsets.UTF_8))
        }

        val encoding = getEncoding(fetched.contentType)
        var decoded = decode(fetched.body, encoding)
        var doc = Doc.load(decoded)
        // after first cheerio.load, check to see if encoding matches
        val contentTypeSelector = "meta[http-equiv=content-type i]"
        val metaContentType =
            doc(contentTypeSelector).attr("value")
                ?: doc(contentTypeSelector).attr("content")
                ?: doc("meta[charset]").attr("charset")
        val properEncoding = getEncoding(metaContentType)

        // if encodings in the header/body dont match, use the one in the body
        if (metaContentType != null && properEncoding != encoding) {
            decoded = decode(fetched.body, properEncoding)
            doc = Doc.load(decoded)
        }
        return doc
    }
}

internal data class FetchedContent(
    val body: ByteArray,
    val contentType: String,
    val alreadyDecoded: Boolean,
)

private fun decode(
    body: ByteArray,
    encoding: String,
): String = String(body, Charset.forName(encoding))
