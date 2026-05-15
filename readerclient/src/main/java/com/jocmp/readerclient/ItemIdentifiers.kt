package com.jocmp.readerclient

private const val TAG_PREFIX = "tag:google.com,2005:reader/item/"

object ItemIdentifiers {
    fun parseToHexID(numericID: String) = String.format("%016x", numericID.toLong())
}

val String.taggedItemID: String
    get() = "$TAG_PREFIX$this"
