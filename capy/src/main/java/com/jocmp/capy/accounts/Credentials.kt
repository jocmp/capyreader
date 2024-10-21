package com.jocmp.capy.accounts

interface Credentials {
    val username: String
    val secret: String
    val source: Source

    suspend fun verify(): Result<Credentials>
}
