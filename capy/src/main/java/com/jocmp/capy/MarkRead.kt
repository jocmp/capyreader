package com.jocmp.capy

sealed class MarkRead {
    data class Pair(
        val afterSnowflakeID: Long? = null,
        val beforeSnowflakeID: Long? = null,
    )

    data object All : MarkRead()

    data class Before(val snowflakeID: Long): MarkRead()

    data class After(val snowflakeID: Long): MarkRead()

    fun reversed(): MarkRead {
        return when(this) {
            is All -> All
            is Before -> After(snowflakeID)
            is After -> Before(snowflakeID)
        }
    }

    val toPair: Pair
        get() = when(this) {
            is After -> Pair(afterSnowflakeID = snowflakeID)
            is Before -> Pair(beforeSnowflakeID = snowflakeID)
            All -> Pair()
        }
}
