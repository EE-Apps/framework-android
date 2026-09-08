package com.eenot.core

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class WebAppInterface(
    private val activity: Activity,
    private val webView: WebView,
    private val scope: CoroutineScope
) {
    private val fileLock = Any() // Объект блокировки для исключения гонки потоков

    private val appFilesDir: File?
        get() = activity.getExternalFilesDir(null)

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
    fun keepScreenOn(enabled: Boolean) {
        activity.runOnUiThread {
            if (enabled) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    @JavascriptInterface
    fun getAppInfo(callbackId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
                val versionName = packageInfo.versionName ?: "unknown"
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toString()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toString()
                }

                val info = JSONObject().apply {
                    put("Платформа", "Android")
                    put("Версия Android", Build.VERSION.RELEASE ?: Build.VERSION.SDK_INT.toString())
                    put("SDK", Build.VERSION.SDK_INT)
                    put("Устройство", "${Build.MANUFACTURER} ${Build.MODEL}")
                    put("Версия APK", versionName)
                    put("Код APK", versionCode)
                }

                safeEvaluateJs(callbackId, info.toString(), isJson = true)
            } catch (e: Exception) {
                Log.e("WebAppInterface", "Error in getAppInfo", e)
            }
        }
    }

    @JavascriptInterface
    fun updateSetting(key: String, valueJsonOrString: String, fileName: String, callbackName: String) {
        scope.launch(Dispatchers.IO) {
            val base = appFilesDir ?: run {
                safeEvaluateJs(callbackName, "false", isJson = true)
                return@launch
            }

            val targetFileName = fileName.ifBlank { "settings.json" }
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

                val cachePath = File(activity.cacheDir, "shared_images")
                cachePath.mkdirs()
                val imageFile = File(cachePath, "shared_image_${System.currentTimeMillis()}.png")
                imageFile.writeBytes(decodedBytes)
                Log.d("WebAppInterface", "File saved to: ${imageFile.absolutePath}")

                val contentUri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.fileprovider",
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
                    activity.startActivity(chooser)
                    Log.d("WebAppInterface", "Share intent started")
                }
            } catch (e: Exception) {
                Log.e("WebAppInterface", "Error in shareImage", e)
            }
        }
    }

    @JavascriptInterface
    fun downloadApk(url: String?) {
        Log.d("WebAppInterface", "downloadApk called with URL: $url")
        if (url.isNullOrBlank()) {
            Log.w("WebAppInterface", "URL is null or blank, ignoring")
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
            Log.d("WebAppInterface", "Intent started successfully")
        } catch (e: Exception) {
            Log.e("WebAppInterface", "Failed to start activity for URL: $url", e)
        }
    }

    @JavascriptInterface
    fun setFullscreen(enabled: Boolean) {
        scope.launch(Dispatchers.Main) {
            val window = activity.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)

            if (enabled) {
                // Прячем строку состояния и навигацию
                controller.hide(WindowInsetsCompat.Type.systemBars())
                // Позволяем вызывать их свайпом (они будут поверх контента и пропадут сами)
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                // Возвращаем всё назад
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}