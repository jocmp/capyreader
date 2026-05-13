package com.jocmp.hyperview

sealed interface HtmlClick {
    data class Link(val href: String, val text: String) : HtmlClick
    data class Image(val src: String, val alt: String?) : HtmlClick
    data class Video(val src: String, val poster: String?) : HtmlClick
    data class Audio(val src: String) : HtmlClick
    data class Iframe(val src: String) : HtmlClick
}

sealed interface HtmlLongClick {
    data class Link(val href: String, val text: String) : HtmlLongClick
    data class Image(val src: String, val alt: String?) : HtmlLongClick
}
