package com.prof18.rssparser

import com.prof18.rssparser.internal.DefaultXmlParser
import com.prof18.rssparser.internal.XmlParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

internal object XmlParserFactory {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun createXmlParser(): XmlParser = DefaultXmlParser(dispatcher = UnconfinedTestDispatcher())
}
