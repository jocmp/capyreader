package com.jocmp.feedfinder

val String.withProtocol: String
    get() {
        return if (!(startsWith("http") || startsWith("https"))) {
            "https://$this"
        } else {
            this
        }
    }
