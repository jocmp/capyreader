package com.jocmp.feedfinder

import java.net.URL

internal interface Request {
    suspend fun fetch(url: URL): Response
}
