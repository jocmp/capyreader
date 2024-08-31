package com.jocmp.capy.accounts

enum class AutoDelete {
    DISABLED,
    WEEKLY,
    EVERY_TWO_WEEKS,
    EVERY_MONTH,
    EVERY_THREE_MONTHS;

    companion object {
        val default = EVERY_THREE_MONTHS
    }
}
