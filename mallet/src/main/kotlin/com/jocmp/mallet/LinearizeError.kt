package com.jocmp.mallet

sealed class LinearizeError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class ParseFailed(
        cause: Throwable,
    ) : LinearizeError("Failed to linearize HTML", cause)
}

data class Truncation(
    val elementCount: Int,
    val charCount: Int,
)
