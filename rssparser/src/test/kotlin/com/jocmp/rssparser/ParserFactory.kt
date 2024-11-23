package com.jocmp.rssparser

import com.jocmp.rssparser.internal.DefaultParser
import com.jocmp.rssparser.internal.Parser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

internal object ParserFactory {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun build(): Parser = DefaultParser(dispatcher = UnconfinedTestDispatcher())
}
