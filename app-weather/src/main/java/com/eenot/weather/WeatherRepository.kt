package com.eenot.weather

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("weather_data", Context.MODE_PRIVATE)

    data class WeatherData(
        val city: String,
        val temperature: Int,
        val description: String,
        val iconCode: String,
        val lastUpdated: Long
    )

    suspend fun fetchAndSaveWeather(city: String = "Moscow") = withContext(Dispatchers.IO) {
        try {
            // Здесь должен быть реальный вызов API (Retrofit/OkHttp)
            // Для примера симулируем ответ:
            val jsonStr = simulateApiCall(city)
            val json = JSONObject(jsonStr)

            val data = WeatherData(
                city = json.getString("city"),
                temperature = json.getInt("temp"),
                description = json.getString("description"),
                iconCode = json.getString("icon"),
                lastUpdated = System.currentTimeMillis()
            )

            // Сохраняем в SharedPreferences
            prefs.edit()
                .putString("city", data.city)
                .putInt("temperature", data.temperature)
                .putString("description", data.description)
                .putString("icon_code", data.iconCode)
                .putLong("last_updated", data.lastUpdated)
                .apply()

            data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getCachedWeather(): WeatherData? {
        if (!prefs.contains("city")) return null
        return WeatherData(
            city = prefs.getString("city", "") ?: "",
            temperature = prefs.getInt("temperature", 0),
            description = prefs.getString("description", "") ?: "",
            iconCode = prefs.getString("icon_code", "clear") ?: "clear",
            lastUpdated = prefs.getLong("last_updated", 0)
        )
    }

    fun formatLastUpdated(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "Обновлено: ${sdf.format(Date(timestamp))}"
    }

    private fun simulateApiCall(city: String): String {
        // Заглушка. В реальности: Retrofit запрос к api.openweathermap.org
        return """
            {
                "city": "$city",
                "temp": ${(Math.random() * 30).toInt()},
                "description": "Переменная облачность",
                "icon": "02d"
            }
        """.trimIndent()
    }
}