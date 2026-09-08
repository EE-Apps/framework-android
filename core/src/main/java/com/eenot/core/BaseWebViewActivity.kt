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
import androidx.webkit.WebViewAssetLoader
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

    private var dynamicTrustedHosts: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_webview)

        webView = findViewById(R.id.webView)

        setupWebView()
        setupBackButton()
        
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                initSettingsIfNeeded()
                // Загружаем доверенные хосты из файла
                loadTrustedHosts()
            }
            loadAvailableUrl()
        }
    }

    private fun loadTrustedHosts() {
        val dir = getExternalFilesDir(null) ?: filesDir
        val settingsFile = File(dir, "settings.json")
        if (settingsFile.exists()) {
            try {
                val content = try {
                    settingsFile.readText()
                } catch (e: Exception) {
                    Log.e("Core", "EACCES on read trusted_hosts, fallback to empty", e)
                    return
                }
                val json = JSONObject(content)
                val hosts = mutableListOf<String>()
                if (json.has("trusted_hosts")) {
                    val arr = json.getJSONArray("trusted_hosts")
                    for (i in 0 until arr.length()) hosts.add(arr.getString(i).lowercase())
                }
                // Также добавляем хосты из списка URL
                if (json.has("urls")) {
                    val arr = json.getJSONArray("urls")
                    for (i in 0 until arr.length()) {
                        Uri.parse(arr.getString(i)).host?.let { hosts.add(it.lowercase()) }
                    }
                }
                dynamicTrustedHosts = hosts.distinct()
            } catch (e: Exception) {
                Log.e("Core", "Ошибка загрузки trusted_hosts", e)
            }
        }
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

        val hardcodedTrusted = getTrustedHosts()

        val assetLoader = WebViewAssetLoader.Builder()
            .setDomain("appassets.androidplatform.net")
            .setHttpAllowed(true)
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                val url = request.url
                if (url.host == "appassets.androidplatform.net") {
                    val response = assetLoader.shouldInterceptRequest(url)
                    if (response == null) {
                        Log.w("Core", "AssetLoader returned null for: $url")
                    }
                    return response
                }
                return null
            }

            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString().lowercase()
                val host = request.url.host?.lowercase() ?: ""
                
                // Проверяем: 
                // 1. Входит ли URL в хардкод
                // 2. Входит ли хост в динамический список из настроек
                // 3. Является ли это локальным файлом или внутренним доменом ассетов
                val isTrusted = hardcodedTrusted.any { url.contains(it.lowercase()) } || 
                                dynamicTrustedHosts.any { host.contains(it) || it.contains(host) } ||
                                url.startsWith("file://") ||
                                host == "appassets.androidplatform.net"

                if (isTrusted) {
                    return false
                }
                
                Log.d("Core", "External URL detected: $url. Opening in browser.")
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: Exception) {
                    Log.e("Core", "Failed to open external browser", e)
                }
                return true
            }

            override fun onReceivedSslError(
                view: WebView, handler: SslErrorHandler, error: SslError
            ) {
                handler.proceed() 
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
        val dir = getExternalFilesDir(null) ?: filesDir
        if (!dir.exists()) dir.mkdirs()
        
        val settingsFile = File(dir, "settings.json")
        Log.d("Core", "Используемый путь для настроек: ${settingsFile.absolutePath}")
        try {
            val assetJsonString = assets.open(getDefaultSettingsAssetName()).bufferedReader().use { it.readText() }

            if (!settingsFile.exists()) {
                Log.d("Core", "Инициализация settings.json из assets")
                settingsFile.writeText(assetJsonString)
            } else {
                // Пытаемся прочитать. Если EACCES - это критическая ошибка доступа к файлу.
                var content = try {
                    settingsFile.readText()
                } catch (e: Exception) {
                    Log.e("Core", "Не удалось прочитать файл (EACCES?). Пробуем пересоздать.", e)
                    // Если не смогли прочитать (например, чужой файл), пробуем его удалить
                    try {
                        settingsFile.delete()
                        settingsFile.writeText(assetJsonString)
                        assetJsonString
                    } catch (e2: Exception) {
                        Log.e("Core", "Даже пересоздать не удалось!", e2)
                        null
                    }
                }

                if (content == null) return

                // Миграция: заменяем старые file:///android_asset/ на новый домен
                if (content.contains("file:///android_asset/")) {
                    Log.d("Core", "Миграция старых file:///android_asset/ ссылок в settings.json")
                    content = content.replace("file:///android_asset/", "https://appassets.androidplatform.net/assets/")
                    try {
                        settingsFile.writeText(content)
                    } catch (e: Exception) {
                        Log.e("Core", "Ошибка при записи миграции", e)
                    }
                }

                val currentJson = try { JSONObject(content) } catch (e: Exception) { JSONObject() }
                
                // Если в файле вообще нет URL или массив пустой, тогда берем из assets
                if (!currentJson.has("urls") || currentJson.optJSONArray("urls")?.length() == 0) {
                    Log.d("Core", "В settings.json нет URL, копируем из assets")
                    val newDefaultJson = JSONObject(assetJsonString)
                    currentJson.put("urls", newDefaultJson.optJSONArray("urls"))
                    try {
                        settingsFile.writeText(currentJson.toString(2))
                    } catch (e: Exception) {
                        Log.e("Core", "Ошибка записи при обновлении URL", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Core", "Ошибка инициализации настроек", e)
        }
    }

    // ── Выбор и загрузка URL ───────────────────────────────

    private fun loadAvailableUrl() {
        lifecycleScope.launch {
            val dir = getExternalFilesDir(null) ?: filesDir
            val settingsFile = File(dir, "settings.json")
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
                    // Исправление: если URL указывает на корень ассетов, добавляем index.html
                    val finalUrl = if (availableUrl == "https://appassets.androidplatform.net/assets/" || 
                        availableUrl == "https://appassets.androidplatform.net/assets") {
                        "https://appassets.androidplatform.net/assets/index.html"
                    } else {
                        availableUrl
                    }
                    webView.loadUrl(finalUrl)
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