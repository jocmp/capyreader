package com.jocmp.basilreader.common

import android.content.Context
import androidx.preference.PreferenceManager
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.preferences.AndroidPreferenceStore
import com.jocmp.basil.preferences.Preference
import com.jocmp.basil.preferences.PreferenceStore
import com.jocmp.basil.preferences.getEnum
import com.jocmp.basilreader.refresher.RefreshInterval
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppPreferences(context: Context) {
    private val preferenceStore: PreferenceStore = AndroidPreferenceStore(
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    )

    val accountID: Preference<String>
        get() = preferenceStore.getString("account_id")

    val filter: Preference<ArticleFilter>
        get() = preferenceStore.getObject(
            key = "article_filter",
            defaultValue = ArticleFilter.default(),
            serializer = { Json.encodeToString(it) },
            deserializer = { Json.decodeFromString(it) }
        )

    val refreshInterval: Preference<RefreshInterval>
        get() = preferenceStore.getEnum("refresh_interval", RefreshInterval.default)

    val articleID: Preference<String>
        get() = preferenceStore.getString("article_id")

    fun clearAll() {
        accountID.delete()
        articleID.delete()
        refreshInterval.delete()
        filter.delete()
    }
}
