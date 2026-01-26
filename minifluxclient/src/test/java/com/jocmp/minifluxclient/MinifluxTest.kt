package com.jocmp.minifluxclient

import okhttp3.OkHttpClient
import org.junit.Test
import kotlin.test.assertNotNull

class MinifluxTest {
    @Test
    fun `create Miniflux client`() {
        val client = OkHttpClient()
        val miniflux = Miniflux.create(
            client = client,
            baseURL = "https://miniflux.app/v1/"
        )

        assertNotNull(miniflux)
    }
}
