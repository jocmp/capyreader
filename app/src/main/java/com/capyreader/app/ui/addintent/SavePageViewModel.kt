package com.capyreader.app.ui.addintent

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.Account
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.withUIContext
import okio.IOException

class SavePageViewModel(
    private val account: Account,
) : ViewModel() {
    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun savePage(url: String, onComplete: () -> Unit) {
        viewModelScope.launchIO {
            loading = true
            error = null

            account.createPage(url).fold(
                onSuccess = {
                    loading = false
                    withUIContext { onComplete() }
                },
                onFailure = { exception ->
                    loading = false
                    error = if (exception is IOException) {
                        "network"
                    } else {
                        "generic"
                    }
                }
            )
        }
    }
}
