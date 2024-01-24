package com.jocmp.feedfinder

import java.io.File
import java.net.URL

internal class TestRequest(val sites: Map<String, String>) : Request {
    override suspend fun fetch(url: URL): Response {
        val bodyPath = sites[url.toString()]

        val body = if (bodyPath == null) {
            ""
        } else {
            File(bodyPath).readText()
        }

        return Response(url = url, body = body)
    }
}
