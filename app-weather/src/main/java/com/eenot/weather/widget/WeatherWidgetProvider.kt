package com.eenot.weather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.*
import com.eenot.weather.R
import com.eenot.weather.WeatherMainActivity
import com.eenot.weather.WeatherDataParser
import com.eenot.weather.WeatherIconMapper
import java.io.InputStream
import java.util.concurrent.TimeUnit

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            updateAppWidget(context, appWidgetManager, appWidgetId, options)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        schedulePeriodicUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun schedulePeriodicUpdate(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<WeatherWidgetUpdateWorker>(
            30, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        options: android.os.Bundle
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget_layout)
        val weather = WeatherDataParser.readFullWeatherFromJson(context)

        if (weather != null) {
            fillAllLayouts(context, views, weather)
            showCorrectLayout(views, options)
        } else {
            // Нет данных — показываем компактный режим с заглушкой
            views.setViewVisibility(R.id.layoutCompact, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.layoutSquare, android.view.View.GONE)
            views.setViewVisibility(R.id.layoutTall, android.view.View.GONE)
            views.setViewVisibility(R.id.layoutWide, android.view.View.GONE)
        }

        // Клик открывает приложение
        val intent = Intent(context, WeatherMainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.layoutCompact, pendingIntent)
        views.setOnClickPendingIntent(R.id.layoutSquare, pendingIntent)
        views.setOnClickPendingIntent(R.id.layoutTall, pendingIntent)
        views.setOnClickPendingIntent(R.id.layoutWide, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun fillAllLayouts(
        context: Context,
        views: RemoteViews,
        weather: WeatherDataParser.FullWeatherData
    ) {
        val current = weather.current
        val iconFileName = WeatherIconMapper.getIconFileName(current.weatherCode, current.isDay)
        val iconBitmap = loadIconFromAssets(context, iconFileName)

        // Компактный режим (2x1)
        if (iconBitmap != null) {
            views.setImageViewBitmap(R.id.iconCompact, iconBitmap)
        }
        views.setTextViewText(R.id.tempCompact, WeatherDataParser.formatTemperature(current.temperature))
        views.setTextViewText(R.id.cityCompact, weather.city)

        // Квадратный режим (2x2, 3x3)
        if (iconBitmap != null) {
            views.setImageViewBitmap(R.id.iconSquare, iconBitmap)
        }
        views.setTextViewText(R.id.tempSquare, WeatherDataParser.formatTemperature(current.temperature))
        views.setTextViewText(R.id.citySquare, weather.city)
        views.setTextViewText(R.id.descSquare, WeatherIconMapper.getWeatherDescription(current.weatherCode))
        views.setTextViewText(
            R.id.detailsSquare,
            "💧 ${current.humidity}%  💨 ${current.windSpeed.toInt()} км/ч"
        )

        // Узкий высокий режим (2x3, 2x4)
        if (iconBitmap != null) {
            views.setImageViewBitmap(R.id.iconTall, iconBitmap)
        }
        views.setTextViewText(R.id.tempTall, WeatherDataParser.formatTemperature(current.temperature))
        views.setTextViewText(R.id.cityTall, weather.city)
        views.setTextViewText(R.id.descTall, WeatherIconMapper.getWeatherDescription(current.weatherCode))
        views.setTextViewText(
            R.id.detailsTall,
            "💧 ${current.humidity}%  💨 ${current.windSpeed.toInt()} км/ч"
        )

        // Широкий режим (4x2, 5x2, 4x3, 5x3)
        if (iconBitmap != null) {
            views.setImageViewBitmap(R.id.iconWide, iconBitmap)
        }
        views.setTextViewText(R.id.tempWide, WeatherDataParser.formatTemperature(current.temperature))
        views.setTextViewText(R.id.cityWide, weather.city)
        views.setTextViewText(R.id.descWide, WeatherIconMapper.getWeatherDescription(current.weatherCode))
        views.setTextViewText(
            R.id.detailsWide,
            "💧 ${current.humidity}%  💨 ${current.windSpeed.toInt()} км/ч"
        )

        views.setTextViewText(
            R.id.cityWide,
            weather.city
        )

        // Подключаем список прогноза для широкого режима
        // val forecastIntent = Intent(context, WeatherForecastService::class.java)
        // views.setRemoteAdapter(R.id.forecastList, forecastIntent)
    }

    private fun showCorrectLayout(views: RemoteViews, options: android.os.Bundle) {
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)

        // Определяем соотношение сторон и размер
        val isWide = maxWidth >= 250 && minHeight <= 150
        val isTall = minWidth <= 150 && maxHeight >= 200
        val isSquare = !isWide && !isTall

        // Скрываем все, показываем нужный
        views.setViewVisibility(R.id.layoutCompact, android.view.View.GONE)
        views.setViewVisibility(R.id.layoutSquare, android.view.View.GONE)
        views.setViewVisibility(R.id.layoutTall, android.view.View.GONE)
        views.setViewVisibility(R.id.layoutWide, android.view.View.GONE)

        when {
            isWide && minHeight >= 100 -> views.setViewVisibility(R.id.layoutWide, android.view.View.VISIBLE)
            isTall -> views.setViewVisibility(R.id.layoutTall, android.view.View.VISIBLE)
            isSquare -> views.setViewVisibility(R.id.layoutSquare, android.view.View.VISIBLE)
            else -> views.setViewVisibility(R.id.layoutCompact, android.view.View.VISIBLE)
        }
    }

    private fun loadIconFromAssets(context: Context, fileName: String): android.graphics.Bitmap? {
        return try {
            context.assets.open("ico/gg/$fileName").use { inputStream ->
                android.graphics.BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val WORK_NAME = "weather_widget_update"
    }
}