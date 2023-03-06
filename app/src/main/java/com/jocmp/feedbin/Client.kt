package com.jocmp.feedbin

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class FeedbinClient {
    val httpClient: HttpClient
        get() {
            return HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                    })
                }
            }
        }

    suspend fun authentication(username: String, password: String): Boolean {
        val response = httpClient.get("https://api.feedbin.com/v2/authentication.json") {
            basicAuth(username, password)
        }

        return response.status == HttpStatusCode.OK;
    }

}
