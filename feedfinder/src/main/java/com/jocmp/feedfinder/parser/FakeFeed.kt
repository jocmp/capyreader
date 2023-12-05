package com.jocmp.feedfinder.parser

class FakeFeed: Feed {
    override fun isValid(): Boolean {
        return false
    }
}
