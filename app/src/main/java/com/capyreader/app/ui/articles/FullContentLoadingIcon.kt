package com.capyreader.app.ui.articles

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FullContentLoadingIcon() {
    var flipped by remember { mutableStateOf(false) }

    Icon(
        painterResource(id = R.drawable.icon_article_loading),
        contentDescription = null,
        modifier = if (flipped) {
            Modifier.scale(scaleX = -1f, scaleY = 1f)
        } else {
            Modifier
        }
    )

    LaunchedEffect(flipped) {
        launch {
            delay(500.milliseconds)
            flipped = !flipped
        }
    }
}

@Preview
@Composable
private fun FullContentIconPreview() {
    FullContentLoadingIcon()
}
