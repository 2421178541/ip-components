package com.ipwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
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
        AppLogger.log("onUpdate 被调用，组件数量：${appWidgetIds.size}")
        for (appWidgetId in appWidgetIds) {
            try {
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
                AppLogger.log("小组件 $appWidgetId 初始视图已更新，开始获取 IP")
                refreshWidget(context, appWidgetManager, appWidgetId)
            } catch (e: Exception) {
                AppLogger.log("小组件初始化错误：${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
            AppLogger.log("收到刷新广播，组件ID：$appWidgetId")
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
            try {
                AppLogger.log("开始网络请求...")
                val domesticIp = IpLookup.fetchIpAddress("https://api.ipify.org")
                val foreignIp = IpLookup.fetchIpAddress("https://checkip.amazonaws.com")
                AppLogger.log("获取到国内IP：$domesticIp，国外IP：$foreignIp")
                val timeText = "更新时间：${
                    SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(Date())
                }"

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
                AppLogger.log("小组件更新成功")
            } catch (e: Exception) {
                AppLogger.log("网络请求/更新失败：${e.message}")
                try {
                    val errorViews = RemoteViews(context.packageName, R.layout.widget_network).apply {
                        setTextViewText(R.id.ipip, "国内 IP")
                        setTextViewText(R.id.ipsb, "国外 IP")
                        setTextViewText(R.id.ipipInfo, "获取失败")
                        setTextViewText(R.id.ipsbInfo, "获取失败")
                        setTextViewText(R.id.updateTime, "请检查网络")
                        setOnClickPendingIntent(
                            R.id.refresh,
                            buildRefreshPendingIntent(context, appWidgetId)
                        )
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, errorViews)
                } catch (updateException: Exception) {
                    AppLogger.log("错误视图更新也失败：${updateException.message}")
                }
            }
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