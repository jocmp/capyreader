package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.common.AudioEnclosure
import com.capyreader.app.common.Media
import com.capyreader.app.common.rememberTalkbackPreference
import com.capyreader.app.common.shareImage
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ReaderImageVisibility
import com.capyreader.app.ui.ConnectivityType
import com.capyreader.app.ui.LocalConnectivity
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.articles.ColumnScrollbar
import com.capyreader.app.ui.articles.media.ImageSaver
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.WebViewState
import com.capyreader.app.ui.collectChangesWithDefault
import com.jocmp.hyperview.HtmlClick
import com.jocmp.hyperview.HtmlContent
import com.jocmp.hyperview.HtmlLongClick
import com.jocmp.hyperview.HtmlParser
import com.jocmp.hyperview.images
import com.capyreader.app.common.MediaItem
import com.capyreader.app.ui.components.ShareLink
import com.capyreader.app.ui.articles.displayFeedName
import com.jocmp.capy.articles.TextAlignment
import com.capyreader.app.ui.LocalTimeFormats
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.capyreader.app.ui.components.rememberSaveableShareLink
import com.capyreader.app.ui.components.rememberWebViewState
import com.capyreader.app.ui.components.LocalSnackbarHost
import com.jocmp.capy.Article
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.launchUI
import com.jocmp.capy.common.withUIContext
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun ArticleReader(
    article: Article,
    pinToolbars: Boolean,
    onSelectMedia: (media: Media) -> Unit,
    onSelectAudio: (audio: AudioEnclosure) -> Unit = {},
    onPauseAudio: () -> Unit = {},
    currentAudioUrl: String? = null,
    isAudioPlaying: Boolean = false,
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

    val webViewState = rememberWebViewState(
        key = article.id,
        onNavigateToMedia = onSelectMedia,
        onRequestLinkDialog = { setShareLink(it) },
        onRequestImageDialog = { setImageUrl(it) },
        onOpenLink = { linkOpener.open(it) },
        onOpenAudioPlayer = onSelectAudio,
        onPauseAudio = onPauseAudio,
        currentAudioUrl = currentAudioUrl,
        isAudioPlaying = isAudioPlaying,
    )

    LaunchedEffect(currentAudioUrl, isAudioPlaying) {
        webViewState.updateAudioPlayState(currentAudioUrl, isAudioPlaying)
    }

    val showImages = rememberImageVisibility()
    val improveTalkback by rememberTalkbackPreference()
    val appPreferences = koinInject<AppPreferences>()
    val useNativeRenderer by appPreferences.readerOptions.useNativeRenderer.collectChangesWithDefault()

    if (useNativeRenderer) {
        NativeArticleReader(
            article = article,
            pinToolbars = pinToolbars,
            onLinkClick = { linkOpener.open(it.toUri()) },
            onSelectMedia = onSelectMedia,
            onRequestLinkDialog = setShareLink,
            onRequestImageDialog = { setImageUrl(it) },
        )
        return
    }

    if (improveTalkback) {
        Column(
            Modifier.fillMaxSize()
        ) {
            WebView(
                modifier = Modifier.fillMaxSize(),
                state = webViewState,
                article = article,
                showImages = showImages,
            )
        }
    } else {
        ScrollableWebView(webViewState, article, showImages, pinToolbars)
    }

    ArticleStyleListener(webView = webViewState.webView)

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
fun ScrollableWebView(webViewState: WebViewState, article: Article, showImages: Boolean, pinToolbars: Boolean) {
    var maxHeight by remember { mutableFloatStateOf(0f) }
    val scrollState = rememberSaveable(article.id, saver = ScrollState.Saver) {
        ScrollState(initial = 0)
    }

    var lastScrollYPercent by rememberSaveable(article.id) { mutableFloatStateOf(0f) }

    CornerTapGestureScroll(
        maxArticleHeight = maxHeight,
        scrollState = scrollState,
        pinToolbars = pinToolbars,
    ) {
        ColumnScrollbar(state = scrollState) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .onGloballyPositioned { coordinates ->
                        maxHeight = coordinates.size.height.toFloat()
                    }
            ) {
                if (!pinToolbars) {
                    Spacer(Modifier.height(ArticleBarDefaults.topBarOffset))
                }
                WebView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    state = webViewState,
                    article = article,
                    showImages = showImages,
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { scrollState.value to maxHeight }
            .collect { (value, height) ->
                if (value > 0 && height > 0f) {
                    lastScrollYPercent = value / height
                }
            }
    }
    LaunchedEffect(scrollState.maxValue, maxHeight) {
        if (scrollState.maxValue > 0 && maxHeight > 0) {
            scrollState.scrollTo((lastScrollYPercent * maxHeight).roundToInt())
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

@Composable
private fun NativeArticleReader(
    article: com.jocmp.capy.Article,
    pinToolbars: Boolean,
    onLinkClick: (String) -> Unit,
    onSelectMedia: (media: Media) -> Unit,
    onRequestLinkDialog: (link: ShareLink?) -> Unit,
    onRequestImageDialog: (url: String) -> Unit,
) {
    val scrollState = rememberSaveable(article.id, saver = ScrollState.Saver) {
        ScrollState(initial = 0)
    }
    val document = remember(article.id, article.content) { HtmlParser.parse(article.content) }
    val gallery = remember(document) {
        document.images()
            .map { MediaItem(url = it.src, altText = it.alt) }
    }
    val htmlStyle = rememberArticleHtmlStyle()
    val titleAlignment by koinInject<AppPreferences>().readerOptions.titleTextAlignment
        .collectChangesWithDefault()
    val titleSize by koinInject<AppPreferences>().readerOptions.titleFontSize.collectChangesWithDefault()
    val context = LocalContext.current

    fun openMedia(src: String) {
        val index = gallery.indexOfFirst { it.url == src }.coerceAtLeast(0)
        if (gallery.isEmpty()) return
        onSelectMedia(Media(images = gallery, startIndex = index))
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        if (!pinToolbars) {
            Spacer(Modifier.height(ArticleBarDefaults.topBarOffset))
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = ArticleHtmlMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = article.title.orEmpty(),
                style = htmlStyle.heading(1).copy(
                    fontSize = titleSize.sp,
                    lineHeight = (titleSize * 1.2f).sp,
                ),
                textAlign = if (titleAlignment == TextAlignment.CENTER) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
            val timeFormats = LocalTimeFormats.current
            val byline = article.byline(context, timeFormats)
            if (!byline.isNullOrBlank()) {
                Text(
                    text = byline,
                    style = htmlStyle.body.copy(
                        color = LocalContentColor.current.copy(alpha = 0.7f),
                        fontSize = (htmlStyle.body.fontSize.value * 0.9f).sp,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            val feedName = article.displayFeedName(context)
            if (feedName.isNotBlank()) {
                Text(
                    text = feedName,
                    style = htmlStyle.body.copy(
                        color = LocalContentColor.current.copy(alpha = 0.7f),
                        fontSize = (htmlStyle.body.fontSize.value * 0.9f).sp,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            HtmlContent(
                document = document,
                style = htmlStyle,
                modifier = Modifier.fillMaxWidth(),
                onClick = { click ->
                    when (click) {
                        is HtmlClick.Link -> onLinkClick(click.href)
                        is HtmlClick.Image -> openMedia(click.src)
                        else -> Unit
                    }
                },
                onLongClick = { longClick ->
                    when (longClick) {
                        is HtmlLongClick.Link -> onRequestLinkDialog(
                            ShareLink(text = longClick.text, url = longClick.href)
                        )
                        is HtmlLongClick.Image -> onRequestImageDialog(longClick.src)
                    }
                },
            )
            Spacer(Modifier.height(64.dp))
        }
    }
}
