package com.jocmp.rssparser.internal

import java.io.InputStream
import java.nio.charset.Charset

internal class ParserInput(private val bytes: ByteArray, val charset: Charset?) {
    fun inputStream() = bytes.inputStream()

    companion object {
        fun from(inputStream: InputStream, charset: Charset?) =
            ParserInput(inputStream.readBytes(), charset = charset)
    }
}
