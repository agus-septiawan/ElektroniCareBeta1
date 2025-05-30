package com.example.elektronicarebeta1.utils

import android.content.Context
import android.util.Log
import com.example.elektronicarebeta1.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.*

object EmailService {
    private const val TAG = "EmailService"
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"
    private const val EMAIL_FROM = "ahmadyasinalazka@gmail.com"
    private const val EMAIL_PASSWORD = "zryh ebkn dcir vzky" // Use App Password
    private const val TECHNICIAN_EMAIL = "satriawiangga200@gmail.com"
    private const val COMPANY_EMAIL = "ahmadyasinalazka@gmail.com"

    /**
     * Send booking notification email to technician
     */
    suspend fun sendBookingNotification(
        context: Context,
        bookingId: String,
        user: User,
        serviceName: String,
        issueDescription: String,
        appointmentDate: Date,
        estimatedCost: Double?,
        deviceImageUrl: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
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

            sendEmail(
                to = arrayOf(TECHNICIAN_EMAIL),
                cc = arrayOf(COMPANY_EMAIL),
                subject = subject,
                body = body
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error sending booking notification", e)
            false
        }
    }

    /**
     * Send booking confirmation to customer
     */
    suspend fun sendCustomerConfirmation(
        context: Context,
        bookingId: String,
        user: User,
        serviceName: String,
        appointmentDate: Date,
        estimatedCost: Double?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val subject = "Booking Confirmation - ElektroniCare #$bookingId"
            val body = createCustomerConfirmationBody(
                bookingId = bookingId,
                user = user,
                serviceName = serviceName,
                appointmentDate = appointmentDate,
                estimatedCost = estimatedCost
            )

            sendEmail(
                to = arrayOf(user.email),
                cc = arrayOf(COMPANY_EMAIL),
                subject = subject,
                body = body
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error sending customer confirmation", e)
            false
        }
    }

    private suspend fun sendEmail(
        to: Array<String>,
        cc: Array<String> = emptyArray(),
        subject: String,
        body: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", SMTP_HOST)
                put("mail.smtp.port", SMTP_PORT)
                put("mail.smtp.ssl.trust", SMTP_HOST)
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(EMAIL_FROM, "ElektroniCare Service"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to.joinToString(",")))
                if (cc.isNotEmpty()) {
                    setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc.joinToString(",")))
                }
                setSubject(subject, "UTF-8")
                setText(body, "UTF-8", "html")
                sentDate = Date()
            }

            Transport.send(message)
            Log.d(TAG, "Email sent successfully to: ${to.joinToString(", ")}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email", e)
            false
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
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>New Booking Request</title>
        </head>
        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
            <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px;">
                    🔧 NEW BOOKING REQUEST - ElektroniCare
                </h2>
                
                <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3 style="color: #e74c3c; margin-top: 0;">Booking ID: #$bookingId</h3>
                </div>

                <h3 style="color: #2c3e50;">📋 CUSTOMER DETAILS:</h3>
                <ul>
                    <li><strong>Name:</strong> ${user.fullName}</li>
                    <li><strong>Email:</strong> ${user.email}</li>
                    <li><strong>Phone:</strong> ${user.phone ?: "Not provided"}</li>
                    <li><strong>Address:</strong> ${user.address ?: "Not provided"}</li>
                </ul>

                <h3 style="color: #2c3e50;">🛠️ SERVICE DETAILS:</h3>
                <ul>
                    <li><strong>Service:</strong> $serviceName</li>
                    <li><strong>Issue Description:</strong> $issueDescription</li>
                    <li><strong>Estimated Cost:</strong> ${if (estimatedCost != null) "Rp ${String.format("%,.0f", estimatedCost)}" else "To be determined"}</li>
                </ul>

                <h3 style="color: #2c3e50;">📅 APPOINTMENT:</h3>
                <ul>
                    <li><strong>Date & Time:</strong> $formattedDate</li>
                    <li><strong>Location:</strong> ElektroniCare Service Center</li>
                </ul>

                ${if (deviceImageUrl != null) """
                <h3 style="color: #2c3e50;">📷 DEVICE IMAGE:</h3>
                <img src="$deviceImageUrl" alt="Device Image" style="max-width: 300px; border-radius: 5px;">
                """ else ""}

                <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3 style="color: #856404; margin-top: 0;">⚡ ACTION REQUIRED:</h3>
                    <ol>
                        <li>Review the booking details</li>
                        <li>Contact customer to confirm appointment</li>
                        <li>Update booking status in the system</li>
                        <li>Prepare necessary tools and parts</li>
                    </ol>
                </div>

                <h3 style="color: #2c3e50;">📱 CUSTOMER CONTACT:</h3>
                <ul>
                    <li><strong>WhatsApp:</strong> ${user.phone ?: "Not available"}</li>
                    <li><strong>Email:</strong> ${user.email}</li>
                </ul>

                <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
                
                <p style="text-align: center; color: #7f8c8d; font-size: 12px;">
                    ElektroniCare Management System<br>
                    Generated on: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}
                </p>
            </div>
        </body>
        </html>
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
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Booking Confirmation</title>
        </head>
        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
            <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #27ae60; border-bottom: 2px solid #27ae60; padding-bottom: 10px;">
                    ✅ BOOKING CONFIRMATION - ElektroniCare
                </h2>
                
                <p>Dear ${user.fullName},</p>
                
                <p>Thank you for choosing ElektroniCare! Your booking has been successfully submitted.</p>

                <div style="background-color: #d4edda; border: 1px solid #c3e6cb; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3 style="color: #155724; margin-top: 0;">📋 BOOKING DETAILS:</h3>
                    <ul>
                        <li><strong>Booking ID:</strong> #$bookingId</li>
                        <li><strong>Service:</strong> $serviceName</li>
                        <li><strong>Estimated Cost:</strong> ${if (estimatedCost != null) "Rp ${String.format("%,.0f", estimatedCost)}" else "To be determined"}</li>
                    </ul>
                </div>

                <h3 style="color: #2c3e50;">📅 APPOINTMENT:</h3>
                <ul>
                    <li><strong>Date & Time:</strong> $formattedDate</li>
                    <li><strong>Location:</strong> ElektroniCare Service Center</li>
                </ul>

                <div style="background-color: #cce5ff; border: 1px solid #99ccff; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3 style="color: #004080; margin-top: 0;">📞 NEXT STEPS:</h3>
                    <ol>
                        <li>Our technician will contact you within 24 hours to confirm the appointment</li>
                        <li>Please prepare your device and any relevant accessories</li>
                        <li>You can track your booking status in the app's History section</li>
                    </ol>
                </div>

                <h3 style="color: #2c3e50;">📱 CONTACT US:</h3>
                <ul>
                    <li><strong>Phone:</strong> +62 812-3456-7890</li>
                    <li><strong>Email:</strong> elektronicare@gmail.com</li>
                    <li><strong>WhatsApp:</strong> Available in the app</li>
                </ul>

                <p>Thank you for trusting ElektroniCare with your electronic repair needs!</p>

                <p style="margin-top: 30px;">
                    Best regards,<br>
                    <strong>ElektroniCare Team</strong>
                </p>

                <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
                
                <p style="text-align: center; color: #7f8c8d; font-size: 12px;">
                    This is an automated message. Please do not reply to this email.<br>
                    Booking generated on: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}
                </p>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}
