package com.jocmp.capy.offline

import java.io.File
import java.security.MessageDigest

/**
 * Stores cached media (images, video sources) for offline articles on disk.
 *
 * Layout: `<rootDir>/<articleId>/<sha256(remoteUrl)>.bin`
 */
class OfflineAssetStore(private val rootDir: File) {
    init {
        rootDir.mkdirs()
    }

    fun targetFile(articleID: String, remoteUrl: String): File {
        val dir = File(rootDir, articleID).apply { mkdirs() }
        return File(dir, fileName(remoteUrl))
    }

    fun fileFor(articleID: String, remoteUrl: String): File =
        File(File(rootDir, articleID), fileName(remoteUrl))

    fun deleteArticle(articleID: String) {
        File(rootDir, articleID).deleteRecursively()
    }

    private fun fileName(remoteUrl: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(remoteUrl.toByteArray())
        return digest.joinToString("") { "%02x".format(it) } + ".bin"
    }
}
