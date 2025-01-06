package com.jocmp.capy.accounts.reader

import com.jocmp.capy.accounts.Source
import com.jocmp.readerclient.GoogleReader
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

internal class ReaderCredentialsTest {
    private val username = "alice"
    private val password = "its-a-secret-to-everybody"
    private val url = "http://selfhosted.example.com/greader.php"
    private val clientCertAlias = "alice@homelab"
    private val credentials = ReaderCredentials(
        username = username,
        secret = password,
        url = url,
        clientCertAlias = clientCertAlias,
        source = Source.FRESHRSS
    )
    lateinit var googleReader: GoogleReader
    private val auth = "alice/8e6845e089457af25303abc6f53356eb60bdb5f8"

    private val successResponse = """
       SID=$auth
       Auth=$auth
    """.trimIndent()

    @BeforeTest
    fun setup() {
        googleReader = mockk<GoogleReader>()
        mockkObject(GoogleReader.Companion)
        every { GoogleReader.create(any(), url) }.returns(googleReader)
    }

    @Test
    fun verify_onSuccess_shouldReturnCredentials() = runTest {
        coEvery {
            googleReader.clientLogin(
                email = any(),
                password = any()
            )
        }.returns(Response.success(successResponse))

        val result = credentials.verify(mockk()).getOrNull()!!

        assertEquals(actual = result.username, expected = username)
        assertEquals(actual = result.secret, expected = auth)
    }

    @Test
    fun verify_onError_unauthorized() = runTest {
        coEvery { googleReader.clientLogin(email = any(), password = any()) }.returns(
            Response.success(
                200,
                "Unauthorized!"
            )
        )

        val result = credentials.verify(mockk())

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()!!.message, "Unauthorized!")
    }

    @Test
    fun verify_onError_shouldReturnFailure() = runTest {
        coEvery { googleReader.clientLogin(email = any(), password = any()) }.returns(
            Response.error(
                401,
                "".toResponseBody()
            )
        )

        val result = credentials.verify(mockk())

        assertTrue(result.isFailure)
    }
}
