package com.eenot.bellschedule

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import com.eenot.core.BaseWebViewActivity
import com.eenot.bellschedule.ui.theme.BellScheduleTheme

class ScheduleMainActivity : BaseWebViewActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Включаем Edge-to-Edge и отключаем принудительную подгонку окон системой
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            BellScheduleTheme {
                AndroidView<WebView>(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        (webView.parent as? ViewGroup)?.removeView(webView)

                        // 2. Слушаем отступы Android и инжектим их в CSS-переменные WebView
                        ViewCompat.setOnApplyWindowInsetsListener(webView) { _, insets ->
                            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                            val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

                            val density = context.resources.displayMetrics.density
                            val topDp = statusBarHeight / density
                            val bottomDp = navigationBarHeight / density

                            // Передаем значения в root DOM
                            val js = """
                                document.documentElement.style.setProperty('--safe-area-top', '${topDp}px');
                                document.documentElement.style.setProperty('--safe-area-bottom', '${bottomDp}px');
                            """.trimIndent()

                            webView.evaluateJavascript(js, null)

                            // Игнорируем вкладывание отступов контейнером Android,
                            // чтобы WebView остался физически на весь экран
                            WindowInsetsCompat.CONSUMED
                        }

                        webView
                    }
                )
            }
        }
    }

    override fun getTrustedHosts(): List<String> {
        return listOf(
            "http://192.168.100.18:5500",
            "http://95.153.88.229:8080/ee-apps/web/bellschedule/",
            "https://ee-apps.github.io/web/bellschedule",
        )
    }

    override fun getDefaultSettingsAssetName(): String {
        return "default_settings.json"
    }
}