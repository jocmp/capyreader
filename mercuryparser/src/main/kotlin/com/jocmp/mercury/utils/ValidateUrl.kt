package com.jocmp.mercury.utils

import java.net.URI

// extremely simple url validation as a first step
fun validateUrl(uri: URI): Boolean = !uri.host.isNullOrEmpty()
