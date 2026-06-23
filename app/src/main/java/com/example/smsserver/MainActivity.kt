package com.example.smsserver

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
//import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvIpAddress: TextView
    private lateinit var tvLogs: TextView

//    private lateinit var scrollView: ScrollView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    private var server: NettyApplicationEngine? = null
    private val smsPermissionCode = 100

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvIpAddress = findViewById(R.id.tvIpAddress)
        tvLogs = findViewById(R.id.tvLogs)

        //scrollView = findViewById(R.id.scrollView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)

        val ip = getWifiIPAddress()
        tvIpAddress.text = "URL для запросов:\nhttp://$ip:8080/send"

        startButton.setOnClickListener { startLocalServer(ip) }
        stopButton.setOnClickListener { stopServer() }

        // Проверка и запрос разрешений на SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), smsPermissionCode)
        }

        // Запуск локального Ktor сервера
        //startLocalServer(ip)
    }

    private fun startLocalServer(ip: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                server = embeddedServer(Netty, port = 8080, host = ip) {
                    routing {
                        get("/send") {
                            val remoteIp = call.request.origin.remoteHost
                            logEvent("Соединение от IP: $remoteIp")

                            val params = call.request.queryParameters
                            val phone = params["phone"]
                            val text = params["text"]

                            if (phone != null && text != null) {
                                val isSent = sendSmsLocally(phone, text)
                                if (isSent) {
                                    logEvent("SMS успешно отправлено на $phone")
                                    call.respondText("OK: Sent", status = HttpStatusCode.OK)
                                } else {
                                    call.respondText("ERROR: Failed to send", status = HttpStatusCode.InternalServerError)
                                }
                            } else {
                                logEvent("Входящий запрос отклонен: неверные параметры")
                                call.respondText("ERROR: Missing phone or text", status = HttpStatusCode.BadRequest)
                            }
                        }
                        // Обработка корневого URL "http://ip-адрес:8080/"
                        get("/") {
                            val html = """
                            <!DOCTYPE html>
                            <html>
                            <head><title>Ktor Server</title></head>
                            <body>
                                <h1>Сервер на Ktor в Android!</h1>
                                <p>Ответ отправлен через метод call.respondText</p>
                            </body>
                            </html>
                        """.trimIndent()

                            // call.respondText принимает текст и тип контента (HTML)
                            call.respondText(html, ContentType.Text.Html)
                        }
                        get("/test") {
                            val remoteIp = call.request.origin.remoteHost
                            logEvent("Соединение от IP: $remoteIp")

                            val params = call.request.queryParameters
                            val phone = params["phone"]
                            val text = params["text"]

                            if (phone != null && text != null) {
                                val html = """
                            <!DOCTYPE html>
                            <html>
                            <head><title>Ktor Server</title></head>
                            <body>
                                <h1>Сервер на Ktor в Android!</h1>
                                <p>Переданные параметры</p>
                                <p>phone: $phone</p>
                                <p>text: $text</p>
                            </body>
                            </html>
                        """.trimIndent()

                                // call.respondText принимает текст и тип контента (HTML)
                                call.respondText(html, ContentType.Text.Html)
                            } else {
                                val html = """
                            <!DOCTYPE html>
                            <html>
                            <head><title>Ktor Server</title></head>
                            <body>
                                <h1>Сервер на Ktor в Android!</h1>
                                <p>Входящий запрос отклонен: неверные параметры</p>
                            </body>
                            </html>
                        """.trimIndent()
                                logEvent("Входящий запрос отклонен: неверные параметры")
                                call.respondText(html, ContentType.Text.Html)
                            }
                        }
                    }
                }
                server?.start(wait = false)
                logEvent("Сервер успешно запущен на порту 8080")
            } catch (e: Exception) {
                logEvent("Ошибка запуска сервера: ${e.message}")
            }
        }
    }

    private fun stopServer() {
        server?.stop(1000, 2000)
        logEvent("Сервер остановлен")
    }
    private fun sendSmsLocally(phone: String, text: String): Boolean {
        return try {
            val smsManager: SmsManager =
                this.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(phone, null, text, null, null)
            true
        } catch (e: Exception) {
            logEvent("Ошибка сотовой сети: ${e.message}")
            false
        }
    }

    private fun logEvent(message: String) {
        runOnUiThread {
            val timeStamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            tvLogs.append("[$timeStamp] $message\n")
        }
    }

    @Suppress("DEPRECATION")
    private fun getWifiIPAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        var ipAddress = wifiInfo.ipAddress
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress)
        }
        val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
        return try {
            InetAddress.getByAddress(ipByteArray).hostAddress ?: "0.0.0.0"
        } catch (_: Exception) {
            "0.0.0.0"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Корректно останавливаем сервер при закрытии приложения
        server?.stop(1000, 2000)
    }
}