package com.eenot.sample

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

class WebAppInterface(
    private val context: Context,
    private val webView: WebView,
    private val scope: CoroutineScope // lifecycleScope из Activity
) {
    // Папка Android/data/приложение/files
    private val appFilesDir: File? = context.getExternalFilesDir(null)

    @JavascriptInterface
    fun getSettings(callbackName: String) {
        scope.launch {
            val file = File(appFilesDir, "settings.json")
            val jsonStr = if (file.exists()) file.readText() else "{}"
            withContext(Dispatchers.Main) {
                webView.evaluateJavascript("window.$callbackName($jsonStr)", null)
            }
        }
    }

    @JavascriptInterface
    fun saveSettings(jsonString: String, callbackName: String) {
        scope.launch {
            val file = File(appFilesDir, "settings.json")
            file.writeText(jsonString)
            withContext(Dispatchers.Main) {
                webView.evaluateJavascript("window.$callbackName(true)", null)
            }
        }
    }

    @JavascriptInterface
    fun listFiles(callbackName: String) {
        scope.launch {
            val files = appFilesDir?.listFiles()?.map { it.name } ?: emptyList()
            val json = JSONArray(files).toString()
            withContext(Dispatchers.Main) {
                webView.evaluateJavascript("window.$callbackName($json)", null)
            }
        }
    }

    @JavascriptInterface
    fun readFile(fileName: String, callbackName: String) {
        scope.launch {
            val file = File(appFilesDir, fileName)
            val content = if (file.exists()) file.readText() else ""
            withContext(Dispatchers.Main) {
                // Экранируем спецсимволы, чтобы не сломать JS-строку
                val escaped = content.replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                webView.evaluateJavascript("window.$callbackName('$escaped')", null)
            }
        }
    }
}