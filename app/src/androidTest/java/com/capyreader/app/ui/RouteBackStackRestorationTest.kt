package com.capyreader.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * `rememberNavBackStack` relies on kotlinx.serialization's built-in sealed-class polymorphism to
 * save/restore [Route] entries (no explicit SerializersModule is registered anywhere in the app).
 * This pushes a route, simulates process death, and confirms the pushed entry (not just the
 * initial start route) survives - proving restoration actually replays state rather than
 * reconstructing from the composable's initial arguments.
 */
@RunWith(AndroidJUnit4::class)
class RouteBackStackRestorationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pushedRouteSurvivesSimulatedProcessDeath() {
        val restorationTester = StateRestorationTester(composeTestRule)

        restorationTester.setContent {
            val backStack = rememberNavBackStack(
                Route.ArticleList(ArticleFilter.Feeds("feed-1", null, ArticleStatus.UNREAD))
            )
            Column {
                Text("size:${backStack.size}", modifier = Modifier.testTag("size"))
                Button(
                    modifier = Modifier.testTag("push"),
                    onClick = { backStack.add(Route.ArticleDetail("article-1")) },
                ) {
                    Text("Push")
                }
            }
        }

        composeTestRule.onNodeWithTag("push").performClick()
        composeTestRule.onNodeWithTag("size").assertTextEquals("size:2")

        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.onNodeWithTag("size").assertTextEquals("size:2")
    }
}
