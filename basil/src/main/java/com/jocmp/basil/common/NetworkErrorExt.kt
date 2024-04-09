package com.jocmp.basil.common

import java.net.UnknownHostException

val Throwable.isNetworkError
    get() = this is UnknownHostException
