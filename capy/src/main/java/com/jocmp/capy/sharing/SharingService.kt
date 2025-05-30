package com.jocmp.capy.sharing

import com.jocmp.capy.sharing.services.ReadeckService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface SharingService {
    val serviceID: ServiceID

    fun share(url: String): Result<Unit>

    fun encode(): String {
        return when {
            this is ReadeckService -> Json.encodeToString<ReadeckService>(this)
            else -> throw IllegalArgumentException("Unsupported service type: ${this::class.java.simpleName}")
        }
    }

    companion object {
        /**
         * Marshals from a JSON blob using the service ID to avoid using a
         * namespaced Java class which may change over time
         */
        fun decode(value: String, serviceID: ServiceID): SharingService {
            return when (serviceID) {
                ServiceID.READECK -> Json.decodeFromString<ReadeckService>(value)
            }
        }
    }

    enum class ServiceID(val id: String) {
        READECK("readeck");
    }
}
