package com.example.smsserver.utils

import java.text.SimpleDateFormat
import java.util.*

object Logger {
    private var logCallback: ((String) -> Unit)? = null

    fun setLogCallback(callback: (String) -> Unit) {
        logCallback = callback
    }

    fun log(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] $message"
        logCallback?.invoke(logMessage)
        println(logMessage)
    }
}