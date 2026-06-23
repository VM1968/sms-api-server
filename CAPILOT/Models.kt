package com.example.smsserver.models

import kotlinx.serialization.Serializable

@Serializable
data class SmsRequest(
    val phoneNumber: String,
    val message: String
)

@Serializable
data class SmsResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class ServerStatus(
    val isRunning: Boolean,
    val timestamp: String,
    val ipAddress: String,
    val port: Int,
    val uptime: String
)

@Serializable
data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val sdkInt: Int,
    val ipAddress: String,
    val hostname: String
)