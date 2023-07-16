package com.jocmp.feedbinclient

interface CredentialsManager {
    fun fetch(): Account
    fun save(account: Account)
    val hasAccount: Boolean
}