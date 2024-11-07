package com.jocmp.capy.accounts

import com.jocmp.capy.common.UnauthorizedError
import java.net.UnknownHostException

internal suspend fun <T> withErrorHandling(func: suspend () -> T?): Result<T> {
    return try {
        val result = func()

        if (result != null) {
            Result.success(result)
        } else {
            Result.failure(Throwable("Unexpected error"))
        }
    } catch (e: UnknownHostException) {
        return Result.failure(e)
    } catch (e: UnauthorizedError) {
        return Result.failure(e)
    }
}
