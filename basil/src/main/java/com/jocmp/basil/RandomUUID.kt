package com.jocmp.basil

import java.util.UUID as JavaUUID

object RandomUUID {
    fun generate(): String {
        return JavaUUID.randomUUID().toString()
    }
}
