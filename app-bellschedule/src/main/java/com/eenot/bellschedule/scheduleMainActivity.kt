package com.eenot.bellschedule

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.eenot.bellschedule.ui.theme.BellScheduleTheme
import com.eenot.core.BaseWebViewActivity

class ScheduleMainActivity : BaseWebViewActivity() {

    // Кэшируем актуальные отступы
    private var cachedTopDp: Float = 0f
    private var cachedBottomDp: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            BellScheduleTheme {
                AndroidView<WebView>(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        (webView.parent as? ViewGroup)?.removeView(webView)

                        // 1. Учитываем вырезы экрана (Cutout) + Статус-бар
                        ViewCompat.setOnApplyWindowInsetsListener(webView) { _, insets ->
                            val systemBars = insets.getInsets(
                                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                            )

                            val density = context.resources.displayMetrics.density
                            cachedTopDp = systemBars.top / density
                            cachedBottomDp = systemBars.bottom / density

                            applySafeAreaToWebView(webView)

                            WindowInsetsCompat.CONSUMED
                        }

                        // 2. Инжектим отступы ПОВТОРНО при каждом успешном рендере страницы
                        val originalClient = webView.webViewClient
                        webView.webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                originalClient?.onPageFinished(view, url)
                                applySafeAreaToWebView(view)
                            }
                        }

                        webView
                    }
                )
            }
        }
    }

    private fun applySafeAreaToWebView(targetWebView: WebView?) {
        if (targetWebView == null || cachedTopDp == 0f) return

        val js = """
            (function() {
                document.documentElement.style.setProperty('--safe-area-top', '${cachedTopDp}px');
                document.documentElement.style.setProperty('--safe-area-bottom', '${cachedBottomDp}px');
            })();
        """.trimIndent()

        targetWebView.evaluateJavascript(js, null)
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