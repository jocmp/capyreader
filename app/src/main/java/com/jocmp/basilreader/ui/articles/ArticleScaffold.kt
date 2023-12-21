package com.jocmp.basilreader.ui.articles

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basilreader.ui.theme.BasilReaderTheme

@Composable
fun ArticleScaffold(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    list: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                list()
            }
        }
    ) {
        content()
    }
}

@Preview(device = Devices.FOLDABLE)
@Composable
fun ArticlesLayoutPreview() {
    BasilReaderTheme {
        ArticleScaffold(
            list = {
                Text("List here!")
            }
        ) {
            Text("Content here...")
        }
    }
}
