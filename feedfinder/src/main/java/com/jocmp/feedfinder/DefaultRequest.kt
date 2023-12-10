package com.jocmp.feedfinder

import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


internal class DefaultRequest(
    private val client: HttpClient = buildClient()
) : Request {
    override suspend fun fetch(url: URL): Response {
        val request = HttpRequest.newBuilder(url.toURI())
            .GET()
            .build()

        val body = client.send(request, HttpResponse.BodyHandlers.ofString()).body()

        return Response(body = body)
    }

    companion object {
        fun buildClient(): HttpClient {
            return HttpClient
                .newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()
        }
    }
}
