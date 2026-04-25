package com.jocmp.rssparser.internal

import okio.BufferedSource
import okio.ByteString.Companion.decodeHex
import java.io.IOException

private val NOT_FEED_SIGNATURES = listOf(
    "89504e47".decodeHex(),     // PNG
    "ffd8ff".decodeHex(),       // JPEG
    "474946383761".decodeHex(), // GIF87a
    "474946383961".decodeHex(), // GIF89a
    "25504446".decodeHex(),     // PDF
    "504b0304".decodeHex(),     // ZIP
    "1f8b08".decodeHex(),       // GZIP
    "49443303".decodeHex(),     // MP3 (ID3v2.3)
)

private const val PREFIX_BYTES = 8L

/**
 * Ported from
 * https://github.com/Ranchero-Software/NetNewsWire/blob/d7c20f5fab7f44e5fd4ab5021df1687ecc31c4b2/Modules/Account/Sources/Account/LocalAccount/LocalAccountRefresher.swift#L291-L294
 */
internal fun BufferedSource.isDefinitelyNotFeed(): Boolean {
    val peek = peek()
    val available = try {
        if (peek.request(PREFIX_BYTES)) PREFIX_BYTES else peek.buffer.size
    } catch (_: IOException) {
        return false
    }
    if (available <= 0) return false
    val prefix = peek.readByteString(available)
    return NOT_FEED_SIGNATURES.any { prefix.startsWith(it) }
}
