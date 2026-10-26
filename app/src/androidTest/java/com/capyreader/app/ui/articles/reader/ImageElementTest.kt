package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jocmp.mallet.LinearImage
import com.jocmp.mallet.LinearImageSource
import com.jocmp.mallet.TableData
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageElementTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val spacer = LinearImage(
        ids = emptySet(),
        sources = listOf(
            LinearImageSource(
                imgUri = "https://example.com/spacer.gif",
                widthPx = 200,
                heightPx = 1,
                pixelDensity = null,
                screenWidth = null,
            ),
        ),
        caption = null,
        link = null,
    )

    @Test
    fun spacerImageFitsViewportWidth() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalReaderPageHeight provides 800.dp) {
                ImageElement(
                    image = spacer,
                    idToIndex = emptyMap(),
                    actions = ReaderActions.none,
                    modifier = Modifier.testTag("image"),
                )
            }
        }

        assertFitsRoot()
    }

    @Test
    fun spacerImageInCellFitsViewportWidth() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalReaderPageHeight provides 800.dp) {
                TableLayout(
                    tableData = TableData(row = 1, column = 1),
                    allowHorizontalScroll = true,
                    modifier = Modifier.fillMaxWidth(),
                ) { _, _ ->
                    ImageElement(
                        image = spacer,
                        idToIndex = emptyMap(),
                        actions = ReaderActions.none,
                        modifier = Modifier.testTag("image"),
                    )
                }
            }
        }

        assertFitsRoot()
    }

    private fun assertFitsRoot() {
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val imageBounds = composeTestRule.onNodeWithTag("image").getBoundsInRoot()
        val rootWidth = rootBounds.right - rootBounds.left
        val imageWidth = imageBounds.right - imageBounds.left

        assertTrue("image $imageWidth exceeds root $rootWidth", imageWidth <= rootWidth)
    }
}
