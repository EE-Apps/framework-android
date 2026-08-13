package com.eenot.core

import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * Базовый Activity для всех WebView-приложений.
 * Каждое конкретное приложение наследуется от него.
 */
abstract class BaseWebViewActivity : AppCompatActivity() {

    protected lateinit var webView: WebView

    // Абстрактные методы — каждое приложение реализует по-своему
    abstract fun getTrustedHosts(): List<String>
    abstract fun getDefaultSettingsAssetName(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_webview)

        webView = findViewById(R.id.webView)

        initSettingsIfNeeded()
        setupWebView()
        setupBackButton()
        loadAvailableUrl()
    }

    // ── Настройка WebView ──────────────────────────────────

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            allowUniversalAccessFromFileURLs = true
            allowFileAccessFromFileURLs = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(false)
        }

        webView.addJavascriptInterface(
            WebAppInterface(this, webView, lifecycleScope),
            "AndroidBridge"
        )

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                Log.d("WebViewJS", "${msg.message()} [${msg.lineNumber()}]")
                return true
            }
        }

        val trusted = getTrustedHosts()

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                if (trusted.any { url.contains(it) } || url.startsWith("file://")) {
                    return false
                }
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                return true
            }

            override fun onReceivedSslError(
                view: WebView, handler: SslErrorHandler, error: SslError
            ) {
                handler.proceed() // Для dev-серверов с самоподписанными cert
            }
        }
    }

    // ── Кнопка "Назад" ─────────────────────────────────────

    private fun setupBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // ── Инициализация settings.json ────────────────────────

    private fun initSettingsIfNeeded() {
        val settingsFile = File(getExternalFilesDir(null), "settings.json")
        if (!settingsFile.exists()) {
            try {
                assets.open(getDefaultSettingsAssetName()).use { input ->
                    settingsFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e("Core", "Нет файла настроек в assets", e)
            }
        }
    }

    // ── Выбор и загрузка URL ───────────────────────────────

    private fun loadAvailableUrl() {
        lifecycleScope.launch {
            val settingsFile = File(getExternalFilesDir(null), "settings.json")
            val json = if (settingsFile.exists()) {
                try { JSONObject(settingsFile.readText()) } catch (e: Exception) { JSONObject() }
            } else JSONObject()

            val urls = mutableListOf<String>()
            if (json.has("urls")) {
                val arr = json.getJSONArray("urls")
                for (i in 0 until arr.length()) urls.add(arr.getString(i))
            }

            val availableUrl = if (urls.isNotEmpty()) {
                NetworkUtils.getFirstAvailableUrl(urls)
            } else null

            withContext(Dispatchers.Main) {
                if (availableUrl != null) {
                    webView.loadUrl(availableUrl)
                } else {
                    showOfflineScreen()
                }
            }
        }
    }

    private fun showOfflineScreen() {
        val html = """
            <html><body style="display:flex;justify-content:center;align-items:center;
            height:100vh;font-family:sans-serif;text-align:center;background:#1a1a2e;">
            <div style="color:#eee;">
                <h1>Нет соединения</h1>
                <p>Ни один сервер не отвечает.</p>
            </div>
            </body></html>
        """.trimIndent()
        webView.loadData(html, "text/html", "UTF-8")
    }
}