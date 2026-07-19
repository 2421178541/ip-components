package com.ipwidget

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logView = findViewById<TextView>(R.id.logView)
        logView.text = AppLogger.getLog()
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.logView).text = AppLogger.getLog()
    }
}