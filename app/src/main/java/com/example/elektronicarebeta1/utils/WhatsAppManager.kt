package com.example.elektronicarebeta1.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * WhatsApp Manager for sending messages to technician
 * Handles WhatsApp integration with booking information
 */
object WhatsAppManager {

    private const val TAG = "WhatsAppManager"
    private const val TECHNICIAN_PHONE = "+6285156789012" // Technician phone number

    /**
     * Send booking details to technician via WhatsApp
     */
    fun sendBookingToTechnician(
        context: Context,
        bookingId: String,
        customerName: String,
        serviceName: String,
        issueDescription: String,
        appointmentDate: Date,
        estimatedCost: Double?,
        customerPhone: String? = null
    ) {
        try {
            val message = createBookingMessage(
                bookingId = bookingId,
                customerName = customerName,
                serviceName = serviceName,
                issueDescription = issueDescription,
                appointmentDate = appointmentDate,
                estimatedCost = estimatedCost,
                customerPhone = customerPhone
            )

            sendWhatsAppMessage(context, TECHNICIAN_PHONE, message)

        } catch (e: Exception) {
            Log.e(TAG, "Error sending booking to technician", e)
            Toast.makeText(context, "Failed to open WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Send message to customer via WhatsApp
     */
    fun sendMessageToCustomer(
        context: Context,
        customerPhone: String,
        bookingId: String,
        message: String? = null
    ) {
        try {
            val defaultMessage = message ?: createCustomerMessage(bookingId)
            sendWhatsAppMessage(context, customerPhone, defaultMessage)

        } catch (e: Exception) {
            Log.e(TAG, "Error sending message to customer", e)
            Toast.makeText(context, "Failed to open WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Send WhatsApp message using Intent
     */
    private fun sendWhatsAppMessage(context: Context, phoneNumber: String, message: String) {
        try {
            // Clean phone number (remove spaces, dashes, etc.)
            val cleanPhone = phoneNumber.replace(Regex("[^+\\d]"), "")

            // Encode message for URL
            val encodedMessage = URLEncoder.encode(message, "UTF-8")

            // Create WhatsApp intent
            val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
                setPackage("com.whatsapp")
            }

            // Check if WhatsApp is installed
            if (isWhatsAppInstalled(context)) {
                context.startActivity(whatsappIntent)
                Log.d(TAG, "WhatsApp message sent to: $cleanPhone")
                Toast.makeText(context, "Please complete sending the message in WhatsApp.", Toast.LENGTH_LONG).show()
            } else {
                // Fallback to WhatsApp Web
                Toast.makeText(context, "WhatsApp app not found. Opening WhatsApp Web in browser.", Toast.LENGTH_LONG).show()
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://web.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
                }
                context.startActivity(webIntent)
                Log.d(TAG, "WhatsApp Web opened for: $cleanPhone")
                Toast.makeText(context, "Please complete sending the message in WhatsApp.", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error opening WhatsApp", e)
            Toast.makeText(context, "Failed to open WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Check if WhatsApp is installed
     */
    private fun isWhatsAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Create booking message for technician
     */
    private fun createBookingMessage(
        bookingId: String,
        customerName: String,
        serviceName: String,
        issueDescription: String,
        appointmentDate: Date,
        estimatedCost: Double?,
        customerPhone: String?
    ): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy 'at' HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(appointmentDate)

        return """
            🔧 *NEW BOOKING - ElektroniCare*
            
            📋 *Booking ID:* #$bookingId
            
            👤 *Customer:* $customerName
            📞 *Phone:* ${customerPhone ?: "Not provided"}
            
            🛠️ *Service:* $serviceName
            💰 *Est. Cost:* ${if (estimatedCost != null) "Rp ${String.format("%,.0f", estimatedCost)}" else "TBD"}
            
            📅 *Appointment:* $formattedDate
            📍 *Location:* ElektroniCare Service Center
            
            📝 *Issue Description:*
            $issueDescription
            
            ⚡ *Action Required:*
            • Contact customer to confirm
            • Update booking status
            • Prepare tools & parts
            
            _Generated by ElektroniCare App_
        """.trimIndent()
    }

    /**
     * Create message for customer
     */
    private fun createCustomerMessage(bookingId: String): String {
        return """
            Hello! 👋
            
            This is regarding your ElektroniCare booking #$bookingId.
            
            Our technician will contact you shortly to confirm your appointment details.
            
            Thank you for choosing ElektroniCare! 🔧
        """.trimIndent()
    }

    /**
     * Create appointment reminder message
     */
    fun createAppointmentReminder(
        bookingId: String,
        appointmentDate: Date,
        serviceName: String
    ): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy 'at' HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(appointmentDate)

        return """
            🔔 *Appointment Reminder - ElektroniCare*
            
            📋 *Booking ID:* #$bookingId
            🛠️ *Service:* $serviceName
            📅 *Date & Time:* $formattedDate
            📍 *Location:* ElektroniCare Service Center
            
            Please bring:
            • Your device
            • Charger/accessories
            • Valid ID
            
            See you soon! 😊
        """.trimIndent()
    }

    /**
     * Create status update message
     */
    fun createStatusUpdateMessage(
        bookingId: String,
        status: String,
        additionalInfo: String? = null
    ): String {
        val statusEmoji = when (status.lowercase()) {
            "confirmed" -> "✅"
            "in_progress" -> "🔧"
            "completed" -> "✅"
            "cancelled" -> "❌"
            else -> "📋"
        }

        val statusText = when (status.lowercase()) {
            "confirmed" -> "Confirmed"
            "in_progress" -> "In Progress"
            "completed" -> "Completed"
            "cancelled" -> "Cancelled"
            else -> status.capitalize()
        }

        return """
            $statusEmoji *Booking Update - ElektroniCare*
            
            📋 *Booking ID:* #$bookingId
            📊 *Status:* $statusText
            
            ${additionalInfo ?: ""}
            
            Thank you for choosing ElektroniCare! 🔧
        """.trimIndent()
    }
}
