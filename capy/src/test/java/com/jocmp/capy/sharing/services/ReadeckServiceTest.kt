package com.jocmp.capy.sharing.services

import com.jocmp.capy.sharing.SharingService
import com.jocmp.capy.sharing.SharingService.ServiceID
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadeckServiceTest {
    @Test
    fun serialization() {
        val service = ReadeckService(
            username = "my-username",
            password = "password123",
            serverURL = "http://example.com"
        )

        val encoded = service.encode()

        val result = SharingService.decode(encoded, ServiceID.READECK)

        assertEquals(expected = service, actual = result)
    }
}
