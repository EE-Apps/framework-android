package com.eenot.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {

    suspend fun getFirstAvailableUrl(urls: List<String>): String? = withContext(Dispatchers.IO) {
        // паралельная проверка
        val tasks = urls.map { url ->
            async {
                if (isUrlReachable(url)) url else null
            }
        }
        
        // ожидание всех результатов -> возвращение первого успешного
        tasks.awaitAll().firstOrNull { it != null }
    }

    private fun isUrlReachable(urlStr: String): Boolean {
        if (urlStr.startsWith("file://")) return true
        
        return try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 2000 
            connection.readTimeout = 2000
            val code = connection.responseCode
            code in 200..399
        } catch (e: Exception) {
            false
        }
    }
}