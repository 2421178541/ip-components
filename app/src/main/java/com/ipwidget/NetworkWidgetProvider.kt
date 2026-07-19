package com.ipwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NetworkWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_network)
            views.setTextViewText(R.id.ipip, "国内 IP")
            views.setTextViewText(R.id.ipsb, "国外 IP")
            views.setTextViewText(R.id.ipipInfo, "正在获取...")
            views.setTextViewText(R.id.ipsbInfo, "正在获取...")
            views.setTextViewText(R.id.updateTime, "更新时间：--")
            views.setOnClickPendingIntent(
                R.id.refresh,
                buildRefreshPendingIntent(context, appWidgetId)
            )
            appWidgetManager.updateAppWidget(appWidgetId, views)
            refreshWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
            if (appWidgetId != 0) {
                onUpdate(context, AppWidgetManager.getInstance(context), intArrayOf(appWidgetId))
            }
        }
    }

    private fun refreshWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Thread {
            val domesticIp = IpLookup.fetchIpAddress("https://api.ipify.org")
            val foreignIp = IpLookup.fetchIpAddress("https://checkip.amazonaws.com")
            val timeText = "更新时间：${SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(Date())}"

            val updatedViews = RemoteViews(context.packageName, R.layout.widget_network).apply {
                setTextViewText(R.id.ipip, "国内 IP")
                setTextViewText(R.id.ipsb, "国外 IP")
                setTextViewText(R.id.ipipInfo, domesticIp)
                setTextViewText(R.id.ipsbInfo, foreignIp)
                setTextViewText(R.id.updateTime, timeText)
                setOnClickPendingIntent(
                    R.id.refresh,
                    buildRefreshPendingIntent(context, appWidgetId)
                )
            }

            appWidgetManager.updateAppWidget(appWidgetId, updatedViews)
        }.start()
    }

    private fun buildRefreshPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, NetworkWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val ACTION_REFRESH = "com.ipwidget.ACTION_REFRESH"
    }
}