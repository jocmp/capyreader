package com.jocmp.capy.sharing.services

import com.jocmp.capy.sharing.SharingService
import com.jocmp.capy.sharing.SharingService.ServiceID

internal class NullService(
    override val serviceID: ServiceID = ServiceID.READECK,
) : SharingService {
    override fun share(url: String): Result<Unit> {
        return Result.success(Unit)
    }
}
