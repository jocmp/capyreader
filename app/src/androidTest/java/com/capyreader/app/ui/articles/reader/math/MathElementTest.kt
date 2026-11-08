package com.capyreader.app.ui.articles.reader.math

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.capyreader.app.ui.articles.reader.LocalReaderStyle
import com.capyreader.app.ui.articles.reader.ParagraphElement
import com.capyreader.app.ui.articles.reader.ReaderActions
import com.capyreader.app.ui.articles.reader.ReaderStyle
import com.jocmp.mallet.LinearMath
import com.jocmp.mallet.LinearText
import com.jocmp.mallet.LinearTextAnnotation
import com.jocmp.mallet.LinearTextAnnotationMath
import com.jocmp.mallet.LinearTextBlockStyle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MathElementTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displayMathRendersWhenEnabled() {
        val tex = "F_n = \\frac{1}{\\sqrt{5}} \\left( \\varphi^n - \\psi^n \\right)"

        composeTestRule.setContent {
            Reader(renderMath = true) {
                Display(tex)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithContentDescription(tex).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun displayMathShowsSourceWhenDisabled() {
        val tex = "x^2"

        composeTestRule.setContent {
            Reader(renderMath = false) {
                Display(tex)
            }
        }

        composeTestRule.onNodeWithText(tex).assertExists()
    }

    @Test
    fun invalidTexShowsSource() {
        val tex = "\\frac{1}"

        composeTestRule.setContent {
            Reader(renderMath = true) {
                Display(tex)
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(tex).assertExists()
    }

    /**
     * Saves a sample to the app's files dir for eyeballing when run with
     * `-e mathScreenshot true`.
     */
    @Test
    fun screenshot() {
        val arguments = InstrumentationRegistry.getArguments()
        if (arguments.getString("mathScreenshot") != "true") {
            return
        }
        val samples =
            listOf(
                "F_n = \\frac{1}{\\sqrt{5}} \\left( \\left( \\frac{1 + \\sqrt{5}}{2} \\right)^n - \\left( \\frac{1 - \\sqrt{5}}{2} \\right)^n \\right)",
                "\\begin{pmatrix} F_{n+1} \\\\ F_n \\end{pmatrix} = \\begin{pmatrix} 1 & 1 \\\\ 1 & 0 \\end{pmatrix}^n \\begin{pmatrix} 1 \\\\ 0 \\end{pmatrix}",
                "\\underbrace{1 + \\cdots + 1}_{n} \\xrightarrow{\\text{sum}} n \\tag{1}",
                "\\boxed{\\widehat{abc} = \\int_0^\\infty e^{-x^2} \\, dx}",
            )
        val inline = "Let F_0 = 0 and \\sqrt{x^2+1} grow."

        composeTestRule.setContent {
            Reader(renderMath = true) {
                Column(
                    modifier =
                        Modifier
                            .testTag("math")
                            .background(Color.White)
                            .width(400.dp)
                            .padding(16.dp),
                ) {
                    samples.forEach { Display(it) }
                    ParagraphElement(
                        linearText =
                            LinearText(
                                ids = emptySet(),
                                text = inline,
                                blockStyle = LinearTextBlockStyle.TEXT,
                                LinearTextAnnotation(LinearTextAnnotationMath("F_0 = 0"), 4, 10),
                                LinearTextAnnotation(LinearTextAnnotationMath("\\sqrt{x^2+1}"), 16, 27),
                            ),
                        idToIndex = emptyMap(),
                        actions = ReaderActions.none,
                    )
                }
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithContentDescription(samples.first()).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()

        val bitmap = composeTestRule.onNodeWithTag("math").captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.filesDir, "math.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Composable
    private fun Reader(renderMath: Boolean, content: @Composable () -> Unit) {
        val style = ReaderStyle.default.copy(renderMath = renderMath)
        CompositionLocalProvider(LocalReaderStyle provides style) {
            ProvideTextStyle(style.bodyTextStyle) {
                content()
            }
        }
    }

    @Composable
    private fun Display(tex: String) {
        MathElement(
            math = LinearMath(ids = emptySet(), tex = tex),
            idToIndex = emptyMap(),
            actions = ReaderActions.none,
            allowHorizontalScroll = true,
        )
    }
}
