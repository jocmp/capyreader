package com.capyreader.app.ui.articles.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun EmptyOnboardingView(
    addFeed: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.empty_onboarding_title),
                color = colorScheme.secondary
            )
            Text(
                stringResource(R.string.empty_onboarding_prompt),
                color = colorScheme.secondary
            )
            addFeed()
        }
    }
}

@Preview
@Composable
private fun EmptyOnboardingViewPreview() {
    CapyTheme {
        EmptyOnboardingView(
            addFeed = {
                OutlinedButton(onClick = { }) {
                    Text("+ Add Feed")
                }
            }
        )
    }
}
