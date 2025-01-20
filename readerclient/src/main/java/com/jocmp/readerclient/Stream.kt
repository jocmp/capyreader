package com.jocmp.readerclient

sealed class Stream(val id: String) {
    class ReadingList: Stream("user/-/state/com.google/reading-list")

    class Starred: Stream("user/-/state/com.google/starred")

    class Read: Stream("user/-/state/com.google/read")

    class Feed(id: String): Stream(id)

    class Label(name: String): Stream("user/-/label/$name")
}
