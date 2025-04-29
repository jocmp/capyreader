package com.jocmp.capy.common

import java.net.IOException

val Throwable.isNetworkError
    get() = this is IOException
