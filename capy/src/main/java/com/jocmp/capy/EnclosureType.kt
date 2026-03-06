package com.jocmp.capy

enum class EnclosureType {
    AUDIO,
    VIDEO;

    companion object {
        fun from(mimeType: String?): EnclosureType? {
            if (mimeType == null) {
                return null
            }

            return when {
                mimeType.startsWith("audio/") -> AUDIO
                mimeType.startsWith("video/") -> VIDEO
                else -> null
            }
        }
    }
}
