package com.jocmp.capy.offline

import com.jocmp.capy.articles.MercuryParser
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.common.withIOContext
import com.jocmp.capy.db.Database
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.net.URL
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val MIN_BODY_TEXT_LENGTH = 32

class OfflineDownloader(
    private val database: Database,
    private val mercuryParser: MercuryParser,
    private val httpClient: OkHttpClient,
    private val assetStore: OfflineAssetStore,
    private val perAssetMaxBytes: Long,
    private val retryAfterSeconds: Long,
) {
    suspend fun run(maxArticles: Int, cacheLimitBytes: Long, cacheBufferBytes: Long): Int = withIOContext {
        val now = nowUTC().toEpochSecond()
        val retryCutoff = now - retryAfterSeconds

        val candidates = database.articlesQueries
            .findOfflineCandidates(retryCutoff = retryCutoff, limit = maxArticles.toLong())
            .executeAsList()

        var processed = 0
        for (candidate in candidates) {
            coroutineContext.ensureActive()
            val url = candidate.url ?: continue
            val downloaded = downloadArticle(candidate.id, url, candidate.content_html.orEmpty())
            if (downloaded) processed += 1
            enforceCacheLimit(cacheLimitBytes, cacheBufferBytes)
        }
        processed
    }

    private suspend fun downloadArticle(
        articleID: String,
        articleUrl: String,
        feedContentHtml: String,
    ): Boolean {
        val parsed = runCatching { URL(articleUrl) }.getOrNull() ?: run {
            markAttempted(articleID)
            return false
        }

        val mercuryHtml = mercuryParser.fetch(parsed).getOrElse {
            CapyLog.error("offline_mercury", it)
            markAttempted(articleID)
            return false
        }

        // Mercury can return an empty wrapper for SPA-rendered pages (Mastodon, etc.).
        // Prefer the feed-supplied body when Mercury didn't extract anything useful.
        val html = if (parseableBody(mercuryHtml)) mercuryHtml else feedContentHtml

        val (absolutized, mediaUrls) = absolutize(html, articleUrl)
        val enclosureUrls = database.enclosuresQueries
            .findUrlsByArticleID(articleID)
            .executeAsList()
        val allUrls = (mediaUrls + enclosureUrls).distinct()

        for (mediaUrl in allUrls) {
            coroutineContext.ensureActive()
            cacheAsset(articleID, mediaUrl)
        }

        database.articlesQueries.setOfflineContent(
            html = absolutized,
            cachedAt = nowUTC().toEpochSecond(),
            articleID = articleID,
        )
        return true
    }

    private fun markAttempted(articleID: String) {
        database.articlesQueries.setOfflineAttempted(
            attemptedAt = nowUTC().toEpochSecond(),
            articleID = articleID,
        )
    }

    private suspend fun cacheAsset(articleID: String, remoteUrl: String) {
        val existing = database.offline_assetsQueries
            .findByUrl(articleID = articleID, remoteURL = remoteUrl)
            .executeAsOneOrNull()
        if (existing != null) return

        val target = assetStore.targetFile(articleID, remoteUrl)
        val mime = runCatching { streamAssetToFile(remoteUrl, target) }.getOrElse {
            target.delete()
            CapyLog.error("offline_asset", it)
            return
        }

        database.offline_assetsQueries.upsert(
            articleID = articleID,
            remoteURL = remoteUrl,
            localPath = target.absolutePath,
            mimeType = mime,
            sizeBytes = target.length(),
            createdAt = nowUTC().toEpochSecond(),
        )
    }

    private fun enforceCacheLimit(cacheLimitBytes: Long, cacheBufferBytes: Long) {
        if (cacheLimitBytes <= 0) return
        val cap = (cacheLimitBytes - cacheBufferBytes).coerceAtLeast(0)
        var total: Long = database.offline_assetsQueries.totalSizeBytes().executeAsOne()
        if (total <= cap) return

        val oldest = database.offline_assetsQueries.oldestCachedArticles().executeAsList()
        for (row in oldest) {
            if (total <= cap) break
            val freed: Long = (row.total_bytes as? Number)?.toLong() ?: 0L
            assetStore.deleteArticle(row.id)
            database.offline_assetsQueries.deleteByArticle(articleID = row.id)
            database.articlesQueries.clearOfflineContent(articleID = row.id)
            total -= freed
        }
    }

    private fun absolutize(html: String, articleUrl: String): Pair<String, List<String>> {
        val doc = Jsoup.parseBodyFragment(html, articleUrl)
        val urls = linkedSetOf<String>()

        // Collapse responsive images to a single src so the WebView fetches one
        // known URL we can intercept, rather than picking an unknown srcset variant.
        doc.select("img[srcset], source[srcset]").forEach { el ->
            pickSrcsetUrl(el.attr("srcset"))?.let { url ->
                el.attr("src", url)
            }
            el.removeAttr("srcset")
            el.removeAttr("sizes")
        }

        doc.select("img[src], video[src], source[src]").forEach { el ->
            val rawSrc = stripTrailingDescriptor(el.attr("src"))
            if (rawSrc.isEmpty()) return@forEach
            el.attr("src", rawSrc)
            val abs = el.absUrl("src")
            if (abs.startsWith("http")) {
                el.attr("src", abs)
                urls.add(abs)
            }
        }

        return doc.body().html() to urls.toList()
    }

    private fun parseableBody(html: String): Boolean {
        if (html.isBlank()) return false
        val text = Jsoup.parseBodyFragment(html).body().text().trim()
        return text.length >= MIN_BODY_TEXT_LENGTH
    }

    private fun pickSrcsetUrl(srcset: String): String? {
        if (srcset.isBlank()) return null
        // srcset format: "url1 376w, url2 768w" — take the first entry's URL.
        val firstEntry = srcset.substringBefore(',').trim()
        val url = firstEntry.split(Regex("\\s+")).firstOrNull()?.trim().orEmpty()
        return url.ifBlank { null }
    }

    private fun stripTrailingDescriptor(value: String): String {
        val trimmed = value.trim()
        // A bare URL has no spaces. If we see one, it's a leaked srcset descriptor.
        val firstSpace = trimmed.indexOf(' ')
        return if (firstSpace >= 0) trimmed.substring(0, firstSpace) else trimmed
    }

    private suspend fun streamAssetToFile(url: String, target: File): String? =
        suspendCancellableCoroutine { cont ->
            val call = httpClient.newCall(Request.Builder().url(url).build())
            cont.invokeOnCancellation { runCatching { call.cancel() } }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { resp ->
                        if (!resp.isSuccessful) {
                            cont.resumeWithException(IOException("HTTP ${resp.code}"))
                            return
                        }
                        val body = resp.body
                        if (body.contentLength() > perAssetMaxBytes) {
                            cont.resumeWithException(IOException("asset too large"))
                            return
                        }
                        val mime = resp.header("Content-Type")?.substringBefore(';')?.trim()
                        try {
                            body.byteStream().use { input ->
                                target.outputStream().use { output ->
                                    val buffer = ByteArray(64 * 1024)
                                    var total = 0L
                                    while (true) {
                                        val read = input.read(buffer)
                                        if (read < 0) break
                                        total += read
                                        if (total > perAssetMaxBytes) {
                                            cont.resumeWithException(IOException("asset exceeded cap during stream"))
                                            return
                                        }
                                        output.write(buffer, 0, read)
                                    }
                                }
                            }
                            cont.resume(mime)
                        } catch (e: IOException) {
                            cont.resumeWithException(e)
                        }
                    }
                }
            })
        }
}
