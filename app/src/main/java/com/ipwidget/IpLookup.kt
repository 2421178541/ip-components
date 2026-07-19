package com.ipwidget

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object IpLookup {
    fun fetchIpAddress(url: String): String {
        return try {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
            }
            connection.connect()
            val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            response.trim().ifEmpty { "获取失败" }
        } catch (_: Exception) {
            "获取失败"
        }
    }
}
