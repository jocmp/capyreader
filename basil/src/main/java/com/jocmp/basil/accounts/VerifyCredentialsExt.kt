package com.jocmp.basil.accounts

import com.jocmp.basil.Account
import com.jocmp.feedbinclient.Feedbin

suspend fun verifyCredentials(username: String, password: String) =
    Feedbin.verifyCredentials(username = username, password = password)
