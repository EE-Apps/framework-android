package com.eenot.core

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class WebAppInterface(
    private val context: Context,
    private val webView: WebView,
    private val scope: CoroutineScope
) {
    private val appFilesDir: File?
        get() = context.getExternalFilesDir(null)

    private fun isPathSafe(file: File): Boolean {
        val baseDir = appFilesDir?.canonicalFile ?: return false
        val targetFile = file.canonicalFile
        return targetFile.path.startsWith(baseDir.path)
    }

    private fun safeEvaluateJs(callbackName: String, rawJsonOrStringData: String, isJson: Boolean = false) {
        scope.launch(Dispatchers.Main) {
            val jsArgument = if (isJson) rawJsonOrStringData else JSONObject.quote(rawJsonOrStringData)
            webView.evaluateJavascript("if (window.$callbackName) { window.$callbackName($jsArgument); }", null)
        }
    }

    // ==========================================
    // 🔧 НОВАЯ ФУНКЦИЯ: Обновление конкретного параметра
    // ==========================================
    @JavascriptInterface
    fun updateSetting(key: String, valueJsonOrString: String, callbackName: String) {
        scope.launch(Dispatchers.IO) {
            val base = appFilesDir ?: run {
                safeEvaluateJs(callbackName, "false", isJson = true)
                return@launch
            }

            val file = File(base, "settings.json")
            var success = false

            try {
                // 1. Читаем существующие настройки или создаём пустой объект
                val currentSettings = if (file.exists()) {
                    val content = file.readText()
                    if (content.isNotBlank()) JSONObject(content) else JSONObject()
                } else {
                    JSONObject()
                }

                // 2. Парсим переданное значение (строка, число, boolean или JSON-объект/массив)
                val parsedValue = try {
                    when {
                        valueJsonOrString == "true" || valueJsonOrString == "false" -> valueJsonOrString.toBoolean()
                        valueJsonOrString.toIntOrNull() != null -> valueJsonOrString.toInt()
                        valueJsonOrString.toDoubleOrNull() != null -> valueJsonOrString.toDouble()
                        valueJsonOrString.startsWith("{") -> JSONObject(valueJsonOrString)
                        valueJsonOrString.startsWith("[") -> JSONArray(valueJsonOrString)
                        else -> valueJsonOrString
                    }
                } catch (e: Exception) {
                    valueJsonOrString // Если сбой парсинга, сохраняем как обычную строку
                }

                // 3. Обновляем или добавляем поле
                currentSettings.put(key, parsedValue)

                // 4. Перезаписываем файл
                file.parentFile?.mkdirs()
                file.writeText(currentSettings.toString(2)) // с отступами для читаемости
                success = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

            safeEvaluateJs(callbackName, success.toString(), isJson = true)
        }
    }

    @JavascriptInterface
    fun writeFile(fileName: String, content: String, callbackName: String) {
        scope.launch(Dispatchers.IO) {
            val base = appFilesDir ?: run {
                safeEvaluateJs(callbackName, "false", isJson = true)
                return@launch
            }

            val file = File(base, fileName)
            if (!isPathSafe(file)) {
                safeEvaluateJs(callbackName, "false", isJson = true)
                return@launch
            }

            var success = false
            try {
                file.parentFile?.mkdirs()
                file.writeText(content)
                success = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

            safeEvaluateJs(callbackName, success.toString(), isJson = true)
        }
    }

    @JavascriptInterface
    fun getSettings(callbackName: String) {
        scope.launch(Dispatchers.IO) {
            val base = appFilesDir ?: return@launch
            val file = File(base, "settings.json")
            val jsonStr = if (file.exists()) file.readText() else "{}"
            safeEvaluateJs(callbackName, jsonStr, isJson = true)
        }
    }

    @JavascriptInterface
    fun saveSettings(jsonString: String, callbackName: String) {
        writeFile("settings.json", jsonString, callbackName)
    }

    @JavascriptInterface
    fun listFiles(callbackName: String) {
        scope.launch(Dispatchers.IO) {
            val files = appFilesDir?.listFiles()?.map { it.name } ?: emptyList()
            val jsonArrayStr = JSONArray(files).toString()
            safeEvaluateJs(callbackName, jsonArrayStr, isJson = true)
        }
    }

    @JavascriptInterface
    fun readFile(fileName: String, callbackName: String) {
        scope.launch(Dispatchers.IO) {
            val base = appFilesDir ?: run {
                safeEvaluateJs(callbackName, "")
                return@launch
            }

            val file = File(base, fileName)
            if (!isPathSafe(file) || !file.exists()) {
                safeEvaluateJs(callbackName, "")
                return@launch
            }

            val content = try {
                file.readText()
            } catch (e: Exception) {
                ""
            }

            safeEvaluateJs(callbackName, content, isJson = false)
        }
    }
}