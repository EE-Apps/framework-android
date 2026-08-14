package com.eenot.weather.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.eenot.weather.R
import com.eenot.weather.WeatherDataParser
import com.eenot.weather.WeatherIconMapper
import java.io.InputStream

class WeatherForecastService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WeatherForecastFactory(applicationContext)
    }
}

class WeatherForecastFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var forecast: List<WeatherDataParser.DailyForecast> = emptyList()

    override fun onCreate() {
        // Загружаем данные
        val data = WeatherDataParser.readFullWeatherFromJson(context)
        forecast = data?.dailyForecast?.drop(1)?.take(5) ?: emptyList() // Пропускаем сегодня, берём 5 следующих дней
    }

    override fun onDataSetChanged() {
        val data = WeatherDataParser.readFullWeatherFromJson(context)
        forecast = data?.dailyForecast?.drop(1)?.take(5) ?: emptyList()
    }

    override fun onDestroy() {}

    override fun getCount(): Int = forecast.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.weather_widget_forecast_item)

        if (position < forecast.size) {
            val item = forecast[position]

            views.setTextViewText(R.id.forecastDay, WeatherDataParser.getDayOfWeek(item.timestamp))
            views.setTextViewText(R.id.forecastTempDay, WeatherDataParser.formatTemperature(item.tempMax))
            views.setTextViewText(R.id.forecastTempNight, WeatherDataParser.formatTemperature(item.tempMin))

            // Загружаем иконки из assets
            val iconDayName = WeatherIconMapper.getIconFileName(item.weatherCodeDay, true)
            val iconNightName = WeatherIconMapper.getIconFileName(item.weatherCodeNight, false)

            loadIconFromAssets(iconDayName)?.let {
                views.setImageViewBitmap(R.id.forecastIconDay, it)
            }
            loadIconFromAssets(iconNightName)?.let {
                views.setImageViewBitmap(R.id.forecastIconNight, it)
            }
        }

        return views
    }

    private fun loadIconFromAssets(fileName: String): android.graphics.Bitmap? {
        return try {
            context.assets.open("ico/gg/$fileName").use { inputStream ->
                android.graphics.BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}