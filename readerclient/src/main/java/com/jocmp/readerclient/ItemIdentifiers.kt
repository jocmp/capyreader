package com.jocmp.readerclient

object ItemIdentifiers {
    fun parseToHexID(numericID: String) = String.format("%016x", numericID.toLong())
}
