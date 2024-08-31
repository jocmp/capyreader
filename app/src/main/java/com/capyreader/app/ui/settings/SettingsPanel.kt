package com.capyreader.app.ui.settings

import android.os.Parcelable
import androidx.annotation.StringRes
import com.capyreader.app.R
import kotlinx.parcelize.Parcelize

sealed class SettingsPanel(@StringRes val title: Int) {
    @Parcelize
    data object General : SettingsPanel(title = R.string.settings_panel_general_title), Parcelable

    @Parcelize
    data object Display : SettingsPanel(title = R.string.settings_panel_display_title), Parcelable

    @Parcelize
    data object Account : SettingsPanel(title = R.string.settings_account_title), Parcelable

    @Parcelize
    data object About : SettingsPanel(title = R.string.settings_about_title), Parcelable

    companion object {
        val items: List<SettingsPanel>
            get() = listOf(
                General,
                Display,
                Account,
                About,
            )
    }
}
