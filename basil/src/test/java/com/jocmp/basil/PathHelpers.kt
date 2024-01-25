package com.jocmp.basil

import java.io.File
import java.net.URI

fun testResource(resource: String): String {
    return "src/test/resources/${resource}"
}

fun testURI(resource: String): URI {
    return File(testResource(resource)).toURI()
}

fun testFile(resource: String): File {
    return File(testResource(resource))
}
