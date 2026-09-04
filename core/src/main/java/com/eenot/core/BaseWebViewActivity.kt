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
import org.json.JSONArray
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
        try {
            val assetJsonString = assets.open(getDefaultSettingsAssetName()).bufferedReader().use { it.readText() }

            if (!settingsFile.exists()) {
                Log.d("Core", "Инициализация settings.json из assets")
                settingsFile.writeText(assetJsonString)
            } else {
                val currentJson = try { JSONObject(settingsFile.readText()) } catch (e: Exception) { JSONObject() }
                val newDefaultJson = try { JSONObject(assetJsonString) } catch (e: Exception) { JSONObject() }

                val currentUrlsArray = currentJson.optJSONArray("urls")
                val defaultUrlsArray = newDefaultJson.optJSONArray("urls")

                // Сравниваем массивы логически (по элементам), а не по форматированию строки
                if (!areJsonArraysEqual(currentUrlsArray, defaultUrlsArray)) {
                    Log.d("Core", "Список URL в assets изменился, обновляем только поле 'urls'")

                    // Сохраняем пользовательские настройки, обновляя ТОЛЬКО ключ "urls"
                    if (defaultUrlsArray != null) {
                        currentJson.put("urls", defaultUrlsArray)
                        settingsFile.writeText(currentJson.toString(2))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Core", "Ошибка инициализации настроек", e)
        }
    }

    // Вспомогательный метод для корректного сравнения JSONArray
    private fun areJsonArraysEqual(arr1: JSONArray?, arr2: JSONArray?): Boolean {
        if (arr1 == null && arr2 == null) return true
        if (arr1 == null || arr2 == null) return false
        if (arr1.length() != arr2.length()) return false

        val list1 = (0 until arr1.length()).map { arr1.opt(it) }
        val list2 = (0 until arr2.length()).map { arr2.opt(it) }

        return list1 == list2
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

            Log.d("Core", "Checking URLs: $urls")
            val availableUrl = if (urls.isNotEmpty()) {
                NetworkUtils.getFirstAvailableUrl(urls)
            } else null
            Log.d("Core", "Selected URL: $availableUrl")

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