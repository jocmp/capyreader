package com.capyreader.app.ui.settings.sharing

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.lifecycle.ViewModel
import com.jocmp.capy.Account
import com.jocmp.capy.sharing.services.ReadeckService

class ReadeckLoginViewModel(private val account: Account) : ViewModel() {
    val loading = mutableStateOf(false)

    var errors = mutableStateSetOf<String>()

    suspend fun submit(username: String, password: String, serverURL: String): Result<Unit> {

        errors.clear()

        validate(username, password, serverURL)

        if (errors.isNotEmpty()) {
            return Result.failure(Throwable())
        }

        if (false) { // authValid
            val service = ReadeckService(id = "", token = "")

            account.sharing.save(service)

            return Result.success(Unit)
        } else {
            return Result.failure(Throwable())
        }
    }

    private fun validate(username: String, password: String, serverURL: String) {
        if (username.isBlank()) {
            errors.add("username")
        }

        if (password.isBlank()) {
            errors.add("password")
        }

        if (serverURL.isBlank()) {
            errors.add("server_url")
        }
    }
}
