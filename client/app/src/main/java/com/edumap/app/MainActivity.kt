package com.edumap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.edumap.app.ui.theme.EduMapTheme
import kotlinx.coroutines.delay

import com.edumap.app.notifications.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannels(this)
        enableEdgeToEdge()
        setContent {
            val preferences = remember { getSharedPreferences(APP_SETTINGS, MODE_PRIVATE) }
            var themeMode by remember {
                mutableStateOf(
                    AppThemeMode.fromStorage(preferences.getString(THEME_MODE_KEY, null))
                )
            }
            val darkTheme = when (themeMode) {
                AppThemeMode.System -> isSystemInDarkTheme()
                AppThemeMode.Light -> false
                AppThemeMode.Dark -> true
            }
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(900)
                showSplash = false
            }

            EduMapTheme(darkTheme = darkTheme) {
                SideEffect {
                    val systemBarColor = if (darkTheme) Color(0xFF0B1120) else Color(0xFFF8FAFC)
                    window.statusBarColor = systemBarColor.toArgb()
                    window.navigationBarColor = systemBarColor.toArgb()
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }
                if (showSplash) {
                    EduMapSplashBanner(darkTheme = darkTheme)
                } else {
                    EduMapApp(
                        themeMode = themeMode,
                        onThemeModeChange = { nextMode ->
                            themeMode = nextMode
                            preferences.edit().putString(THEME_MODE_KEY, nextMode.storageValue).apply()
                        }
                    )
                }
            }
        }
    }

    private companion object {
        const val APP_SETTINGS = "edumap_settings"
        const val THEME_MODE_KEY = "theme_mode"
    }
}

@androidx.compose.runtime.Composable
private fun EduMapSplashBanner(darkTheme: Boolean) {
    val background = if (darkTheme) Color(0xFF081A22) else Color(0xFFF1FCF8)
    val titleColor = if (darkTheme) Color(0xFFF1FCF8) else Color(0xFF163A53)
    val subtitleColor = if (darkTheme) Color(0xFFA9C4BC) else Color(0xFF4E6476)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.edumap_splash_icon),
            contentDescription = "EduMap"
        )
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(id = R.drawable.edumap_splash_branding),
            contentDescription = "EduMap banner",
            modifier = Modifier.size(width = 280.dp, height = 64.dp)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Учебная программа и материалы в одном месте",
            style = MaterialTheme.typography.titleMedium,
            color = titleColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Открываем EduMap",
            style = MaterialTheme.typography.bodyMedium,
            color = subtitleColor
        )
    }
}
