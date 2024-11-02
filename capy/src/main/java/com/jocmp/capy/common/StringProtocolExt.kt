package com.jocmp.capy.common

val String.withTrailingSeparator: String
    get() {
        return if (endsWith("/")) {
            this
        } else {
            "$this/"
        }
    }
