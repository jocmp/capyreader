package com.jocmp.capy.common

import com.jocmp.feedbinclient.Subscription
import java.net.MalformedURLException
import java.net.URL

internal val Subscription.host: String?
    get() {
        return try {
            URL(site_url).host
        } catch (e: MalformedURLException) {
            null
        }
    }
