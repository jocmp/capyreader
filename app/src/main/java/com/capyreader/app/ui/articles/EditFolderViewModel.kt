package com.capyreader.app.ui.articles

import androidx.lifecycle.ViewModel
import com.jocmp.capy.Account
import com.jocmp.capy.EditFolderFormEntry
import com.jocmp.capy.Folder
import com.jocmp.capy.common.withIOContext

class EditFolderViewModel(
    private val account: Account,
) : ViewModel() {

    suspend fun submit(
        form: EditFolderFormEntry,
    ): Result<Folder> {
        return withIOContext {
            account.editFolder(form = form)
        }
    }
}
