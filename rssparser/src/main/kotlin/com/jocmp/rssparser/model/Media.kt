package com.jocmp.rssparser.model

data class Media(
    val title: String?,
    val contentUrl: String?,
    val thumbnailUrl: String?,
    val description: String?,
) {
    internal data class Builder(
        private var title: String? = null,
        private var contentUrl: String? = null,
        private var thumbnailUrl: String? = null,
        private var description: String? = null,
    ) {
        fun title(title: String?) = apply { this.title = title }
        fun contentUrl(contentUrl: String?) = apply { this.contentUrl = contentUrl }
        fun thumbnailUrl(thumbnailUrl: String?) = apply { this.thumbnailUrl = thumbnailUrl }
        fun description(description: String?) = apply { this.description = description }

        fun build() = Media(
            title = title,
            contentUrl = contentUrl,
            thumbnailUrl = thumbnailUrl,
            description = description,
        )
    }
}
