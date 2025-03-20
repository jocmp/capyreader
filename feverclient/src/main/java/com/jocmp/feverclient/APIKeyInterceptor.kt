package com.jocmp.feverclient

import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request

class APIKeyInterceptor(
    private val apiKey: () -> String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()

        if (request.method() == "POST") {
            return chain.proceed(appendAPIKeyToBody(request))
        } else {
            return chain.proceed(request)
        }
    }

    private fun appendAPIKeyToBody(request: Request): Request {
        val originalBody = request.body()

        val formBuilder = FormBody.Builder()

        if (originalBody is FormBody) {
            for (i in 0 until originalBody.size()) {
                formBuilder.add(originalBody.name(i), originalBody.value(i))
            }
        }

        formBuilder.add("api_key", apiKey())

        return request.newBuilder()
            .method(request.method(), formBuilder.build())
            .build()
    }
}
