package com.jocmp.readerclient

enum class Stream(val id: String){
    READING_LIST("user/-/state/com.google/reading-list"),

    STARRED("user/-/state/com.google/starred");
}
