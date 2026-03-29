package com.jocmp.capyreader.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.File
import javax.swing.UIManager

private val LightColors = lightColorScheme(
    primary = Color(0xFF3B6837),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBCF0B4),
    onPrimaryContainer = Color(0xFF002200),
    secondary = Color(0xFF526350),
    secondaryContainer = Color(0xFFD5E8CF),
    surface = Color(0xFFFCFDF7),
    surfaceVariant = Color(0xFFDEE5D9),
    background = Color(0xFFFCFDF7),
    onSurface = Color(0xFF1A1C19),
    onSurfaceVariant = Color(0xFF434846),
    outline = Color(0xFF737873),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA1D49A),
    onPrimary = Color(0xFF0A390E),
    primaryContainer = Color(0xFF235022),
    onPrimaryContainer = Color(0xFFBCF0B4),
    secondary = Color(0xFFB9CCB4),
    secondaryContainer = Color(0xFF3A4B39),
    surface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFF434846),
    background = Color(0xFF1A1C19),
    onSurface = Color(0xFFE2E3DD),
    onSurfaceVariant = Color(0xFFC2C9BD),
    outline = Color(0xFF8C9389),
)

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    application {
        val dataDir = File(System.getProperty("user.home"), ".capyreader").apply { mkdirs() }
        val appState = remember { AppState(dataDir) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Capy Reader",
            state = rememberWindowState(width = 1200.dp, height = 800.dp),
        ) {
            val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors
            val typography = platformTypography()

            MaterialTheme(colorScheme = colorScheme, typography = typography) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var account by remember { mutableStateOf(appState.loadAccount()) }

                    val currentAccount = account
                    if (currentAccount == null) {
                        LoginScreen(
                            appState = appState,
                            onAccountCreated = {
                                account = appState.loadAccount()
                            },
                        )
                    } else {
                        val scope = rememberCoroutineScope()
                        val readerState = remember(currentAccount.id) {
                            ReaderState(account = currentAccount, scope = scope)
                        }
                        ReaderScreen(
                            state = readerState,
                            onSignOut = {
                                val id = appState.savedAccountID()
                                if (id != null) {
                                    appState.manager.removeAccount(id)
                                    appState.clearAccountID()
                                }
                                account = null
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun platformTypography(): Typography {
    val fontFamily = platformFontFamily()
    val defaults = Typography()

    return Typography(
        displayLarge = defaults.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = defaults.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = defaults.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = defaults.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = defaults.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = defaults.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = defaults.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = defaults.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = defaults.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = defaults.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = defaults.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = defaults.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = defaults.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = defaults.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = defaults.labelSmall.copy(fontFamily = fontFamily),
    )
}

@Composable
private fun platformFontFamily(): FontFamily {
    val os = System.getProperty("os.name").orEmpty().lowercase()

    return when {
        os.contains("win") -> FontFamily(Font("Segoe UI"))
        os.contains("mac") -> FontFamily(Font(".AppleSystemUIFont"))
        else -> FontFamily.Default
    }
}
