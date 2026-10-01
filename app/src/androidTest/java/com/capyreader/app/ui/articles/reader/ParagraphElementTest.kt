package com.capyreader.app.ui.articles.reader

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jocmp.mallet.LinearText
import com.jocmp.mallet.LinearTextAnnotation
import com.jocmp.mallet.LinearTextAnnotationH2
import com.jocmp.mallet.LinearTextBlockStyle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParagraphElementTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

    @Test
    fun headingIsMarkedAsHeading() {
        val text = "Section title"

        composeTestRule.setContent {
            ParagraphElement(
                linearText = LinearText(
                    ids = emptySet(),
                    text = text,
                    blockStyle = LinearTextBlockStyle.TEXT,
                    LinearTextAnnotation(LinearTextAnnotationH2, 0, text.length),
                ),
                idToIndex = emptyMap(),
                actions = ReaderActions.none,
            )
        }

        composeTestRule.onNodeWithText(text).assert(isHeading)
    }

    @Test
    fun paragraphIsNotMarkedAsHeading() {
        val text = "Body copy"

        composeTestRule.setContent {
            ParagraphElement(
                linearText = LinearText(
                    ids = emptySet(),
                    text = text,
                    blockStyle = LinearTextBlockStyle.TEXT,
                ),
                idToIndex = emptyMap(),
                actions = ReaderActions.none,
            )
        }

        composeTestRule.onNodeWithText(text).assert(!isHeading)
    }
}
