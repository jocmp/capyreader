package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jocmp.basilreader.R

@Composable
fun AddFeedButton(
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = { onClick() },
    ) {
        Box(Modifier.padding(end = 8.dp)) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(stringResource(R.string.nav_add_feed))
    }
}
