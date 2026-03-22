package com.jocmp.capy.common

import java.net.URI
import java.net.URL

fun String.toURL(): URL = URI(this).toURL()
