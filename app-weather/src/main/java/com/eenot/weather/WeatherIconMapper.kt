package com.eenot.weather

object WeatherIconMapper {

    /**
     * Маппит WMO weather code на имя файла иконки из assets/ico/gg/
     */
    fun getIconFileName(weatherCode: Int, isDay: Boolean): String {
        return when (weatherCode) {
            // Clear sky
            0 -> if (isDay) "sunny.webp" else "clear_night.webp"

            // Mainly clear, partly cloudy
            1 -> if (isDay) "mostly_sunny.webp" else "mostly_clear_night.webp"
            2 -> if (isDay) "partly_cloudy.webp" else "partly_cloudy_night.webp"

            // Overcast
            3 -> if (isDay) "mostly_cloudy_day.webp" else "mostly_cloudy_night.webp"

            // Fog
            45, 48 -> "haze_fog_dust_smoke.webp"

            // Drizzle
            51, 53, 55 -> "drizzle.webp"
            56, 57 -> "sleet_hail.webp"

            // Rain
            61, 63 -> "showers_rain.webp"
            65 -> "heavy_rain.webp"
            66, 67 -> "wintry_mix_rain_snow.webp"

            // Snow
            71 -> "flurries.webp"
            73 -> "snow_showers_snow.webp"
            75, 77 -> "heavy_snow.webp"

            // Rain showers
            80 -> if (isDay) "scattered_showers_day.webp" else "scattered_showers_night.webp"
            81, 82 -> "showers_rain.webp"

            // Snow showers
            85 -> "snow_showers_snow.webp"
            86 -> "blowing_snow.webp"

            // Thunderstorm
            95 -> if (isDay) "isolated_scattered_tstorms_day.webp" else "isolated_scattered_tstorms_night.webp"
            96, 99 -> "strong_tstorms.webp"

            else -> if (isDay) "sunny.webp" else "clear_night.webp"
        }
    }

    /**
     * Текстовое описание погоды
     */
    fun getWeatherDescription(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "Ясно"
            1 -> "Преимущественно ясно"
            2 -> "Переменная облачность"
            3 -> "Пасмурно"
            45, 48 -> "Туман"
            51 -> "Лёгкая морось"
            53 -> "Умеренная морось"
            55 -> "Сильная морось"
            56, 57 -> "Ледяная морось"
            61 -> "Небольшой дождь"
            63 -> "Умеренный дождь"
            65 -> "Сильный дождь"
            66, 67 -> "Ледяной дождь"
            71 -> "Небольшой снег"
            73 -> "Умеренный снег"
            75 -> "Сильный снег"
            77 -> "Снежные зёрна"
            80 -> "Ливень"
            81, 82 -> "Сильный ливень"
            85 -> "Снегопад"
            86 -> "Сильный снегопад"
            95 -> "Гроза"
            96, 99 -> "Гроза с градом"
            else -> "Неизвестно"
        }
    }
}