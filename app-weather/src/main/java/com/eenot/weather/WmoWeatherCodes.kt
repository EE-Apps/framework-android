package com.eenot.weather

object WmoWeatherCodes {

    data class WeatherInfo(
        val description: String,
        val icon: String // Emoji или имя drawable
    )

    fun getWeatherInfo(code: Int): WeatherInfo {
        return when (code) {
            0 -> WeatherInfo("Ясно", "☀️")
            1 -> WeatherInfo("Преимущественно ясно", "🌤️")
            2 -> WeatherInfo("Переменная облачность", "⛅")
            3 -> WeatherInfo("Пасмурно", "☁️")
            45, 48 -> WeatherInfo("Туман", "🌫️")
            51, 53, 55 -> WeatherInfo("Морось", "🌦️")
            56, 57 -> WeatherInfo("Ледяная морось", "🌧️")
            61 -> WeatherInfo("Небольшой дождь", "🌦️")
            63 -> WeatherInfo("Умеренный дождь", "🌧️")
            65 -> WeatherInfo("Сильный дождь", "🌧️")
            66, 67 -> WeatherInfo("Ледяной дождь", "🌧️")
            71 -> WeatherInfo("Небольшой снег", "🌨️")
            73 -> WeatherInfo("Умеренный снег", "🌨️")
            75 -> WeatherInfo("Сильный снег", "❄️")
            77 -> WeatherInfo("Снежные зёрна", "❄️")
            80 -> WeatherInfo("Ливень", "🌧️")
            81, 82 -> WeatherInfo("Сильный ливень", "⛈️")
            85 -> WeatherInfo("Снегопад", "🌨️")
            86 -> WeatherInfo("Сильный снегопад", "❄️")
            95 -> WeatherInfo("Гроза", "⛈️")
            96, 99 -> WeatherInfo("Гроза с градом", "⛈️")
            else -> WeatherInfo("Неизвестно", "❓")
        }
    }

    // Для ночной версии (is_day = 0)
    fun getNightIcon(code: Int): String {
        return when (code) {
            0 -> "🌙"
            1 -> "🌙"
            else -> getWeatherInfo(code).icon
        }
    }
}