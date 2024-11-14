package com.capyreader.app.ui.articles.media

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import coil.request.ImageRequest
import com.capyreader.app.ui.components.LoadingView
import com.capyreader.app.ui.components.Swiper
import com.capyreader.app.ui.components.rememberSwiperState
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

@Composable
fun ArticleMediaView(
    onDismissRequest: () -> Unit,
    url: String?
) {
    val view = LocalView.current

    var showError by rememberSaveable { mutableStateOf(false) }

    val swiperState = rememberSwiperState(
        onDismiss = {
            onDismissRequest()
        }
    )

    val imageState = rememberZoomableImageState(
        rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f))
    )

    Scaffold(
        containerColor = Color.Black.copy(alpha = 1f - swiperState.progress),
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            Swiper(
                state = swiperState,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ZoomableAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .listener(
                            onError = { _, _ ->
                                if (url != null) {
                                    showError = true
                                }
                            },
                        )
                        .build(),
                    state = imageState,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    alignment = Alignment.Center,
                )

                if (!imageState.isImageDisplayed && !showError) {
                    LoadingView()
                } else if (showError) {
                    ImageErrorView()
                }
            }

            CloseButton(
                onClick = { onDismissRequest() },
                visible = swiperState.progress == 0f
            )
        }
    }

    SideEffect {
        val window = (view.context as Activity).window

        window.navigationBarColor = Color.Black.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }

    DisposableEffect(url) {
        val window = (view.context as Activity).window

        val previousColor = window.navigationBarColor
        val previousAppearanceLightStatusBars =
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars

        onDispose {
            window.navigationBarColor = previousColor
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                previousAppearanceLightStatusBars
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
