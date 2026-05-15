package com.jocmp.capy

import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.preferences.OfflineCacheSize
import com.jocmp.capy.preferences.Preference
import com.jocmp.capy.preferences.PreferenceStore
import com.jocmp.capy.preferences.getEnum

class AccountPreferences(
    private val store: PreferenceStore,
) {
    val source: Preference<Source>
        get() = store.getEnum("source", Source.LOCAL)

    val username: Preference<String>
        get() = store.getString("username", "")

    val url: Preference<String>
        get() = store.getString("api_url", "")

    val clientCertAlias: Preference<String>
        get() = store.getString("client_cert_alias", "")

    val password: Preference<String>
        get() = store.getString("password", "")

    val autoDelete: Preference<AutoDelete>
        get() = store.getEnum("auto_delete_articles", AutoDelete.default)

    val offlineCacheLimit: Preference<Int>
        get() = store.getInt("offline_cache_limit", OfflineCacheSize.default.limit)

    fun offlineCacheSize(): OfflineCacheSize =
        OfflineCacheSize.fromLimit(offlineCacheLimit.get())

    fun setOfflineCacheSize(size: OfflineCacheSize) {
        offlineCacheLimit.set(size.limit)
    }

    val filterKeywords: Preference<Set<String>>
        get() = store.getStringSet("keyword_blocklist")

    val canSaveArticleExternally: Preference<Boolean>
        get() = store.getBoolean("can_save_article_externally", false)

    val lastRefreshedAt: Preference<Long>
        get() = store.getLong("last_refreshed_at", 0L)

    suspend fun touchLastRefreshedAt() {
        lastRefreshedAt.set(TimeHelpers.nowUTC().toEpochSecond())
    }
}
