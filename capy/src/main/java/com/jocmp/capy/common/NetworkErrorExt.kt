package com.jocmp.capy.common

import java.net.UnknownHostException

val Throwable.isNetworkError
    get() = this is UnknownHostException
