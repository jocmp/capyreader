package com.jocmp.feedfinder

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DefaultRequestTest {
    private lateinit var mockServer: MockWebServer
    private lateinit var defaultRequest: DefaultRequest

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
        defaultRequest = DefaultRequest()
    }

    @After
    fun tearDown() {
        mockServer.close()
    }

    @Test
    fun `should make request without auth header without user info`() = runTest {
        mockServer.enqueue(MockResponse(body = "test content"))

        val url = mockServer.url("/").toUrl()
        val response = defaultRequest.fetch(url)

        assertEquals(url, response.url)
        assertEquals("test content", response.body)

        val recordedRequest = mockServer.takeRequest()
        assertNull(recordedRequest.headers["Authorization"])
    }

    @Test
    fun `should add basic auth header with user info`() = runTest {
        mockServer.enqueue(MockResponse(body = "authenticated content"))

        val url = URI("http://username:password@${mockServer.hostName}:${mockServer.port}/").toURL()
        val response = defaultRequest.fetch(url)

        assertEquals(url, response.url)
        assertEquals("authenticated content", response.body)

        val recordedRequest = mockServer.takeRequest()
        val authHeader = recordedRequest.headers["Authorization"]
        assertEquals("Basic dXNlcm5hbWU6cGFzc3dvcmQ=", authHeader)
    }
}