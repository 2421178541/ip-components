package com.ipwidget

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logView = findViewById<TextView>(R.id.logView)
        logView.text = AppLogger.getLog()
    }

    override fun onResume() {
        super.onResume()
        // 每次从后台回来刷新日志（如果小组件后台写入了新内容）
        findViewById<TextView>(R.id.logView).text = AppLogger.getLog()
    }
}