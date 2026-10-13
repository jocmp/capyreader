package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.logging.buildDebugInfo
import com.capyreader.app.refresher.RefreshLog
import com.capyreader.app.refresher.RefreshLogRow
import com.capyreader.app.refresher.RefreshReport
import com.capyreader.app.refresher.RefreshStatus
import com.capyreader.app.refresher.RefreshStatusReader
import com.capyreader.app.refresher.refreshLogRows
import com.jocmp.capy.Account
import kotlinx.coroutines.launch

class RefreshLogViewModel(
    private val account: Account,
    private val refreshLog: RefreshLog,
    private val statusReader: RefreshStatusReader,
) : ViewModel() {
    var status by mutableStateOf<RefreshStatus?>(null)
        private set

    var rows by mutableStateOf<List<RefreshLogRow>>(emptyList())
        private set

    val report: String?
        get() {
            val status = status ?: return null

            return RefreshReport.build(
                status = status,
                rows = rows,
                debugInfo = buildDebugInfo(account.source),
            )
        }

    fun load() {
        viewModelScope.launch {
            status = statusReader.read()
            rows = refreshLogRows(refreshLog.entries())
        }
    }
}
