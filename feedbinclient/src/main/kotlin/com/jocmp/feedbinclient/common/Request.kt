package com.jocmp.feedbinclient.common

suspend fun <T> request(request: suspend () -> T): Result<T> {
    return try {
        Result.success(request.invoke())
    } catch (exception: Throwable) {
        Result.failure(exception)
    }
}