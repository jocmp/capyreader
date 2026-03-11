package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.preferences.AiProvider
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Account
import com.jocmp.capy.ai.AiSummaryClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AiSettingsViewModel(
    private val appPreferences: AppPreferences,
    private val account: Account,
) : ViewModel() {
    var enableAiSummaries by mutableStateOf(appPreferences.enableAiSummaries.get())
        private set

    var aiProvider by mutableStateOf(appPreferences.aiProvider.get())
        private set

    var aiApiKey by mutableStateOf(appPreferences.aiApiKey.get())
        private set

    var aiBaseUrl by mutableStateOf(appPreferences.aiBaseUrl.get())
        private set

    var aiModel by mutableStateOf(appPreferences.aiModel.get())
        private set

    var aiSystemPrompt by mutableStateOf(appPreferences.aiSystemPrompt.get())
        private set

    var models by mutableStateOf<List<String>>(emptyList())
        private set

    var isFetchingModels by mutableStateOf(false)
        private set

    private var fetchJob: Job? = null

    init {
        if (aiApiKey.isNotBlank() && aiBaseUrl.isNotBlank()) {
            fetchModels()
        }
    }

    fun updateEnableAiSummaries(enable: Boolean) {
        enableAiSummaries = enable
        appPreferences.enableAiSummaries.set(enable)
    }

    fun updateAiProvider(provider: AiProvider) {
        aiProvider = provider
        appPreferences.aiProvider.set(provider)
        
        val defaultUrl = when(provider) {
            AiProvider.OPENAI -> "https://api.openai.com/v1/"
            AiProvider.GOOGLE -> "https://generativelanguage.googleapis.com/v1beta/openai/"
            AiProvider.ANTHROPIC -> "https://api.anthropic.com/v1/"
            AiProvider.CUSTOM -> aiBaseUrl
        }
        
        if (provider != AiProvider.CUSTOM) {
            updateAiBaseUrl(defaultUrl)
        } else {
            fetchModels()
        }
    }

    fun updateAiApiKey(key: String) {
        aiApiKey = key
        appPreferences.aiApiKey.set(key)
        debounceFetchModels()
    }

    fun updateAiBaseUrl(url: String) {
        aiBaseUrl = url
        appPreferences.aiBaseUrl.set(url)
        debounceFetchModels()
    }

    private fun debounceFetchModels() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            delay(500)
            fetchModels()
        }
    }

    fun updateAiModel(model: String) {
        aiModel = model
        appPreferences.aiModel.set(model)
    }

    fun updateAiSystemPrompt(prompt: String) {
        aiSystemPrompt = prompt
        appPreferences.aiSystemPrompt.set(prompt)
    }

    fun clearAllAiSummaries() {
        viewModelScope.launch {
            account.clearAllAiSummaries()
        }
    }

    fun fetchModels() {
        if (aiApiKey.isBlank() || aiBaseUrl.isBlank()) return

        viewModelScope.launch {
            isFetchingModels = true
            val client = AiSummaryClient(aiApiKey, aiBaseUrl)
            val result = client.fetchModels()
            result.onSuccess { fetchedModels ->
                models = fetchedModels
                if (aiModel.isBlank() || !models.contains(aiModel)) {
                    models.firstOrNull()?.let { updateAiModel(it) }
                }
            }.onFailure {
                models = emptyList()
            }
            isFetchingModels = false
        }
    }
}
