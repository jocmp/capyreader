package com.jocmp.capy.common

import com.jocmp.feedbinclient.Subscription
import java.net.URI

internal val Subscription.host: String?
    get() {
        return try {
            URI(site_url).toURL().host
        } catch (_: Throwable) {
            null
        }
    }
