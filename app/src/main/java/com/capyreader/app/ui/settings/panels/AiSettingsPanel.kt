package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.RowItem
import com.capyreader.app.preferences.AiProvider
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsPanel(
    viewModel: AiSettingsViewModel = koinViewModel(),
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.ai_delete_all_summaries)) },
            text = { Text(stringResource(R.string.ai_delete_all_summaries_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllAiSummaries()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.ai_delete_all_summaries))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        FormSection(title = stringResource(R.string.settings_ai_summary_title)) {
            RowItem {
                TextSwitch(
                    checked = viewModel.enableAiSummaries,
                    onCheckedChange = viewModel::updateEnableAiSummaries,
                    title = stringResource(R.string.settings_ai_summary_enable)
                )
            }
        }

        if (viewModel.enableAiSummaries) {
            FormSection(title = stringResource(R.string.ai_provider_label)) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiProviderGrid(
                        selectedProvider = viewModel.aiProvider,
                        onProviderSelected = viewModel::updateAiProvider
                    )
                }
            }

            FormSection(title = stringResource(R.string.ai_configuration_section)) {
                ApiKeyField(
                    value = viewModel.aiApiKey,
                    onValueChange = viewModel::updateAiApiKey
                )

                if (viewModel.aiProvider == AiProvider.CUSTOM) {
                    RowItem {
                        OutlinedTextField(
                            value = viewModel.aiBaseUrl,
                            onValueChange = viewModel::updateAiBaseUrl,
                            label = { Text(stringResource(R.string.settings_ai_base_url)) },
                            supportingText = { Text(stringResource(R.string.settings_ai_base_url_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                } else {
                    RowItem {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.settings_ai_base_url),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = viewModel.aiBaseUrl,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                RowItem {
                    ModelSelector(
                        selectedModel = viewModel.aiModel,
                        models = viewModel.models,
                        onModelSelected = viewModel::updateAiModel,
                        isFetching = viewModel.isFetchingModels
                    )
                }
            }

            FormSection(title = stringResource(R.string.settings_ai_system_prompt)) {
                RowItem {
                    OutlinedTextField(
                        value = viewModel.aiSystemPrompt,
                        onValueChange = viewModel::updateAiSystemPrompt,
                        label = { Text(stringResource(R.string.settings_ai_system_prompt)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            RowItem {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.ai_delete_all_summaries))
                }
            }
        }
    }
}

@Composable
private fun ApiKeyField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    RowItem {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.settings_ai_api_key)) },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = stringResource(
                            if (visible) R.string.ai_api_key_hide else R.string.ai_api_key_show
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun AiProviderGrid(
    selectedProvider: AiProvider,
    onProviderSelected: (AiProvider) -> Unit
) {
    val providers = AiProvider.entries

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        providers.chunked(2).forEach { rowProviders ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowProviders.forEach { provider ->
                    ProviderCard(
                        provider = provider,
                        isSelected = selectedProvider == provider,
                        onClick = { onProviderSelected(provider) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowProviders.size < 2) {
                    repeat(2 - rowProviders.size) {
                        Box(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private data class ProviderVisuals(
    val title: String,
    val iconRes: Int?,
    val bgColor: Color,
)

@Composable
private fun providerVisuals(provider: AiProvider): ProviderVisuals {
    return when (provider) {
        AiProvider.OPENAI -> ProviderVisuals(
            title = stringResource(R.string.ai_provider_openai),
            iconRes = R.drawable.ai_logo_openai,
            bgColor = Color(0xFF000000)
        )
        AiProvider.GOOGLE -> ProviderVisuals(
            title = stringResource(R.string.ai_provider_google),
            iconRes = R.drawable.ai_logo_gemini,
            bgColor = Color(0xFF4285F4)
        )
        AiProvider.ANTHROPIC -> ProviderVisuals(
            title = stringResource(R.string.ai_provider_anthropic),
            iconRes = R.drawable.ai_logo_claude,
            bgColor = Color(0xFFD97757)
        )
        AiProvider.CUSTOM -> ProviderVisuals(
            title = stringResource(R.string.ai_provider_custom),
            iconRes = null,
            bgColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ProviderCard(
    provider: AiProvider,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visuals = providerVisuals(provider)

    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(visuals.bgColor),
                contentAlignment = Alignment.Center
            ) {
                if (visuals.iconRes != null) {
                    Image(
                        painter = painterResource(visuals.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Text(
                text = visuals.title,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelector(
    selectedModel: String,
    models: List<String>,
    onModelSelected: (String) -> Unit,
    isFetching: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = when {
                isFetching -> stringResource(R.string.ai_models_loading)
                else -> selectedModel
            },
            onValueChange = { onModelSelected(it) },
            label = { Text(stringResource(R.string.settings_ai_model)) },
            placeholder = {
                if (!isFetching && models.isEmpty()) {
                    Text(stringResource(R.string.ai_models_enter_manually))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            readOnly = models.isNotEmpty() || isFetching
        )

        if (models.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                models.forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model) },
                        onClick = {
                            onModelSelected(model)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

