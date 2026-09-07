package com.eenot.core

import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.core.content.FileProvider
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
    private val fileLock = Any() // Объект блокировки для исключения гонки потоков

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

    @JavascriptInterface
    fun updateSetting(key: String, valueJsonOrString: String, fileName: String, callbackName: String) {
        scope.launch(Dispatchers.IO) {
            val base = appFilesDir ?: run {
                safeEvaluateJs(callbackName, "false", isJson = true)
                return@launch
            }

            val targetFileName = if (fileName.isNotBlank()) fileName else "settings.json"
            val file = File(base, targetFileName)
            var success = false

            synchronized(fileLock) {
                try {
                    val currentSettings = if (file.exists() && file.length() > 0) {
                        try { JSONObject(file.readText()) } catch (e: Exception) { JSONObject() }
                    } else {
                        JSONObject()
                    }

                    val trimmedVal = valueJsonOrString.trim()
                    val parsedValue = when {
                        trimmedVal == "true" || trimmedVal == "false" -> trimmedVal.toBoolean()
                        trimmedVal.toIntOrNull() != null -> trimmedVal.toInt()
                        trimmedVal.toDoubleOrNull() != null -> trimmedVal.toDouble()
                        trimmedVal.startsWith("{") -> try { JSONObject(trimmedVal) } catch (e: Exception) { valueJsonOrString }
                        trimmedVal.startsWith("[") -> try { JSONArray(trimmedVal) } catch (e: Exception) { valueJsonOrString }
                        else -> valueJsonOrString
                    }

                    if (currentSettings.has(key) && currentSettings.get(key) is JSONObject && parsedValue is JSONObject) {
                        val targetObj = currentSettings.getJSONObject(key)
                        val keys = parsedValue.keys()
                        while (keys.hasNext()) {
                            val subKey = keys.next()
                            targetObj.put(subKey, parsedValue.get(subKey))
                        }
                    } else {
                        currentSettings.put(key, parsedValue)
                    }

                    file.parentFile?.mkdirs()
                    file.writeText(currentSettings.toString(2))
                    success = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
            synchronized(fileLock) {
                try {
                    // Валидируем JSON перед сохранением, если сохраняется settings.json
                    if (fileName == "settings.json") {
                        JSONObject(content) // Выбросит исключение, если JSON невалиден
                    }
                    file.parentFile?.mkdirs()
                    file.writeText(content)
                    success = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            safeEvaluateJs(callbackName, success.toString(), isJson = true)
        }
    }

    @JavascriptInterface
    fun getSettings(callbackName: String) {
        scope.launch(Dispatchers.IO) {
            val base = appFilesDir ?: run {
                safeEvaluateJs(callbackName, "{}", isJson = true)
                return@launch
            }
            val file = File(base, "settings.json")

            val jsonStr = synchronized(fileLock) {
                if (file.exists() && file.length() > 0) {
                    try {
                        val text = file.readText()
                        JSONObject(text) // Проверяем на валидность
                        text
                    } catch (e: Exception) {
                        "{}"
                    }
                } else {
                    "{}"
                }
            }

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

            val content = synchronized(fileLock) {
                try {
                    file.readText()
                } catch (e: Exception) {
                    ""
                }
            }

            safeEvaluateJs(callbackName, content, isJson = false)
        }
    }

    @JavascriptInterface
    fun shareImage(base64Data: String) {
        Log.d("WebAppInterface", "shareImage called, data length: ${base64Data.length}")
        scope.launch(Dispatchers.IO) {
            try {
                val pureBase64 = if (base64Data.contains(",")) {
                    base64Data.substring(base64Data.indexOf(",") + 1)
                } else {
                    base64Data
                }

                val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                Log.d("WebAppInterface", "Decoded bytes: ${decodedBytes.size}")

                val cachePath = File(context.cacheDir, "shared_images")
                cachePath.mkdirs()
                val imageFile = File(cachePath, "shared_image_${System.currentTimeMillis()}.png")
                imageFile.writeBytes(decodedBytes)
                Log.d("WebAppInterface", "File saved to: ${imageFile.absolutePath}")

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                scope.launch(Dispatchers.Main) {
                    val chooser = Intent.createChooser(shareIntent, "Share Image")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                    Log.d("WebAppInterface", "Share intent started")
                }
            } catch (e: Exception) {
                Log.e("WebAppInterface", "Error in shareImage", e)
            }
        }
    }
}