package com.ipwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logView = findViewById<TextView>(R.id.logView)
        logView.text = AppLogger.getLog()

        findViewById<Button>(R.id.btnRefreshWidget).setOnClickListener {
            refreshAllWidgets(this)
            logView.text = AppLogger.getLog()
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.logView).text = AppLogger.getLog()
    }

    private fun refreshAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, NetworkWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        AppLogger.log("手动刷新，找到 ${appWidgetIds.size} 个小组件")
        val intent = Intent(context, NetworkWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(intent)
    }
}