package com.capyreader.app.ui.settings

import androidx.annotation.StringRes
import com.capyreader.app.R
import com.jocmp.capy.accounts.Source

data class AccountSettingsStrings(
    @StringRes val dialogTitle: Int,
    @StringRes val dialogMessage: Int,
    @StringRes val dialogConfirmText: Int,
    @StringRes val requestRemoveText: Int
) {
    companion object {
        fun build(source: Source): AccountSettingsStrings {
            return when (source) {
                Source.LOCAL -> AccountSettingsStrings(
                    dialogTitle = R.string.settings_remove_account_title_local,
                    dialogMessage = R.string.settings_remove_account_message_local,
                    dialogConfirmText = R.string.settings_remove_account_confirm_local,
                    requestRemoveText = R.string.settings_remove_account_button_local,
                )

                Source.FEEDBIN,
                Source.FRESHRSS -> AccountSettingsStrings(
                    dialogTitle = R.string.settings_remove_account_title_feedbin,
                    dialogMessage = R.string.settings_remove_account_message_feedbin,
                    dialogConfirmText = R.string.settings_remove_account_confirm_feedbin,
                    requestRemoveText = R.string.settings_remove_account_button_feedbin,
                )
            }
        }
    }
}
