package com.prof18.rssparser.exception

/**
 * An exception thrown whe the parsing of the RSS feed fails
 *
 * @property message the detail message string.
 * @property cause the cause of this throwable.
 */
data class RssParsingException(
    override val message: String?,
    override val cause: Throwable?,
) : Exception()
