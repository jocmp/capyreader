package com.jocmp.capy.accounts

enum class AutoDelete {
    ENABLED,
    DISABLED;

    companion object {
        val default = ENABLED
    }
}
