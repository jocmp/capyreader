package com.jocmp.capy.sharing

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.db.Database
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.sharing.services.NullService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SharingRecords(private val database: Database) {
    fun save(service: SharingService) {
        database.transactionWithErrorHandling {
            queries.upsert(service_id = service.serviceID, settings_json = service.encode())
        }
    }

    fun deactivate(serviceID: SharingService.ServiceID) {
        queries.delete(service_id = serviceID)
    }

    fun active(): Flow<List<SharingService>> {
        return queries
            .active(mapper = ::mapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.filter { it !is NullService } }
    }

    private fun mapper(id: SharingService.ServiceID, json: String): SharingService {
        return try {
            SharingService.decode(json, serviceID = id)
        } catch (e: Throwable) {
            CapyLog.error("sharing", e)

            NullService()
        }
    }

    private val queries
        get() = database.sharing_servicesQueries
}
