package com.jocmp.feedbinclient

import com.jocmp.feedbinclient.api.FeedbinClient
import com.jocmp.feedbinclient.common.request
import okhttp3.Credentials

class Authentication(private val client: FeedbinClient) {
    suspend fun login(username: String, password: String): Boolean {
        val credentials = Credentials.basic(username, password)

        return request { client.authentication(credentials = credentials) }.fold(
            onSuccess = { it.isSuccessful },
            onFailure = { false }
        )
    }
}