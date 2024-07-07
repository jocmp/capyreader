package com.capyreader.common

import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
object MD5 {
    fun from(value: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(value.toByteArray())
        return digest.toHexString()
    }
}
