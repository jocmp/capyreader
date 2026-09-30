package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.capyreader.app.R
import com.capyreader.app.common.AudioEnclosure
import com.capyreader.app.common.Media
import com.capyreader.app.common.shareImage
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ReaderImageVisibility
import com.capyreader.app.ui.ConnectivityType
import com.capyreader.app.ui.LocalConnectivity
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.articles.ColumnScrollbar
import com.capyreader.app.ui.articles.media.ImageSaver
import com.capyreader.app.ui.articles.reader.AnchorRegistry
import com.capyreader.app.ui.articles.reader.ArticleReaderContent
import com.capyreader.app.ui.articles.reader.LocalReaderPageHeight
import com.capyreader.app.ui.articles.reader.PageDirection
import com.capyreader.app.ui.articles.reader.LocalReaderStyle
import com.capyreader.app.ui.articles.reader.ReaderActions
import com.capyreader.app.ui.articles.reader.galleryItems
import com.capyreader.app.ui.articles.reader.largestSource
import com.capyreader.app.ui.articles.reader.rememberReaderStyle
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.components.LocalSnackbarHost
import com.capyreader.app.ui.components.rememberSaveableShareLink
import com.jocmp.capy.Article
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.launchUI
import com.jocmp.capy.common.withUIContext
import com.jocmp.mallet.LinearArticle
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ArticleReader(
    article: Article,
    flattened: LinearArticle?,
    scrollState: ScrollState,
    pinToolbars: Boolean,
    showToolbars: Boolean,
    onHideToolbars: () -> Unit,
    onToggleToolbars: () -> Unit,
    onTurnPastArticle: (PageDirection) -> Unit,
    openAtEnd: Boolean,
    onOpenedAtEnd: () -> Unit,
    onSelectMedia: (media: Media) -> Unit,
    onSelectAudio: (audio: AudioEnclosure) -> Unit = {},
    onPauseAudio: () -> Unit = {},
    currentAudioUrl: String? = null,
    isAudioPlaying: Boolean = false,
    isAudioBuffering: Boolean = false,
    appPreferences: AppPreferences = koinInject(),
) {
    val (shareLink, setShareLink) = rememberSaveableShareLink()
    val (shareImageUrl, setImageUrl) = rememberSaveable { mutableStateOf<String?>(null) }
    val linkOpener = LocalLinkOpener.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = LocalSnackbarHost.current
    val successMessage = stringResource(R.string.media_save_success)
    val failureMessage = stringResource(R.string.media_save_failure)
    val shareFailureMessage = stringResource(R.string.media_share_failure)

    fun showSnackbar(message: String) {
        scope.launchUI {
            snackbar.showSnackbar(message)
        }
    }

    fun saveImage(imageUrl: String) {
        scope.launchIO {
            val result = ImageSaver.saveImage(imageUrl, context = context)

            withUIContext {
                result.fold(
                    onSuccess = {
                        showSnackbar(successMessage)
                    },
                    onFailure = {
                        showSnackbar(failureMessage)
                    }
                )

            }
        }

        setImageUrl(null)
    }

    fun shareImage(imageUrl: String) {
        scope.launchIO {
            ImageSaver.shareImage(imageUrl, context = context)
                .fold(
                    onSuccess = { uri ->
                        context.shareImage(uri)
                    },
                    onFailure = {
                        showSnackbar(shareFailureMessage)
                    }
                )
        }

        setImageUrl(null)
    }

    val anchors = remember(scrollState) { AnchorRegistry(scrollState) }

    LaunchedEffect(openAtEnd) {
        if (!openAtEnd) {
            return@LaunchedEffect
        }

        onOpenedAtEnd()

        var pinnedEnd = 0

        snapshotFlow { scrollState.maxValue }.collect { end ->
            if (scrollState.value >= pinnedEnd) {
                scrollState.scrollTo(end)
                pinnedEnd = end
            }
        }
    }

    val currentFlattened by rememberUpdatedState(flattened)
    val currentOnSelectMedia by rememberUpdatedState(onSelectMedia)
    val currentOnSelectAudio by rememberUpdatedState(onSelectAudio)
    val currentArticle by rememberUpdatedState(article)

    val externalUrl = article.url?.toString() ?: article.siteURL
    val openExternalLink = {
        externalUrl?.let { linkOpener.open(it.toUri()) }
        Unit
    }

    val actions = remember(article.id, linkOpener) {
        ReaderActions(
            onLinkClick = { url, elementIndex ->
                scope.launch {
                    val scrolled = elementIndex != null && anchors.scrollTo(elementIndex)

                    if (!scrolled) {
                        linkOpener.open(url.toUri())
                    }
                }
            },
            onLinkLongPress = { link -> setShareLink(link) },
            onImageClick = { image ->
                val items = currentFlattened?.galleryItems().orEmpty()
                val clickedUrl = image.largestSource()?.imgUri
                val index = items.indexOfFirst { it.url == clickedUrl }.coerceAtLeast(0)

                if (items.isNotEmpty()) {
                    currentOnSelectMedia(Media(images = items, startIndex = index))
                }
            },
            onImageLongPress = { url -> setImageUrl(url) },
            onAudioClick = { url ->
                currentOnSelectAudio(
                    AudioEnclosure(
                        articleID = currentArticle.id,
                        url = url,
                        title = currentArticle.title,
                        feedName = currentArticle.feedName,
                        durationSeconds = null,
                        artworkUrl = null,
                    )
                )
            },
        )
    }

    val showImages = rememberImageVisibility()
    val readerStyle = rememberReaderStyle(showImages = showImages)

    CompositionLocalProvider(
        LocalReaderStyle provides readerStyle,
    ) {
        ScrollableArticle(
            scrollState = scrollState,
            pinToolbars = pinToolbars,
            showToolbars = showToolbars,
            onHideToolbars = onHideToolbars,
            onToggleToolbars = onToggleToolbars,
            onTurnPastArticle = onTurnPastArticle,
            onContentPositioned = {
                anchors.contentCoordinates = it
            },
        ) {
            ArticleReaderContent(
                article = article,
                flattened = flattened,
                actions = actions,
                onOpenExternalLink = openExternalLink,
                currentAudioUrl = currentAudioUrl,
                isAudioPlaying = isAudioPlaying,
                isAudioBuffering = isAudioBuffering,
                onSelectAudio = onSelectAudio,
                onPauseAudio = onPauseAudio,
                onElementPositioned = { index, coordinates -> anchors.register(index, coordinates) },
            )
        }
    }

    if (shareLink != null) {
        ShareLinkDialog(
            onClose = {
                setShareLink(null)
            },
            link = shareLink,
        )
    }

    if (shareImageUrl != null) {
        ShareImageDialog(
            onClose = {
                setImageUrl(null)
            },
            imageUrl = shareImageUrl,
            onSave = { saveImage(shareImageUrl) },
            onShare = { shareImage(shareImageUrl) },
        )
    }
}

@Composable
private fun ScrollableArticle(
    scrollState: ScrollState,
    pinToolbars: Boolean,
    showToolbars: Boolean,
    onHideToolbars: () -> Unit,
    onToggleToolbars: () -> Unit,
    onTurnPastArticle: (PageDirection) -> Unit,
    onContentPositioned: (coordinates: androidx.compose.ui.layout.LayoutCoordinates) -> Unit,
    appPreferences: AppPreferences = koinInject(),
    content: @Composable () -> Unit,
) {
    val enableScrollbar by appPreferences.readerOptions.enableEInkScrollbar.collectChangesWithCurrent()

    PageTurnGestures(
        scrollState = scrollState,
        pinToolbars = pinToolbars,
        showToolbars = showToolbars,
        onHideToolbars = onHideToolbars,
        onToggleToolbars = onToggleToolbars,
        onTurnPastArticle = onTurnPastArticle,
    ) {
        BoxWithConstraints {
            val pageHeight = maxHeight - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

            val column = @Composable {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .onGloballyPositioned { coordinates ->
                            onContentPositioned(coordinates)
                        }
                ) {
                    if (!pinToolbars) {
                        Spacer(Modifier.height(ArticleBarDefaults.topBarOffset))
                    }
                    CompositionLocalProvider(LocalReaderPageHeight provides pageHeight) {
                        content()
                    }
                }
            }

            if (enableScrollbar) {
                column()
            } else {
                ColumnScrollbar(state = scrollState) {
                    column()
                }
            }
        }
    }
}

@Composable
fun rememberImageVisibility(appPreferences: AppPreferences = koinInject()): Boolean {
    val imagePreference by appPreferences.readerOptions
        .imageVisibility
        .changes()
        .collectAsState(appPreferences.readerOptions.imageVisibility.get())

    val connectivity = LocalConnectivity.current

    return imagePreference == ReaderImageVisibility.ALWAYS_SHOW ||
            (imagePreference == ReaderImageVisibility.SHOW_ON_WIFI && connectivity.isOnWifi)
}

private val ConnectivityType.isOnWifi
    get() = this == ConnectivityType.WIFI || this == ConnectivityType.ETHERNET
