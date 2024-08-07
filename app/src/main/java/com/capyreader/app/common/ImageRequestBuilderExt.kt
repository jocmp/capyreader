package com.capyreader.app.common

import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest

fun ImageRequest.Builder.supportGifs() = apply {
    decoderFactory(ImageDecoderDecoder.Factory())
}
