package com.jocmp.capy.common

import java.io.File


fun optionalFile(string: String?): File? {
    if (string.isNullOrBlank()) {
        return null
    }

    return try {
        File(string)
    } catch (_: Throwable) {
        null
    }
}
