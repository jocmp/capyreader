package com.capyreader.app.ui.articles.media

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil.request.ImageRequest
import com.capyreader.app.common.Media
import com.capyreader.app.common.MediaItem
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferredMaxWidth
import com.capyreader.app.ui.EdgeToEdgeHelper.isEdgeToEdgeAvailable
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.components.LoadingView
import com.capyreader.app.ui.components.Swiper
import com.capyreader.app.ui.components.rememberSwiperState
import com.capyreader.app.ui.isCompact
import com.capyreader.app.ui.settings.LocalSnackbarHost
import com.capyreader.app.ui.theme.CapyTheme
import com.capyreader.app.ui.theme.findStatusBarColor
import com.capyreader.app.ui.theme.showAppearanceLightStatusBars
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import org.koin.compose.koinInject

@Composable
fun ArticleMediaView(
    onDismissRequest: () -> Unit,
    media: Media?,
    appPreferences: AppPreferences = koinInject()
) {
    if (media == null || media.images.isEmpty()) return
    
    val view = LocalView.current
    val images = media.images
    val initialPage = media.currentIndex.coerceIn(0, images.size - 1)
    
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { images.size }
    )

    var showOverlay by rememberSaveable { mutableStateOf(true) }
    val currentImage = images.getOrNull(pagerState.currentPage)

    MediaScaffold(
        onDismissRequest = onDismissRequest,
        showOverlay = showOverlay,
        footer = {
            CaptionOverlay(
                caption = currentImage?.altText?.ifBlank { null },
                imageUrl = currentImage?.url ?: "",
                currentPage = pagerState.currentPage + 1,
                totalPages = images.size
            )
        }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val imageItem = images.getOrNull(page) ?: return@HorizontalPager
            ImagePage(
                imageItem = imageItem,
                onToggleOverlay = { showOverlay = !showOverlay }
            )
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val themeMode by appPreferences.themeMode.collectChangesWithCurrent()
    val showLightStatusBar = themeMode.showAppearanceLightStatusBars()
    val pureBlack by appPreferences.pureBlackDarkMode.collectChangesWithCurrent()

    DisposableEffect(media) {
        val window = (view.context as Activity).window
        val previousNavigationBarColor = window.navigationBarColor

        window.navigationBarColor = Color.Black.toArgb()
        if (!isEdgeToEdgeAvailable()) {
            window.statusBarColor = Color.Black.toArgb()
        }
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

        onDispose {
            window.navigationBarColor = previousNavigationBarColor

            if (!isEdgeToEdgeAvailable()) {
                window.statusBarColor = findStatusBarColor(
                    colorScheme,
                    pureBlack = pureBlack,
                    isLightStatusBar = showLightStatusBar
                ).toArgb()
            }

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                showLightStatusBar
        }
    }
}

@Composable
private fun ImagePage(
    imageItem: MediaItem,
    onToggleOverlay: () -> Unit
) {
    var showError by rememberSaveable(imageItem.url) { mutableStateOf(false) }

    val imageState = rememberZoomableImageState(
        rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f))
    )

    Box(modifier = Modifier.fillMaxSize()) {
        ZoomableAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageItem.url)
                .listener(
                    onError = { _, _ ->
                        showError = true
                    },
                )
                .build(),
            state = imageState,
            contentDescription = imageItem.altText,
            onClick = { onToggleOverlay() },
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.Center,
        )

        if (!imageState.isImageDisplayed && !showError) {
            LoadingView()
        } else if (showError) {
            ImageErrorView()
        }
    }
}

@Composable
fun MediaScaffold(
    onDismissRequest: () -> Unit,
    showOverlay: Boolean,
    footer: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val swiperState = rememberSwiperState(
        onDismiss = {
            onDismissRequest()
        }
    )

    val isOverlayVisible = showOverlay && swiperState.progress == 0f
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = Color.Black.copy(alpha = 1f - swiperState.progress),
        modifier = Modifier
            .fillMaxSize(),
        snackbarHost = {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxSize(),
            ) {
                SnackbarHost(snackbarHostState) { data ->
                    val darkColors = darkColorScheme()
                    Snackbar(
                        data,
                        containerColor = darkColors.inverseSurface,
                        contentColor = darkColors.inverseOnSurface,
                    )
                }
            }
        }
    ) { paddingValues ->
        CompositionLocalProvider(
            LocalSnackbarHost provides snackbarHostState,
        ) {
            Box(
                Modifier.padding(paddingValues)
            ) {
                Swiper(
                    state = swiperState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    content()
                }

                Box(Modifier.align(Alignment.BottomStart)) {
                    AnimatedVisibility(
                        isOverlayVisible,
                        enter = fadeIn() + expandVertically(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        footer()
                    }
                }

                CloseButton(
                    onClick = { onDismissRequest() },
                    visible = isOverlayVisible
                )
            }
        }
    }
}

@Composable
private fun CaptionOverlay(
    caption: String?, 
    imageUrl: String,
    currentPage: Int = 1,
    totalPages: Int = 1
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = if (isCompact()) {
            Alignment.Start
        } else {
            Alignment.CenterHorizontally
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        if (totalPages > 1) {
            Text(
                "$currentPage / $totalPages",
                color = MediaColors.textColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (!caption.isNullOrBlank()) {
            Box(
                Modifier
                    .preferredMaxWidth()
            ) {
                Text(
                    caption,
                    color = MediaColors.textColor,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MediaSaveButton(imageUrl)
            MediaShareButton(imageUrl)
        }
    }
}

@Composable
private fun CloseButton(
    onClick: () -> Unit,
    visible: Boolean,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        CloseIconButton(onClick = { onClick() })
    }
}

@Preview(device = "spec:width=673dp,height=841dp")
@Composable
private fun ArticleMediaViewPreview_Foldable() {
    CapyTheme {
        Box(
            Modifier
                .fillMaxHeight()
                .background(Color.Cyan)
        ) {
            Box(Modifier.align(Alignment.BottomStart)) {
                CaptionOverlay(
                    caption = "A description of the picture you're taking a look at",
                    imageUrl = "http://example.com/test.jpg",
                    currentPage = 2,
                    totalPages = 5
                )
            }
        }
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
private fun ArticleMediaViewPreview_Phone() {
    CapyTheme {
        Box(
            Modifier
                .fillMaxHeight()
                .background(Color.Cyan)
        ) {
            Box(Modifier.align(Alignment.BottomStart)) {
                CaptionOverlay(
                    caption = "A description",
                    imageUrl = "http://example.com/test.jpg",
                    currentPage = 1,
                    totalPages = 3
                )
            }
        }
    }
}


@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun ArticleMediaViewPreview_Tablet() {
    CapyTheme {
        Box(
            Modifier
                .fillMaxHeight()
                .background(Color.Cyan)
        ) {
            Box(Modifier.align(Alignment.BottomStart)) {
                CaptionOverlay(
                    caption = "A description of the picture you're taking a look at",
                    imageUrl = "http://example.com/test.jpg",
                    currentPage = 1,
                    totalPages = 1
                )
            }
        }
    }
}
