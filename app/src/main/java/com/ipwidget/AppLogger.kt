package com.ipwidget

object AppLogger {
    private val logBuilder = StringBuilder()

    fun log(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        logBuilder.append("[$timestamp] $message\n")
    }

    fun getLog(): String {
        return logBuilder.toString().ifEmpty { "暂无日志记录" }
    }
}