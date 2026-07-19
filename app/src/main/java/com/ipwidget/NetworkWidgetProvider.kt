package com.ipwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class NetworkWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        AppLogger.log("onUpdate 被调用，组件数量：${appWidgetIds.size}")
        for (appWidgetId in appWidgetIds) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_network)
                views.setTextViewText(R.id.title, "IP 信息")
                views.setTextViewText(R.id.domesticLabel, "国内 IP")
                views.setTextViewText(R.id.domesticInfo, "正在获取...")
                views.setTextViewText(R.id.foreignLabel, "国外 IP")
                views.setTextViewText(R.id.foreignInfo, "正在获取...")
                views.setTextViewText(R.id.updateTime, "更新时间：--")
                views.setOnClickPendingIntent(R.id.refresh, buildRefreshPendingIntent(context, appWidgetId))
                appWidgetManager.updateAppWidget(appWidgetId, views)
                AppLogger.log("小组件 $appWidgetId 初始视图已更新，开始获取 IP")
                refreshWidget(context, appWidgetManager, appWidgetId)
            } catch (e: Exception) {
                AppLogger.log("小组件初始化错误：${e.message}")
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
                // 国内：ipip.net
                val ipipText = IpLookup.fetchIpAddress("https://myip.ipip.net")
                // 解析 ipip 返回的文本，例如：当前 IP：203.198.103.77  来自于：中国 北京 北京  联通
                val ipipParts = ipipText.split("：", " ").filter { it.isNotEmpty() }
                val domesticIp = if (ipipParts.size > 1) ipipParts[1] else "解析失败"
                // 提取地点信息（简单去除“来自于：”字样）
                val domesticLocation = if (ipipParts.size > 2) ipipText.substringAfter("来自于：").trim() else ""

                // 国外：ip.sb
                val geoJson = IpLookup.fetchIpAddress("https://api.ip.sb/geoip")
                val json = JSONObject(geoJson)
                val foreignIp = json.optString("ip", "未知")
                val foreignCountry = json.optString("country", "未知")

                AppLogger.log("国内 IP：$domesticIp ($domesticLocation)，国外 IP：$foreignIp ($foreignCountry)")

                val timeText = "更新时间：${SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(Date())}"

                val updatedViews = RemoteViews(context.packageName, R.layout.widget_network).apply {
                    setTextViewText(R.id.title, "IP 信息")
                    setTextViewText(R.id.domesticLabel, "国内 IP")
                    setTextViewText(R.id.domesticInfo, "$domesticIp\n$domesticLocation".trim())
                    setTextViewText(R.id.foreignLabel, "国外 IP")
                    setTextViewText(R.id.foreignInfo, "$foreignIp ($foreignCountry)")
                    setTextViewText(R.id.updateTime, timeText)
                    setOnClickPendingIntent(R.id.refresh, buildRefreshPendingIntent(context, appWidgetId))
                }
                appWidgetManager.updateAppWidget(appWidgetId, updatedViews)
                AppLogger.log("小组件更新成功")
            } catch (e: Exception) {
                AppLogger.log("网络请求/更新失败：${e.message}")
                try {
                    val errorViews = RemoteViews(context.packageName, R.layout.widget_network).apply {
                        setTextViewText(R.id.domesticInfo, "获取失败")
                        setTextViewText(R.id.foreignInfo, "获取失败")
                        setTextViewText(R.id.updateTime, "请检查网络")
                        setOnClickPendingIntent(R.id.refresh, buildRefreshPendingIntent(context, appWidgetId))
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