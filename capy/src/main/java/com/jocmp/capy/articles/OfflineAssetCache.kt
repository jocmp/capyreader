package com.jocmp.capy.articles

import com.jocmp.capy.common.await
import com.jocmp.capy.logging.CapyLog
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.net.URI
import java.security.MessageDigest

private const val MAX_ASSET_BYTES: Long = 100L * 1024L * 1024L
private const val ASSET_HOST = "https://appassets.androidplatform.net"

class OfflineAssetCache(
    private val rootDirectory: URI,
    httpClient: OkHttpClient,
) {
    private val client = httpClient.newBuilder().build()

    private val baseDir: File
        get() = File(rootDirectory.path, "offline-assets").also { it.mkdirs() }

    fun articleDir(articleID: String): File =
        File(baseDir, sanitize(articleID))

    fun resolveAsset(articleID: String, fileName: String): File? {
        val file = File(articleDir(articleID), sanitize(fileName))
        return if (file.exists() && file.isFile) file else null
    }

    fun contentType(articleID: String, fileName: String): String? {
        val ct = File(articleDir(articleID), "${sanitize(fileName)}.ct")
        return if (ct.exists()) ct.readText().trim().ifBlank { null } else null
    }

    fun clear(articleID: String) {
        val dir = articleDir(articleID)
        if (dir.exists()) dir.deleteRecursively()
    }

    /**
     * Walk the html for img/video/source assets, fetch each (skipping anything > 100 MB
     * or non-http(s)), persist to disk, and rewrite the html so references point at the
     * in-app asset host. Returns the rewritten html on success.
     */
    suspend fun download(articleID: String, html: String): Result<String> {
        return try {
            val doc = Jsoup.parse(html)
            val articleFolder = articleDir(articleID).apply { mkdirs() }

            rewriteAttribute(doc, "img", "src", articleID, articleFolder)
            rewriteAttribute(doc, "img", "data-src", articleID, articleFolder)
            rewriteAttribute(doc, "video", "src", articleID, articleFolder)
            rewriteAttribute(doc, "video", "poster", articleID, articleFolder)
            rewriteAttribute(doc, "source", "src", articleID, articleFolder)

            rewriteSrcset(doc, "img", articleID, articleFolder)
            rewriteSrcset(doc, "source", articleID, articleFolder)

            Result.success(doc.body().html())
        } catch (e: Throwable) {
            CapyLog.error("offline_assets", e)
            Result.failure(e)
        }
    }

    private suspend fun rewriteAttribute(
        doc: Document,
        tag: String,
        attr: String,
        articleID: String,
        articleFolder: File,
    ) {
        for (element in doc.select("$tag[$attr]")) {
            val original = element.attr(attr).trim()
            if (!original.startsWith("http://") && !original.startsWith("https://")) continue

            val hash = sha1(original)
            val storedName = downloadAsset(original, hash, articleFolder) ?: continue

            element.attr(attr, "$ASSET_HOST/offline/$articleID/$storedName")
        }
    }

    private suspend fun rewriteSrcset(
        doc: Document,
        tag: String,
        articleID: String,
        articleFolder: File,
    ) {
        for (element in doc.select("$tag[srcset]")) {
            val candidates = parseSrcset(element.attr("srcset"))
            val best = pickBestCandidate(candidates) ?: continue

            val url = best.url
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                element.attr("srcset", url + if (best.descriptor.isNotEmpty()) " ${best.descriptor}" else "")
                continue
            }

            val hash = sha1(url)
            val stored = downloadAsset(url, hash, articleFolder)
            val finalUrl = if (stored != null) "$ASSET_HOST/offline/$articleID/$stored" else url
            element.attr(
                "srcset",
                finalUrl + if (best.descriptor.isNotEmpty()) " ${best.descriptor}" else ""
            )
        }
    }

    private data class SrcsetCandidate(val url: String, val descriptor: String)

    /**
     * Spec-shaped srcset parser. Collects each URL as a run of non-whitespace —
     * so commas inside a query string stay with the URL — and only treats commas
     * as candidate separators after the URL ends (trailing commas) or in the
     * descriptor section.
     */
    private fun parseSrcset(input: String): List<SrcsetCandidate> {
        val results = mutableListOf<SrcsetCandidate>()
        var i = 0
        val length = input.length

        while (i < length) {
            while (i < length && (input[i].isWhitespace() || input[i] == ',')) i++
            if (i >= length) break

            val urlStart = i
            while (i < length && !input[i].isWhitespace()) i++
            var url = input.substring(urlStart, i)

            var trailingCommas = 0
            while (url.endsWith(",")) {
                url = url.dropLast(1)
                trailingCommas++
            }
            if (url.isEmpty()) continue

            if (trailingCommas > 0) {
                results.add(SrcsetCandidate(url, ""))
                continue
            }

            while (i < length && input[i].isWhitespace()) i++
            val descStart = i
            while (i < length && input[i] != ',') i++
            val descriptor = input.substring(descStart, i).trim()
            if (i < length) i++

            results.add(SrcsetCandidate(url, descriptor))
        }

        return results
    }

    /**
     * Pick the second-smallest candidate by descriptor value, falling back to the
     * smallest. Sites typically list small → large, so the second slot is usually
     * the "comfortable middle" — big enough to look right, small enough to avoid
     * downloading a 4K hero image for an article that'll render at 600 px wide.
     */
    private fun pickBestCandidate(candidates: List<SrcsetCandidate>): SrcsetCandidate? {
        if (candidates.isEmpty()) return null

        val byWidth = candidates
            .mapNotNull { c -> parseDescriptorValue(c.descriptor, 'w')?.let { c to it } }
            .sortedBy { it.second }
        if (byWidth.isNotEmpty()) {
            return (byWidth.getOrNull(1) ?: byWidth.first()).first
        }

        val byDensity = candidates
            .mapNotNull { c -> parseDescriptorValue(c.descriptor, 'x')?.let { c to it } }
            .sortedBy { it.second }
        if (byDensity.isNotEmpty()) {
            return (byDensity.getOrNull(1) ?: byDensity.first()).first
        }

        return candidates.getOrNull(1) ?: candidates.first()
    }

    private fun parseDescriptorValue(descriptor: String, suffix: Char): Double? {
        if (descriptor.isEmpty() || descriptor.last() != suffix) return null
        return descriptor.dropLast(1).toDoubleOrNull()
    }

    private suspend fun downloadAsset(url: String, hash: String, folder: File): String? {
        val target = File(folder, hash)
        val ctFile = File(folder, "$hash.ct")
        if (target.exists()) return hash

        return try {
            val response = client.newCall(Request.Builder().url(url).get().build()).await()

            if (!response.isSuccessful) {
                response.close()
                return null
            }

            val contentLength = response.header("Content-Length")?.toLongOrNull()
            if (contentLength != null && contentLength > MAX_ASSET_BYTES) {
                response.close()
                return null
            }

            val contentType = response.header("Content-Type")?.substringBefore(";")?.trim().orEmpty()

            response.body.byteStream().use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        total += read
                        if (total > MAX_ASSET_BYTES) {
                            output.close()
                            target.delete()
                            return null
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }

            if (contentType.isNotBlank()) ctFile.writeText(contentType)
            hash
        } catch (e: Throwable) {
            CapyLog.warn("offline_assets_fetch", mapOf("url" to url, "error" to (e.message ?: "")))
            null
        }
    }

    private fun sha1(input: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        return md.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun sanitize(name: String): String =
        name.replace(Regex("[^A-Za-z0-9._-]"), "_")
}
