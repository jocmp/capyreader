package com.prof18.rssparser.exception

/**
 * An exception thrown when an HTTP call fails with an HTTP error
 *
 * @property code the HTTP error code
 * @property message the detail message string.
 */
data class HttpException(
    val code: Int,
    override val message: String?,
) : Exception()
