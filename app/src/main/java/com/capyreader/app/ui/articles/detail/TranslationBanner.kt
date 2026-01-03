package com.capyreader.app.ui.articles.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.translation.TranslationState
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun TranslationBanner(
    state: TranslationState,
    sourceLanguage: String,
    onTranslate: () -> Unit,
    onShowOriginal: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = state != TranslationState.NONE && state != TranslationState.DETECTING

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp
        ) {
            when (state) {
                TranslationState.AVAILABLE,
                TranslationState.DOWNLOADING,
                TranslationState.TRANSLATING -> TranslationOffer(
                    sourceLanguage = sourceLanguage,
                    isLoading = state == TranslationState.DOWNLOADING || state == TranslationState.TRANSLATING,
                    onTranslate = onTranslate,
                    onDismiss = onDismiss
                )
                TranslationState.TRANSLATED -> TranslationComplete(
                    onShowOriginal = onShowOriginal
                )
                TranslationState.ERROR -> TranslationError(
                    onRetry = onTranslate,
                    onDismiss = onDismiss
                )
                else -> {}
            }
        }
    }
}

@Composable
private fun TranslationOffer(
    sourceLanguage: String,
    isLoading: Boolean,
    onTranslate: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Translate,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.translation_offer, sourceLanguage),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Text(stringResource(R.string.translation_dismiss))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onTranslate,
                enabled = !isLoading,
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.translation_translate))
                }
            }
        }
    }
}

@Composable
private fun TranslationComplete(
    onShowOriginal: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Translate,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.translation_viewing_translated),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        TextButton(onClick = onShowOriginal) {
            Text(stringResource(R.string.translation_show_original))
        }
    }
}

@Composable
private fun TranslationError(
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.translation_error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        Row {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.translation_dismiss))
            }
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.translation_retry))
            }
        }
    }
}

@Preview
@Composable
private fun TranslationOfferPreview() {
    CapyTheme {
        TranslationBanner(
            state = TranslationState.AVAILABLE,
            sourceLanguage = "German",
            onTranslate = {},
            onShowOriginal = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun TranslationLoadingPreview() {
    CapyTheme {
        TranslationBanner(
            state = TranslationState.DOWNLOADING,
            sourceLanguage = "German",
            onTranslate = {},
            onShowOriginal = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun TranslationCompletePreview() {
    CapyTheme {
        TranslationBanner(
            state = TranslationState.TRANSLATED,
            sourceLanguage = "German",
            onTranslate = {},
            onShowOriginal = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun TranslationErrorPreview() {
    CapyTheme {
        TranslationBanner(
            state = TranslationState.ERROR,
            sourceLanguage = "German",
            onTranslate = {},
            onShowOriginal = {},
            onDismiss = {}
        )
    }
}
