package com.capyreader.app.ui.articles.feeds

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.ui.theme.CapyTheme
import com.capyreader.app.ui.theme.LocalAppTheme

@Composable
fun DrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    icon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    shape: Shape = CircleShape,
) {
    val colors = NavigationDrawerItemDefaults.colors()
    val mappedColors = colors.mapToTheme(selected = selected)

    Surface(
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                role = Role.Tab,
            ),
        shape = shape,
        color = mappedColors.containerColor,
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                CompositionLocalProvider(
                    LocalContentColor provides mappedColors.iconColor,
                    content = icon
                )
                Spacer(Modifier.width(12.dp))
            }
            Box(Modifier.weight(1f)) {
                CompositionLocalProvider(
                    LocalContentColor provides mappedColors.textColor,
                    content = label
                )
            }
            if (badge != null) {
                Spacer(Modifier.width(12.dp))
                CompositionLocalProvider(
                    LocalContentColor provides mappedColors.badgeColor,
                    content = badge
                )
            }
        }
    }
}

private data class DrawerColors(
    val containerColor: Color,
    val iconColor: Color,
    val textColor: Color,
    val badgeColor: Color,
)

@Composable
private fun NavigationDrawerItemColors.mapToTheme(
    selected: Boolean,
): DrawerColors {
    val isMonochrome = LocalAppTheme.current == AppTheme.MONOCHROME
    val useMonochromeSelected = selected && isMonochrome
    val unselectedTextColor = textColor(false).value
    val surfaceColor = MaterialTheme.colorScheme.surface

    return DrawerColors(
        containerColor = if (useMonochromeSelected) {
            unselectedTextColor
        } else {
            containerColor(selected).value
        },
        iconColor = if (useMonochromeSelected) {
            surfaceColor
        } else {
            iconColor(selected).value
        },
        textColor = if (useMonochromeSelected) {
            surfaceColor
        } else {
            textColor(selected).value
        },
        badgeColor = if (useMonochromeSelected) {
            surfaceColor
        } else {
            badgeColor(selected).value
        },
    )
}

@Preview
@Composable
fun DrawerItemSelectedPreview() {
    CapyTheme {
        DrawerItem(
            label = { Text("Title Goes Here") },
            selected = true,
            onClick = {},
            onLongClick = {},
        )
    }
}

@Preview
@Composable
fun DrawerItemSelectedMonochromePreview() {
    CapyTheme(AppTheme.MONOCHROME) {
        DrawerItem(
            label = { Text("Title Goes Here") },
            selected = true,
            onClick = {},
            onLongClick = {},
        )
    }
}

@Preview
@Composable
fun DrawerItemPreview() {
    CapyTheme {
        DrawerItem(
            label = { Text("Title Goes Here") },
            selected = false,
            onClick = {},
            onLongClick = {},
        )
    }
}
