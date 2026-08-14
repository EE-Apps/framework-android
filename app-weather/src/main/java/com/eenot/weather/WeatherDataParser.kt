package com.eenot.weather

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WeatherDataParser {

    data class CurrentWeather(
        val temperature: Double,
        val apparentTemperature: Double,
        val humidity: Int,
        val weatherCode: Int,
        val isDay: Boolean,
        val windSpeed: Double,
        val timestamp: Long
    )

    data class DailyForecast(
        val timestamp: Long,
        val weatherCodeDay: Int,
        val weatherCodeNight: Int,
        val tempMax: Double,
        val tempMin: Double
    )

    data class FullWeatherData(
        val current: CurrentWeather,
        val city: String,
        val dailyForecast: List<DailyForecast>
    )

    /**
     * Читает и парсит savedWeather.json с прогнозом
     */
    fun readFullWeatherFromJson(context: Context): FullWeatherData? {
        return try {
            val file = File(context.getExternalFilesDir(null), "savedWeather.json")
            if (!file.exists()) return null

            val jsonString = file.readText()
            val json = JSONObject(jsonString)

            val current = json.getJSONObject("current")
            val currentTimeStr = current.optString("time")
            val currentTimestamp = if (currentTimeStr.contains("-")) {
                parseDateToTimestamp(currentTimeStr)
            } else {
                current.optLong("time")
            }

            val currentWeather = CurrentWeather(
                temperature = current.getDouble("temperature_2m"),
                apparentTemperature = current.getDouble("apparent_temperature"),
                humidity = current.getInt("relative_humidity_2m"),
                weatherCode = current.getInt("weather_code"),
                isDay = current.getInt("is_day") == 1,
                windSpeed = current.getDouble("wind_speed_10m"),
                timestamp = currentTimestamp
            )

            // Парсим прогноз на 7 дней
            val dailyForecast = mutableListOf<DailyForecast>()
            val daily = json.optJSONObject("daily")

            if (daily != null) {
                val times = daily.getJSONArray("time")
                val weatherCodes = daily.getJSONArray("weather_code")
                val tempMax = daily.getJSONArray("temperature_2m_max")
                val tempMin = daily.getJSONArray("temperature_2m_min")

                for (i in 0 until minOf(times.length(), 7)) {
                    val timeStr = times.optString(i)
                    val timestamp = if (timeStr.contains("-")) {
                        parseDateToTimestamp(timeStr)
                    } else {
                        times.optLong(i)
                    }

                    dailyForecast.add(
                        DailyForecast(
                            timestamp = timestamp,
                            weatherCodeDay = weatherCodes.getInt(i),
                            weatherCodeNight = weatherCodes.getInt(i), // Упрощение: один код на день
                            tempMax = tempMax.getDouble(i),
                            tempMin = tempMin.getDouble(i)
                        )
                    )
                }
            }

            // Читаем город из настроек или используем координаты
            val city = getCityFromCoordinates(
                json.getDouble("latitude"),
                json.getDouble("longitude")
            )

            FullWeatherData(
                current = currentWeather,
                city = city,
                dailyForecast = dailyForecast
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCityFromCoordinates(lat: Double, lon: Double): String {
        // Здесь можно добавить обратное геокодирование
        // Пока просто возвращаем "Неизвестно"
        return "Неизвестно"
    }

    private fun parseDateToTimestamp(dateString: String): Long {
        return try {
            val format = if (dateString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            }
            (format.parse(dateString)?.time ?: 0L) / 1000
        } catch (e: Exception) {
            0L
        }
    }

    fun formatTemperature(temp: Double): String {
        return "${temp.toInt()}°"
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "Обновлено: ${sdf.format(Date(timestamp * 1000))}"
    }

    fun getDayOfWeek(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}