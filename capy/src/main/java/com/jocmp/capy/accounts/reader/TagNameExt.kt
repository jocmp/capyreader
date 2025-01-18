package com.jocmp.capy.accounts.reader

import com.jocmp.readerclient.Tag

val Tag.name: String
    get() = id.split("/").lastOrNull().orEmpty()
