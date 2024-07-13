package com.jocmp.capy.common

import java.time.ZonedDateTime
import java.util.TimeZone

fun ZonedDateTime.toDeviceDateTime(): ZonedDateTime {
    return withZoneSameInstant(TimeZone.getDefault().toZoneId())
}
