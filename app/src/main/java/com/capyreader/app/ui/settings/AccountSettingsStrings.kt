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
                Source.FRESHRSS,
                Source.MINIFLUX,
                Source.MINIFLUX_TOKEN,
                Source.READER -> AccountSettingsStrings(
                    dialogTitle = R.string.settings_remove_account_title_service,
                    dialogMessage = R.string.settings_remove_account_message_service,
                    dialogConfirmText = R.string.settings_remove_account_confirm_service,
                    requestRemoveText = R.string.settings_remove_account_button_service,
                )
            }
        }
    }
}
