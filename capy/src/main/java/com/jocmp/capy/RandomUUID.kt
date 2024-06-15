package com.jocmp.capy

import java.util.UUID as JavaUUID

object RandomUUID {
    fun generate(): String {
        return JavaUUID.randomUUID().toString()
    }
}
