package com.jocmp.capy

sealed class MarkRead {
    data class Pair(
        val afterSnowflakeID: Long? = null,
        val beforeSnowflakeID: Long? = null,
    )

    data object All : MarkRead()

    data class Before(val snowflakeId: Long): MarkRead()

    data class After(val snowflakeId: Long): MarkRead()

    fun reversed(): MarkRead {
        return when(this) {
            is All -> All
            is Before -> After(snowflakeId)
            is After -> Before(snowflakeId)
        }
    }

    val toPair: Pair
        get() = when(this) {
            is After -> Pair(afterSnowflakeID = snowflakeId)
            is Before -> Pair(beforeSnowflakeID = snowflakeId)
            All -> Pair()
        }
}
