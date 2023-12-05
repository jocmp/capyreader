package com.jocmp.feedfinder

import java.net.URL

class TestRequest(private val response: Response): Request {
    override suspend fun fetch(url: URL): Response {
        return response
    }
}
