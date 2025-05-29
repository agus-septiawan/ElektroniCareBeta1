package com.example.elektronicarebeta1.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.elektronicarebeta1.models.User
import java.text.SimpleDateFormat
import java.util.*

/**
 * Email Manager for sending booking notifications
 * Uses Intent to open email client with pre-filled content
 */
object EmailManager {

    private const val TAG = "EmailManager"
    private const val TECHNICIAN_EMAIL = "satriawiangga200@gmail.com"
    private const val COMPANY_EMAIL = "ahmadyasinalazka@gmail.com"

    /**
     * Send booking confirmation email to technician
     */
    fun sendBookingNotification(
        context: Context,
        bookingId: String,
        user: User,
        serviceName: String,
        issueDescription: String,
        appointmentDate: Date,
        estimatedCost: Double?,
        deviceImageUrl: String? = null
    ) {
        try {
            val subject = "New Booking Request - ElektroniCare #$bookingId"
            val body = createBookingEmailBody(
                bookingId = bookingId,
                user = user,
                serviceName = serviceName,
                issueDescription = issueDescription,
                appointmentDate = appointmentDate,
                estimatedCost = estimatedCost,
                deviceImageUrl = deviceImageUrl
            )

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(TECHNICIAN_EMAIL))
                putExtra(Intent.EXTRA_CC, arrayOf(COMPANY_EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }

            if (emailIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(emailIntent, "Send booking notification"))
                Log.d(TAG, "Email intent created successfully for booking: $bookingId")
                // Add Toast after starting activity
                android.widget.Toast.makeText(context, "Please complete sending the booking notification via your email app.", android.widget.Toast.LENGTH_LONG).show()
            } else {
                Log.w(TAG, "No email client available for booking notification")
                // Add Toast if no email client is found
                android.widget.Toast.makeText(context, "No email app found to send booking notification. Please install one.", android.widget.Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error creating email intent", e)
        }
    }

    /**
     * Send booking confirmation to customer
     */
    fun sendCustomerConfirmation(
        context: Context,
        bookingId: String,
        user: User,
        serviceName: String,
        appointmentDate: Date,
        estimatedCost: Double?
    ) {
        try {
            val subject = "Booking Confirmation - ElektroniCare #$bookingId"
            val body = createCustomerConfirmationBody(
                bookingId = bookingId,
                user = user,
                serviceName = serviceName,
                appointmentDate = appointmentDate,
                estimatedCost = estimatedCost
            )

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(user.email))
                putExtra(Intent.EXTRA_CC, arrayOf(COMPANY_EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }

            if (emailIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(emailIntent, "Send confirmation"))
                Log.d(TAG, "Customer confirmation email created for booking: $bookingId")
                // Add Toast after starting activity
                android.widget.Toast.makeText(context, "Please complete sending the confirmation email via your email app.", android.widget.Toast.LENGTH_LONG).show()
            } else {
                Log.w(TAG, "No email client available for customer confirmation")
                // Add Toast if no email client is found
                android.widget.Toast.makeText(context, "No email app found to send confirmation. Please install one.", android.widget.Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error creating customer confirmation email", e)
        }
    }

    private fun createBookingEmailBody(
        bookingId: String,
        user: User,
        serviceName: String,
        issueDescription: String,
        appointmentDate: Date,
        estimatedCost: Double?,
        deviceImageUrl: String?
    ): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy 'at' HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(appointmentDate)

        return """
            🔧 NEW BOOKING REQUEST - ElektroniCare
            
            Booking ID: #$bookingId
            
            📋 CUSTOMER DETAILS:
            Name: ${user.fullName}
            Email: ${user.email}
            Phone: ${user.phone ?: "Not provided"}
            Address: ${user.address ?: "Not provided"}
            
            🛠️ SERVICE DETAILS:
            Service: $serviceName
            Issue Description: $issueDescription
            Estimated Cost: ${if (estimatedCost != null) "Rp ${String.format("%,.0f", estimatedCost)}" else "To be determined"}
            
            📅 APPOINTMENT:
            Date & Time: $formattedDate
            Location: ElektroniCare Service Center
            
            ${if (deviceImageUrl != null) "📷 Device Image: $deviceImageUrl\n" else ""}
            
            ⚡ ACTION REQUIRED:
            1. Review the booking details
            2. Contact customer to confirm appointment
            3. Update booking status in the system
            4. Prepare necessary tools and parts
            
            📱 Customer Contact:
            WhatsApp: ${user.phone ?: "Not available"}
            Email: ${user.email}
            
            ---
            ElektroniCare Management System
            Generated on: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}
        """.trimIndent()
    }

    private fun createCustomerConfirmationBody(
        bookingId: String,
        user: User,
        serviceName: String,
        appointmentDate: Date,
        estimatedCost: Double?
    ): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy 'at' HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(appointmentDate)

        return """
            ✅ BOOKING CONFIRMATION - ElektroniCare
            
            Dear ${user.fullName},
            
            Thank you for choosing ElektroniCare! Your booking has been successfully submitted.
            
            📋 BOOKING DETAILS:
            Booking ID: #$bookingId
            Service: $serviceName
            Estimated Cost: ${if (estimatedCost != null) "Rp ${String.format("%,.0f", estimatedCost)}" else "To be determined"}
            
            📅 APPOINTMENT:
            Date & Time: $formattedDate
            Location: ElektroniCare Service Center
            
            📞 NEXT STEPS:
            1. Our technician will contact you within 24 hours to confirm the appointment
            2. Please prepare your device and any relevant accessories
            3. You can track your booking status in the app's History section
            
            📱 CONTACT US:
            Phone: +62 812-3456-7890
            Email: elektronicare@gmail.com
            WhatsApp: Available in the app
            
            Thank you for trusting ElektroniCare with your electronic repair needs!
            
            Best regards,
            ElektroniCare Team
            
            ---
            This is an automated message. Please do not reply to this email.
            Booking generated on: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}
        """.trimIndent()
    }
}
