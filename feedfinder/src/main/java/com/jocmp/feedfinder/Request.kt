package com.jocmp.feedfinder

import java.net.URL

interface Request {
    suspend fun fetch(url: URL): Response
}
