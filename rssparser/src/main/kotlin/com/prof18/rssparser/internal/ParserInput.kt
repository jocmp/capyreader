package com.prof18.rssparser.internal

import java.io.InputStream

internal class ParserInput(private val bytes: ByteArray) {
    fun inputStream() = bytes.inputStream()

    companion object {
        fun from(inputStream: InputStream) = ParserInput(inputStream.readBytes())
    }
}
