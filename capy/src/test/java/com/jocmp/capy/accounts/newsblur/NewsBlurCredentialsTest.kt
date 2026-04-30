/*
 * Created by Josiah Campbell.
 */
package com.jocmp.capy.accounts.newsblur

import com.jocmp.capy.accounts.Credentials
import com.jocmp.capy.accounts.Source
import com.jocmp.newsblurclient.NewsBlur
import com.jocmp.newsblurclient.NewsBlurLoginErrors
import com.jocmp.newsblurclient.NewsBlurLoginResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class NewsBlurCredentialsTest {
    private val username = "samuelclay"
    private val password = "its-a-secret-to-everybody"
    private lateinit var newsblur: NewsBlur

    @BeforeTest
    fun setup() {
        newsblur = mockk<NewsBlur>()
        mockkObject(NewsBlur.Companion)
        every { NewsBlur.create(any(), any()) }.returns(newsblur)
    }

    @Test
    fun verify_onSuccess_shouldReturnCredentials() = runTest {
        coEvery { newsblur.login(username = any(), password = any()) }.returns(
            Response.success(NewsBlurLoginResponse(code = 1))
        )

        val credentials = NewsBlurCredentials(username = username, secret = password)

        val result = credentials.verify().getOrNull()!!

        assertEquals(actual = result.username, expected = username)
        assertEquals(actual = result.secret, expected = password)
        assertEquals(actual = result.source, expected = Source.NEWSBLUR)
    }

    @Test
    fun verify_onAuthFailure_shouldReturnFailure() = runTest {
        coEvery { newsblur.login(username = any(), password = any()) }.returns(
            Response.success(
                NewsBlurLoginResponse(
                    code = -1,
                    errors = NewsBlurLoginErrors(others = listOf("Bad password")),
                )
            )
        )

        val credentials = NewsBlurCredentials(username = username, secret = password)

        val result = credentials.verify()

        assertTrue(result.isFailure)
    }

    @Test
    fun verify_onHTTPError_shouldReturnFailure() = runTest {
        coEvery { newsblur.login(username = any(), password = any()) }.returns(
            Response.error(500, "".toResponseBody())
        )

        val credentials = NewsBlurCredentials(username = username, secret = password)

        val result = credentials.verify()

        assertTrue(result.isFailure)
    }

    @Test
    fun defaultURL_shouldBeNewsBlurDotCom() {
        val credentials = NewsBlurCredentials(username = username, secret = password)

        assertEquals(actual = credentials.url, expected = "https://newsblur.com/")
    }

    @Test
    fun credentialsFromFactory_shouldDefaultToNewsBlurURL_whenURLBlank() {
        val credentials = Credentials.from(
            source = Source.NEWSBLUR,
            username = username,
            password = password,
            url = "",
        )

        assertEquals(actual = credentials.url, expected = "https://newsblur.com/")
        assertEquals(actual = credentials.source, expected = Source.NEWSBLUR)
    }

    @Test
    fun credentialsFromFactory_shouldNormalizeProvidedURL() {
        val credentials = Credentials.from(
            source = Source.NEWSBLUR,
            username = username,
            password = password,
            url = "https://example.newsblur.com",
        )

        assertEquals(actual = credentials.url, expected = "https://example.newsblur.com")
    }
}
