package com.jocmp.capy.sharing

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.sharing.SharingService.ServiceID
import com.jocmp.capy.sharing.services.ReadeckService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SharingRecordsTest {
    private lateinit var records: SharingRecords

    @BeforeTest
    fun setup() {
        records = SharingRecords(InMemoryDatabaseProvider.build("777"))
    }

    @Test
    fun save() = runTest {
        val service = ReadeckService("username", "password", "example.com")
        records.save(service)

        val result = records.active().first().first()

        assertEquals(expected = service, actual = result)
    }

    @Test
    fun deactivate() = runTest {
        val service = ReadeckService("username", "password", "example.com")
        records.save(service)

        var active = records.active().first()
        assertEquals(expected = 1, actual = active.size)

        records.deactivate(ServiceID.READECK)

        active = records.active().first()
        assertEquals(expected = 0, actual = active.size)
    }
}
