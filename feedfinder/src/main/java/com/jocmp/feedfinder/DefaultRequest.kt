package com.jocmp.feedfinder

import java.net.HttpURLConnection
import java.net.URL


internal class DefaultRequest: Request {
    override suspend fun fetch(url: URL): Response {
        val parsedURL = URL("https", url.host, url.port, url.file)
        val connection = parsedURL.openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", USER_AGENT)

        val body = connection.inputStream.bufferedReader().readText()

        return Response(url = parsedURL, body = body)
    }

    companion object {
        const val USER_AGENT = "Basil/1.0"
    }
}
