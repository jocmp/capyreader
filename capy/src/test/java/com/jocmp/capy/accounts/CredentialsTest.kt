package com.jocmp.capy.accounts

import com.jocmp.feedbinclient.Feedbin
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

class FeedbinCredentialsTest {
    val username = "test@example.com"
    val password = "its-a-secret-to-everybody"
    val credentials = FeedbinCredentials(username, password)
    lateinit var feedbin: Feedbin

    @BeforeTest
    fun setup() {
        feedbin = mockk<Feedbin>()
        mockkObject(Feedbin.Companion)
        every { Feedbin.create(any(), any()) }.returns(feedbin)
    }

    @Test
    fun verify_onSuccess_shouldReturnCredentials() = runTest {
        coEvery { feedbin.authentication(authentication = any()) }.returns(Response.success(null))

        val result = Credentials.verify(credentials).getOrNull()!!

        assertEquals(actual = result.username, expected = username)
        assertEquals(actual = result.secret, expected = password)
    }

    @Test
    fun verify_onError_shouldReturnFailure() = runTest {
        coEvery { feedbin.authentication(authentication = any()) }.returns(
            Response.error(
                401,
                "".toResponseBody()
            )
        )

        val result = Credentials.verify(credentials)

        assertTrue(result.isFailure)
    }
}

class ReaderCredentialsTest {
    @Test
    fun verify_onSuccess_shouldReturnCredentials() {

    }

    @Test
    fun verify_onError_shouldReturnFailure() {
    }
}
