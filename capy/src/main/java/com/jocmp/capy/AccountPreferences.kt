package com.jocmp.capy

import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.common.TimeHelpers
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

    val keywordBlocklist: Preference<Set<String>>
        get() = store.getStringSet("keyword_blocklist")

    val canSaveArticleExternally: Preference<Boolean>
        get() = store.getBoolean("can_save_article_externally", false)

    val lastRefreshedAt: Preference<Long>
        get() = store.getLong("last_refreshed_at", 0L)

    val enableAiSummaries: Preference<Boolean>
        get() = store.getBoolean("enable_ai_summaries", false)

    val aiApiKey: Preference<String>
        get() = store.getString("ai_api_key", "")

    val aiBaseUrl: Preference<String>
        get() = store.getString("ai_base_url", "https://api.openai.com/v1/")

    val aiModel: Preference<String>
        get() = store.getString("ai_model", "gpt-4o-mini")

    val aiSystemPrompt: Preference<String>
        get() = store.getString("ai_system_prompt", "Summarize this article in a few sentences.")

    suspend fun touchLastRefreshedAt() {
        lastRefreshedAt.set(TimeHelpers.nowUTC().toEpochSecond())
    }
}
