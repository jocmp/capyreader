package com.jocmp.feedbinclient

import okhttp3.Interceptor

class BasicAuthInterceptor(private val credentials: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()

        if (request.headers("Authorization").isNullOrEmpty()) {
            val authenticatedRequest =
                request.newBuilder().header("Authorization", credentials()).build()
            return chain.proceed(authenticatedRequest)
        }

        return chain.proceed(request)
    }
}
