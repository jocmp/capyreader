package com.jocmp.feverclient

enum class MarkState(override val value: String): EnumValue {
    READ("read"),
    UNREAD("unread"),
    SAVED("saved"),
    UNSAVED("unsaved")
}
