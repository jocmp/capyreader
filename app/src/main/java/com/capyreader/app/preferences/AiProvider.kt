package com.capyreader.app.preferences

enum class AiProvider {
    OPENAI,
    GOOGLE,
    ANTHROPIC,
    CUSTOM;

    companion object {
        val default = OPENAI
    }
}
