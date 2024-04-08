package com.jocmp.basil.common

import retrofit2.Response

internal fun <T> withResult(response: Response<T>, handler: (result: T) -> Unit) {
    val result = response.body()

    if (!response.isSuccessful || result == null) {
        return
    }

    handler(result)
}
