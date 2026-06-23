package com.example.smsserver.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.example.smsserver.models.SmsResponse
import com.example.smsserver.utils.Logger

class SmsService {

    fun sendSms(phoneNumber: String, message: String): SmsResponse {
        return try {
            validateInput(phoneNumber, message)

            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)

            val sentIntents = parts.map {
                PendingIntent.getBroadcast(
                    null,
                    0,
                    Intent("SMS_SENT"),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            smsManager.sendMultipartTextMessage(
                phoneNumber,
                null,
                parts,
                sentIntents as ArrayList<PendingIntent>,
                null
            )

            Logger.log("SMS sent to: $phoneNumber (${parts.size} parts)")
            SmsResponse(success = true, message = "SMS sent successfully")
        } catch (e: Exception) {
            Logger.log("SMS Error: ${e.message}")
            SmsResponse(success = false, message = "Error: ${e.message}")
        }
    }

    private fun validateInput(phoneNumber: String, message: String) {
        if (phoneNumber.isBlank()) {
            throw IllegalArgumentException("Phone number cannot be empty")
        }
        if (message.isBlank()) {
            throw IllegalArgumentException("Message cannot be empty")
        }
        if (message.length > 1000) {
            throw IllegalArgumentException("Message too long (max 1000 chars)")
        }
    }
}