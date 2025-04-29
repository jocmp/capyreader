package com.jocmp.capy.common

import java.io.IOException


val Throwable.isIOError
    get() = this is IOException
