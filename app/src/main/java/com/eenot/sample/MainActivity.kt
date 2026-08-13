package com.eenot.sample

import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        webView = findViewById(R.id.webView)

        // 1. файл настроек (из assets, если его нет)
        initDefaultSettings()

        setupWebView()
        setupBackButton()

        if (savedInstanceState != null) {
            // Восстанавливаем состояние WebView (историю, прокрутку и т.д.)
            webView.restoreState(savedInstanceState)
        } else {
            // Первый запуск или состояние потеряно - грузим с нуля
            loadAvailableUrl()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем состояние WebView перед уничтожением Activity системой
        webView.saveState(outState)
    }

    private fun initDefaultSettings() {
        val targetDir = getExternalFilesDir(null)
        val settingsFile = File(targetDir, "settings.json")

        // для отладки: если нужно, чтобы изменения в assets/default_settings.json
        // подхватывались, можно добавить условие или просто копировать всегда, 
        // если файл еще не был изменен пользователем
        // Здесь мы копируем, если файла нет ИЛИ если он пустой
        if (!settingsFile.exists() || settingsFile.length() == 0L) {
            try {
                assets.open("default_settings.json").use { inputStream ->
                    settingsFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("Settings", "settings.json инициализирован из assets.")
            } catch (e: Exception) {
                Log.e("Settings", "Ошибка копирования default_settings.json", e)
            }
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true

            // Доступ к локальным файлам и обход CORS
            allowFileAccess = true
            allowContentAccess = true
            allowUniversalAccessFromFileURLs = true
            allowFileAccessFromFileURLs = true

            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        // JS-мост (window.AndroidBridge)
        webView.addJavascriptInterface(
            WebAppInterface(this, webView, lifecycleScope),
            "AndroidBridge"
        )

        // Перехват console.log
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d("WebViewJS", "${consoleMessage.message()} [Line: ${consoleMessage.lineNumber()}]")
                return true
            }
        }

        // обработка кликов по ссылкам и SSL ошибок
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                // внутри
                val trustedHosts = listOf("localhost", "10.0.2.2", "95.153.88.229", "ee-apps.github.io", "127.0.0.1", "192.168.100.18")

                if (trustedHosts.any { url.contains(it) } || url.startsWith("file://")) {
                    return false // Грузим внутри WebView
                }

                // перенаправление внешних ссылок
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                return true
            }

            // плевать на самописные сертификаты
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                handler.proceed()
            }
        }
    }

    private fun setupBackButton() {
        // переопределение кнопки назад - возврат на прошлую страницу, а не закрытие приложения
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

    private fun loadAvailableUrl() {
        // экран загрузки
        val loadingHtml = "<html><body style='display:flex;justify-content:center;align-items:center;height:100vh;margin:0;font-family:sans-serif;background-color:#0e0f0e;color:white'><h2>Loading...</h2></body></html>"
        webView.loadData(loadingHtml, "text/html", "UTF-8")

        lifecycleScope.launch {
            val settingsFile = File(getExternalFilesDir(null), "settings.json")
            Log.d("LoadUrl", "Чтение настроек из: ${settingsFile.absolutePath}")

            val settingsJson = if (settingsFile.exists()) {
                try {
                    val content = settingsFile.readText()
                    Log.d("LoadUrl", "Содержимое settings.json: $content")
                    JSONObject(content)
                } catch (e: Exception) {
                    Log.e("LoadUrl", "Ошибка парсинга JSON", e)
                    JSONObject()
                }
            } else {
                Log.w("LoadUrl", "Файл settings.json не найден, используем defaults")
                JSONObject()
            }

            val urls = mutableListOf<String>()
            if (settingsJson.has("urls")) {
                val arr = settingsJson.getJSONArray("urls")
                for (i in 0 until arr.length()) {
                    urls.add(arr.getString(i))
                }
            } else {
                val defaultUrls = resources.getStringArray(R.array.default_urls)
                urls.addAll(defaultUrls.toList())
            }
            
            Log.d("LoadUrl", "Список URL для проверки: $urls")

            // первыйт доступный источник
            val availableUrl = NetworkUtils.getFirstAvailableUrl(urls)
            Log.d("LoadUrl", "Выбранный URL: $availableUrl")

            withContext(Dispatchers.Main) {
                if (availableUrl != null) {
                    webView.loadUrl(availableUrl)
                } else {
                    val errorHtml = """
                    <html><body style="display:flex;justify-content:center;align-items:center;height:100vh;font-family:sans-serif;text-align:center;background:#0e0f0e;color:white;margin:0;padding:20px;">
                        <div>
                            <h1 style="color:#d32f2f;">Нет соединения</h1>
                            <p>Не удалось подключиться к серверам:</p>
                            <ul style="text-align:left;display:inline-block;">
                                ${urls.joinToString("") { "<li>$it</li>" }}
                            </ul>
                            <p>Проверьте настройки сети или settings.json</p>
                            <button onclick="location.reload()" style="padding:10px 20px;font-size:16px;">Повторить</button>
                        </div>
                    </body></html>
                """.trimIndent()
                    webView.loadData(errorHtml, "text/html", "UTF-8")
                }
            }
        }
    }
}