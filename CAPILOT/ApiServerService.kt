package com.example.smsserver.services

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.smsserver.R
import com.example.smsserver.models.SmsRequest
import com.example.smsserver.models.SmsResponse
import com.example.smsserver.models.ServerStatus
import com.example.smsserver.models.DeviceInfo
import com.example.smsserver.utils.NetworkUtils
import com.example.smsserver.utils.Logger
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ApiServerService : Service() {

    private var server: ApplicationEngine? = null
    private val smsService = SmsService()
    private val smsHistory = mutableListOf<SmsRequest>()

    override fun onCreate() {
        super.onCreate()
        Logger.log("ApiServerService created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.log("Starting API Server...")
        startServer()
        return START_STICKY
    }

    private fun startServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                server = embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
                    install(ContentNegotiation) {
                        json(Json {
                            prettyPrint = true
                            ignoreUnknownKeys = true
                        })
                    }

                    routing {
                        // Send SMS
                        post("/api/sms/send") {
                            try {
                                val request = call.receive<SmsRequest>()
                                Logger.log("SMS Request: ${request.phoneNumber}")

                                val response = smsService.sendSms(
                                    phoneNumber = request.phoneNumber,
                                    message = request.message
                                )

                                if (response.success) {
                                    smsHistory.add(request)
                                    Logger.log("SMS sent successfully to ${request.phoneNumber}")
                                }

                                call.respond(response)
                            } catch (e: Exception) {
                                Logger.log("Error sending SMS: ${e.message}")
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    SmsResponse(false, "Error: ${e.message}")
                                )
                            }
                        }

                        // Server Status
                        get("/api/server/status") {
                            val status = ServerStatus(
                                isRunning = true,
                                timestamp = LocalDateTime.now().format(
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                ),
                                ipAddress = NetworkUtils.getLocalIpAddress(),
                                port = 8080,
                                uptime = "Active"
                            )
                            call.respond(status)
                        }

                        // Device Info
                        get("/api/device/info") {
                            val info = DeviceInfo(
                                model = Build.MODEL,
                                manufacturer = Build.MANUFACTURER,
                                androidVersion = Build.VERSION.RELEASE,
                                sdkInt = Build.VERSION.SDK_INT,
                                ipAddress = NetworkUtils.getLocalIpAddress(),
                                hostname = Build.HOST
                            )
                            call.respond(info)
                        }

                        // SMS History
                        get("/api/sms/history") {
                            call.respond(mapOf("history" to smsHistory))
                        }

                        // Clear History
                        delete("/api/sms/history") {
                            smsHistory.clear()
                            call.respond(mapOf("message" to "History cleared"))
                        }

                        // Health Check
                        get("/api/health") {
                            call.respond(mapOf("status" to "ok"))
                        }
                    }
                }.start(wait = false)

                Logger.log("Server started on http://${NetworkUtils.getLocalIpAddress()}:8080")
                startForeground(NOTIFICATION_ID, buildNotification())
            } catch (e: Exception) {
                Logger.log("Error starting server: ${e.message}")
            }
        }
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("SMS API Server")
        .setContentText("Running on port 8080")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "SMS Server",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(android.app.NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
        Logger.log("Server stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "sms_server_channel"
    }
}