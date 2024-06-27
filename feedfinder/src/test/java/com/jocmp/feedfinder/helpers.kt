package com.jocmp.feedfinder

import java.io.File

fun testResource(resource: String): String {
    return "src/test/resources/${resource}"
}

fun testFile(resource: String): File {
    return File(testResource(resource))
}
