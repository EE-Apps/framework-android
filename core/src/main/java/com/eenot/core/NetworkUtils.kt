package com.eenot.core

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {

    suspend fun getFirstAvailableUrl(urls: List<String>): String? = withContext(Dispatchers.IO) {
        // Проверяем URL строго ПООЧЕРЕДНО в порядке приоритета
        for (url in urls) {
            Log.d("NetworkUtils", "Checking: $url")
            if (isUrlReachable(url)) {
                Log.d("NetworkUtils", "Successfully connected to: $url")
                return@withContext url
            }
        }
        null
    }

    private fun isUrlReachable(urlStr: String): Boolean {
        if (urlStr.startsWith("file://")) return true
        if (urlStr.contains("appassets.androidplatform.net")) return true

        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD" // Используем стандартный HEAD без кастомных заголовков
            connection.connectTimeout = 1500  // 1.5 сек вполне достаточно для локальной сети
            connection.readTimeout = 1500
            connection.instanceFollowRedirects = true

            val code = connection.responseCode
            Log.d("NetworkUtils", "URL: $urlStr -> Response Code: $code")

            // Если сервер возвращает 405 (Method Not Allowed) на HEAD, пробуем GET
            if (code == 405) {
                return checkWithGet(urlStr)
            }

            code in 200..399
        } catch (e: Exception) {
            Log.e("NetworkUtils", "Failed to connect to $urlStr: ${e.localizedMessage}")
            false
        } finally {
            connection?.disconnect()
        }
    }

    private fun checkWithGet(urlStr: String): Boolean {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 1500
            connection.readTimeout = 1500
            val code = connection.responseCode
            code in 200..399
        } catch (e: Exception) {
            false
        } finally {
            connection?.disconnect()
        }
    }
}