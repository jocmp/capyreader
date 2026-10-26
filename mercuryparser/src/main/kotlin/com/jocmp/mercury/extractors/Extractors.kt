package com.jocmp.mercury.extractors

import com.jocmp.mercury.extractors.custom.AllExtractors
import com.jocmp.mercury.resource.utils.baseDomain
import java.net.URI

// Registry of custom extractors. Loaded lazily from `AllExtractors.all`.
object Extractors {
    private val byDomain: Map<String, CustomExtractor> by lazy {
        val map = mutableMapOf<String, CustomExtractor>()
        AllExtractors.all.forEach { add(map, it) }
        runtimeRegistered.forEach { add(map, it) }
        map
    }
    private val runtimeRegistered: MutableList<CustomExtractor> = mutableListOf()

    private fun add(
        map: MutableMap<String, CustomExtractor>,
        ex: CustomExtractor,
    ) {
        map[ex.domain] = ex
        ex.supportedDomains.forEach { map[it] = ex }
    }

    /** Add an extractor at runtime. Useful for CLI `--customExtractor` or tests. */
    fun register(extractor: CustomExtractor) {
        runtimeRegistered.add(extractor)
    }

    /** Look up the extractor for [url], falling back through base-domain matching. */
    fun get(url: String): CustomExtractor? {
        val uri = runCatching { URI.create(url) }.getOrNull() ?: return null
        val host = uri.host ?: return null
        byDomain[host]?.let { return it }
        val base = baseDomain(uri)
        return byDomain[base]
    }
}
