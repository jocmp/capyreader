package com.jocmp.capy.accounts

import com.jocmp.feedbinclient.Feedbin

suspend fun verifyCredentials(username: String, password: String) =
    Feedbin.verifyCredentials(username = username, password = password)
