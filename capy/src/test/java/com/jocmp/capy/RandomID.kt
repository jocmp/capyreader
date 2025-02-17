package com.jocmp.capy

import java.security.SecureRandom

fun randomID() = SecureRandom.getInstanceStrong().nextLong()
