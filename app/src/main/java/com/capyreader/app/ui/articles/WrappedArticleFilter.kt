package com.capyreader.app.ui.articles

import android.os.Bundle
import androidx.navigation.NavType
import com.jocmp.capy.ArticleFilter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

object WrappedArticleFilter : NavType<ArticleFilter>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): ArticleFilter? {
        val value = bundle.getString(key) ?: return null

        return Json.decodeFromString(value)
    }

    override fun parseValue(value: String): ArticleFilter {
        return Json.decodeFromString(value)
    }

    override fun put(bundle: Bundle, key: String, value: ArticleFilter) {
        bundle.putString(key, Json.encodeToString(value))
    }

    override fun serializeAsValue(value: ArticleFilter): String {
        return Json.encodeToString(value)
    }

    val typeMap = mapOf(typeOf<ArticleFilter>() to WrappedArticleFilter)
}
