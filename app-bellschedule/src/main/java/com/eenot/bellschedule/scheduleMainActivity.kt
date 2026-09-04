package com.eenot.bellschedule

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.eenot.core.BaseWebViewActivity
import com.eenot.bellschedule.ui.theme.BellScheduleTheme

class ScheduleMainActivity : BaseWebViewActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Включаем edge-to-edge для современного вида
        enableEdgeToEdge()

        // Подключаем Compose тему и разворачиваем на весь экран
        setContent {
            BellScheduleTheme {
                AndroidView<WebView>(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        // Извлекаем webView из базового макета и помещаем в Compose
                        (webView.parent as? ViewGroup)?.removeView(webView)
                        webView
                    }
                )
            }
        }
    }

    // Какие домены открывать внутри WebView
    override fun getTrustedHosts(): List<String> {
        return listOf(
            "http://192.168.100.18:5500",
            "http://95.153.88.229:8080/ee-apps/web/bellschedule/",
            "https://ee-apps.github.io/web/bellschedule",
        )
    }

    // Имя файла настроек в assets
    override fun getDefaultSettingsAssetName(): String {
        return "default_settings.json"
    }
}
