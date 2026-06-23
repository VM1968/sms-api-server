package com.example.smsserver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.BatteryManager
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smsserver.services.ApiServerService
import com.example.smsserver.utils.NetworkUtils
import com.example.smsserver.utils.Logger
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var infoText: TextView
    private lateinit var logText: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var testButton: Button

    private val scope = MainScope()
    private var isServerRunning = false

    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.SEND_SMS,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.ACCESS_NETWORK_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        requestPermissions()
        updateDeviceInfo()
    }

    private fun initViews() {
        statusText = findViewById(R.id.statusText)
        infoText = findViewById(R.id.infoText)
        logText = findViewById(R.id.logText)
        scrollView = findViewById(R.id.scrollView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        testButton = findViewById(R.id.testButton)

        startButton.setOnClickListener { startServer() }
        stopButton.setOnClickListener { stopServer() }
        testButton.setOnClickListener { testSms() }

        Logger.setLogCallback { message ->
            runOnUiThread {
                logText.append(message + "\n")
                scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }
    }

    private fun requestPermissions() {
        val missingPermissions = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startServer() {
        val intent = Intent(this, ApiServerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isServerRunning = true
        updateStatus()
        Toast.makeText(this, "Server started", Toast.LENGTH_SHORT).show()
    }

    private fun stopServer() {
        val intent = Intent(this, ApiServerService::class.java)
        stopService(intent)
        isServerRunning = false
        updateStatus()
        Toast.makeText(this, "Server stopped", Toast.LENGTH_SHORT).show()
    }

    private fun testSms() {
        scope.launch {
            try {
                Logger.log("Testing SMS send...")
                Toast.makeText(this@MainActivity, "Check SMS app for test message", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Logger.log("Error: ${e.message}")
            }
        }
    }

    private fun updateStatus() {
        val status = if (isServerRunning) "🟢 RUNNING" else "🔴 STOPPED"
        val ipAddress = NetworkUtils.getLocalIpAddress()
        statusText.text = "Server Status: $status\nIP: $ipAddress\nPort: 8080"
        startButton.isEnabled = !isServerRunning
        stopButton.isEnabled = isServerRunning
    }

    private fun updateDeviceInfo() {
        val deviceInfo = buildString {
            append("Device: ${Build.MODEL}\n")
            append("Android: ${Build.VERSION.RELEASE}\n")
            append("IP Address: ${NetworkUtils.getLocalIpAddress()}\n")
            append("Battery: ${getBatteryPercentage()}%\n")
            append("URL: http://${NetworkUtils.getLocalIpAddress()}:8080\n")
        }
        infoText.text = deviceInfo
    }

    private fun getBatteryPercentage(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        updateDeviceInfo()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}