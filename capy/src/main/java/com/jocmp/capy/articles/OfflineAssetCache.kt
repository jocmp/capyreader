package com.jocmp.capy.articles

import com.jocmp.capy.SyncStatus
import com.jocmp.capy.db.Database
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.persistence.SyncStatusRecords
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.net.URL
import java.security.MessageDigest

class OfflineAssetCache(
    private val database: Database,
    private val mercuryParser: MercuryParser,
    private val httpClient: OkHttpClient,
    private val storageDir: File,
    private val syncStatusRecords: SyncStatusRecords,
    private val parallelism: Int = DEFAULT_PARALLELISM,
) {
    /**
     * Insert a CACHE sync-status row for every desired article that isn't
     * already on disk. Cheap, idempotent, durable — survives crashes so
     * partially-finished caching can resume on the next [drain].
     */
    fun enqueueCandidates(limit: Int) {
        val desired = computeDesired(limit).keys
        if (desired.isEmpty()) return

        val alreadyCached = database.articlesQueries.findCachedArticleIDs()
            .executeAsList().toSet()
        val toQueue = (desired - alreadyCached).toList()
        if (toQueue.isEmpty()) return

        syncStatusRecords.insertStatuses(
            articleIDs = toQueue,
            key = SyncStatus.Key.CACHE,
            flag = true,
        )
    }

    /**
     * Drain pending CACHE sync-status rows: for each, fetch via Mercury,
     * download media, write `offline_html`, then remove the row. Rows for
     * articles that fail processing remain so the next drain retries.
     */
    suspend fun drain() {
        withContext(Dispatchers.IO) {
            runCatching {
                storageDir.mkdirs()

                val pending = syncStatusRecords.pendingArticleIDs(SyncStatus.Key.CACHE)
                if (pending.isEmpty()) return@runCatching

                val urls = database.articlesQueries.findURLsByIDs(pending)
                    .executeAsList()
                    .associate { it.id to it.url }
                val alreadyCached = database.articlesQueries.findCachedArticleIDs()
                    .executeAsList().toSet()

                pending.forEach { id ->
                    if (id in alreadyCached) {
                        syncStatusRecords.deletePending(listOf(id), SyncStatus.Key.CACHE)
                        return@forEach
                    }
                    val ok = cacheArticle(id, urls[id])
                    if (ok) {
                        syncStatusRecords.deletePending(listOf(id), SyncStatus.Key.CACHE)
                    }
                }
            }.onFailure { e -> CapyLog.error("offline_cache_drain", e) }
        }
    }

    /**
     * Clear `offline_html` and delete asset directories for articles that
     * are cached but no longer in the desired set.
     */
    fun evict(limit: Int) {
        val desired = computeDesired(limit).keys
        val cached = database.articlesQueries.findCachedArticleIDs()
            .executeAsList().toSet()
        val toRemove = (cached - desired).toList()
        if (toRemove.isEmpty()) return

        toRemove.forEach { File(storageDir, it).deleteRecursively() }
        database.articlesQueries.clearOfflineHtmlForIDs(toRemove)
        syncStatusRecords.deletePending(toRemove, SyncStatus.Key.CACHE)
    }

    internal fun computeDesired(limit: Int): Map<String, String?> {
        if (limit <= 0) return emptyMap()

        val desired = linkedMapOf<String, String?>()
        database.articlesQueries.findOfflineCandidates(limit.toLong())
            .executeAsList()
            .forEach { row -> desired[row.id] = row.url }
        return desired
    }

    private suspend fun cacheArticle(id: String, articleUrl: String?): Boolean {
        if (articleUrl.isNullOrBlank()) return false

        val url = runCatching { URL(articleUrl) }.getOrNull() ?: return false

        val rendered = mercuryParser.fetch(url).getOrNull() ?: run {
            CapyLog.info(
                "offline_cache_fetch_failed",
                mapOf("article_id" to id),
            )
            return false
        }

        val articleDir = File(storageDir, id)
        articleDir.mkdirs()

        val rewritten = runCatching {
            downloadAndRewrite(rendered, articleDir, baseUrl = url.toString(), articleID = id)
        }.onFailure { e ->
            CapyLog.error("offline_cache_rewrite", e)
            articleDir.deleteRecursively()
        }.getOrNull() ?: return false

        database.articlesQueries.setOfflineHtml(html = rewritten, articleID = id)
        return true
    }

    private suspend fun downloadAndRewrite(
        html: String,
        articleDir: File,
        baseUrl: String,
        articleID: String,
    ): String {
        val doc = Jsoup.parse(html, baseUrl)
        narrowSrcsets(doc)
        val urls = collectMediaURLs(doc)
        if (urls.isEmpty()) return doc.body().html()

        val semaphore = Semaphore(parallelism)
        val results = coroutineScope {
            urls.map { url ->
                async {
                    semaphore.withPermit { url to downloadAsset(url, articleDir) }
                }
            }.awaitAll()
        }.toMap()

        rewriteMedia(doc, results, articleID)
        return doc.body().html()
    }

    private fun collectMediaURLs(doc: Document): Set<String> {
        val urls = linkedSetOf<String>()
        MEDIA_SRC_SELECTORS.forEach { selector ->
            doc.select(selector).forEach { el ->
                el.absUrl("src").takeIf { it.isNotBlank() }?.let(urls::add)
            }
        }
        doc.select("video[poster]").forEach { el ->
            el.absUrl("poster").takeIf { it.isNotBlank() }?.let(urls::add)
        }
        doc.select("[srcset]").forEach { el ->
            el.attr("srcset").trim().takeIf { it.isNotBlank() }?.let(urls::add)
        }
        return urls
    }

    internal fun narrowSrcsets(doc: Document) {
        doc.select("[srcset]").forEach { el ->
            val best = bestSrcsetUrl(el.attr("srcset"), doc.baseUri()) ?: return@forEach
            el.attr("srcset", best)
        }
    }

    internal fun bestSrcsetUrl(srcset: String, baseUri: String): String? {
        val candidates = srcset.split(",").mapNotNull { entry ->
            val parts = entry.trim().split(Regex("\\s+"), limit = 2)
            val raw = parts.firstOrNull()?.trim().orEmpty()
            if (raw.isBlank()) return@mapNotNull null
            val descriptor = parts.getOrNull(1)?.trim().orEmpty()
            absoluteUrl(raw, baseUri) to descriptorScore(descriptor)
        }
        return candidates.maxByOrNull { it.second }?.first
    }

    private fun descriptorScore(descriptor: String): Double {
        if (descriptor.isBlank()) return 1.0
        return when {
            descriptor.endsWith("w") -> descriptor.dropLast(1).toDoubleOrNull() ?: 1.0
            descriptor.endsWith("x") -> 1_000.0 * (descriptor.dropLast(1).toDoubleOrNull() ?: 1.0)
            else -> 1.0
        }
    }

    private fun absoluteUrl(value: String, baseUri: String): String =
        runCatching { URL(URL(baseUri), value).toString() }.getOrDefault(value)

    private fun downloadAsset(url: String, articleDir: File): String? = runCatching {
        val filename = filenameFor(url)
        val target = File(articleDir, filename)
        if (target.exists()) return@runCatching filename

        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@runCatching null
            val body = response.body ?: return@runCatching null
            target.outputStream().use { out ->
                body.byteStream().use { input -> input.copyTo(out) }
            }
        }
        filename
    }.onFailure { e ->
        CapyLog.info(
            "offline_cache_asset_failed",
            mapOf("url" to url, "error" to e.message),
        )
    }.getOrNull()

    private fun filenameFor(url: String): String {
        val hash = sha1(url)
        val ext = extensionFor(url)
        return if (ext.isNullOrBlank()) hash else "$hash.$ext"
    }

    private fun extensionFor(url: String): String? {
        val path = runCatching { URL(url).path }.getOrNull() ?: return null
        val dot = path.lastIndexOf('.')
        if (dot < 0) return null
        val ext = path.substring(dot + 1)
        if (ext.length > 5 || ext.contains('/')) return null
        return ext.lowercase()
    }

    private fun sha1(value: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }

    private fun rewriteMedia(
        doc: Document,
        mapping: Map<String, String?>,
        articleID: String,
    ) {
        MEDIA_SRC_SELECTORS.forEach { selector ->
            doc.select(selector).forEach { el ->
                val abs = el.absUrl("src")
                mapping[abs]?.let { el.attr("src", localPath(articleID, it)) }
            }
        }
        doc.select("video[poster]").forEach { el ->
            val abs = el.absUrl("poster")
            mapping[abs]?.let { el.attr("poster", localPath(articleID, it)) }
        }
        doc.select("[srcset]").forEach { el ->
            val abs = el.attr("srcset")
            mapping[abs]?.let { el.attr("srcset", localPath(articleID, it)) }
        }
    }

    private fun localPath(articleID: String, filename: String): String =
        "$OFFLINE_ASSET_BASE/$articleID/$filename"

    companion object {
        const val OFFLINE_ASSET_BASE = "https://appassets.androidplatform.net/offline-assets"
        const val DEFAULT_PARALLELISM = 2

        private val MEDIA_SRC_SELECTORS = listOf(
            "img[src]",
            "source[src]",
            "video[src]",
            "audio[src]",
        )
    }
}
