package com.prof18.rssparser

import com.prof18.rssparser.internal.DefaultParser
import com.prof18.rssparser.internal.Parser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

internal object ParserFactory {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun build(): Parser = DefaultParser(dispatcher = UnconfinedTestDispatcher())
}
