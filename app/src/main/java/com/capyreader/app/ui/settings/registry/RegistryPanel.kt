package com.capyreader.app.ui.settings.registry

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.settings.panels.SettingsPanel

@Composable
fun RegistryPanel(
    panel: SettingsPanel,
    environment: SettingsEnvironment,
    highlighted: Setting?,
    onHighlightShown: () -> Unit,
    onRemoveAccount: () -> Unit,
) {
    val sections = remember(panel, environment) {
        SettingsRegistry.sections(panel, environment)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        sections.forEach { section ->
            FormSection(title = section.title?.let { stringResource(it) }) {
                Column {
                    section.settings.forEach { setting ->
                        HighlightableSetting(
                            highlighted = setting == highlighted,
                            onHighlightShown = onHighlightShown,
                        ) {
                            SettingContent(
                                setting = setting,
                                onRemoveAccount = onRemoveAccount,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HighlightableSetting(
    highlighted: Boolean,
    onHighlightShown: () -> Unit,
    content: @Composable () -> Unit,
) {
    val requester = remember { BringIntoViewRequester() }
    val highlightAlpha = remember { Animatable(0f) }
    val highlightColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(highlighted) {
        if (!highlighted) {
            return@LaunchedEffect
        }

        withFrameNanos {}
        requester.bringIntoView()
        highlightAlpha.animateTo(0.16f, tween(durationMillis = 200))
        highlightAlpha.animateTo(0f, tween(durationMillis = 1200, delayMillis = 600))
        onHighlightShown()
    }

    Box(
        modifier = Modifier
            .bringIntoViewRequester(requester)
            .drawWithContent {
                drawContent()
                drawRect(highlightColor.copy(alpha = highlightAlpha.value))
            }
    ) {
        content()
    }
}
