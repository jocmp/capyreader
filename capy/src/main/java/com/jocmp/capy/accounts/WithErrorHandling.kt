package com.jocmp.capy.accounts

import com.jocmp.capy.logging.CapyLog
import retrofit2.HttpException

class ValidationError(override val message: String? = null): Throwable(message)

internal suspend fun <T> withErrorHandling(func: suspend () -> T?): Result<T> {
    return try {
        val result = func()

        if (result != null) {
            Result.success(result)
        } else {
            val error = Throwable("Unexpected error")
            CapyLog.error("with_error_handling", error)
            Result.failure(error)
        }
    } catch (e: HttpException) {
        CapyLog.error("with_error_handling", e, httpData(e))
        Result.failure(e)
    } catch (e: Throwable) {
        CapyLog.error("with_error_handling", e)
        Result.failure(e)
    }
}

private fun httpData(e: HttpException): Map<String, Any?> = mapOf(
    "code" to e.code(),
    "body" to runCatching { e.response()?.errorBody()?.string() }.getOrNull(),
)
