package com.jocmp.capy.sharing.services

import com.jocmp.capy.sharing.SharingService
import kotlinx.serialization.Serializable

@Serializable
data class ReadeckService(
    val id: String = "",
    val token: String = ""
) : SharingService {
    override val serviceID = SharingService.ServiceID.READECK

    override fun share(url: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}
