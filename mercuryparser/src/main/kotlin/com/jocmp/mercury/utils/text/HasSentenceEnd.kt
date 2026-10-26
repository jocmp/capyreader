package com.jocmp.mercury.utils.text

// Given a string, return True if it appears to have an ending sentence
// within it, false otherwise.
private val SENTENCE_END_RE: Regex = Regex(".( |$)")

fun hasSentenceEnd(text: String): Boolean = SENTENCE_END_RE.containsMatchIn(text)
