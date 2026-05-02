package com.jocmp.capy

import kotlinx.serialization.Serializable

@Serializable
sealed class Velocity {
    abstract val hours: Long?

    @Serializable
    data object EightHours : Velocity() {
        override val hours: Long = 8
    }

    @Serializable
    data object Day : Velocity() {
        override val hours: Long = 24
    }

    @Serializable
    data object ThreeDays : Velocity() {
        override val hours: Long = 24 * 3
    }

    @Serializable
    data object TwoWeeks : Velocity() {
        override val hours: Long = 24 * 14
    }

    @Serializable
    data object Forever : Velocity() {
        override val hours: Long? = null
    }

    @Serializable
    data class Custom(val days: Int) : Velocity() {
        override val hours: Long = days.toLong() * 24
    }

    companion object {
        val default: Velocity = TwoWeeks

        fun fromHours(hours: Long?): Velocity {
            if (hours == null) return Forever
            return when (hours) {
                EightHours.hours -> EightHours
                Day.hours -> Day
                ThreeDays.hours -> ThreeDays
                TwoWeeks.hours -> TwoWeeks
                else -> Custom(days = ((hours + 23) / 24).toInt().coerceAtLeast(1))
            }
        }
    }
}
