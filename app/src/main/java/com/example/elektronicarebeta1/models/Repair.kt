package com.example.elektronicarebeta1.models

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

data class Repair(
    val id: String,
    val userId: String,
    val deviceType: String,
    val deviceModel: String,
    val issueDescription: String,
    val serviceId: String? = null,
    val technicianEmail: String? = null,
    val status: String,
    val estimatedCost: Double? = null,
    val appointmentTimestamp: Date? = null,
    val completedDate: Date? = null,
    val location: String? = null,
    val deviceImageUrl: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    companion object {
        private const val TAG = "Repair"

        fun fromDocument(document: DocumentSnapshot): Repair? {
            return try {
                Log.d(TAG, "=== PARSING REPAIR DOCUMENT ===")
                Log.d(TAG, "📄 Document ID: ${document.id}")
                Log.d(TAG, "📊 Document exists: ${document.exists()}")
                Log.d(TAG, "📝 Document data: ${document.data}")

                if (!document.exists()) {
                    Log.e(TAG, "❌ Document does not exist!")
                    return null
                }

                val id = document.id
                val userId = document.getString("userId") ?: ""
                val deviceType = document.getString("deviceType") ?: "Electronic Device"
                val deviceModel = document.getString("deviceModel") ?: document.getString("issueDescription") ?: "Unknown Model"
                val issueDescription = document.getString("issueDescription") ?: ""
                val serviceId = document.getString("serviceId")
                val technicianEmail = document.getString("technicianEmail")
                val status = document.getString("status") ?: "pending"
                val estimatedCost = document.getDouble("estimatedCost")
                val location = document.getString("location")
                val deviceImageUrl = document.getString("deviceImageUrl")

                Log.d(TAG, "✅ Basic fields parsed:")
                Log.d(TAG, "  userId: $userId")
                Log.d(TAG, "  deviceType: $deviceType")
                Log.d(TAG, "  deviceModel: $deviceModel")
                Log.d(TAG, "  status: $status")
                Log.d(TAG, "  estimatedCost: $estimatedCost")
                Log.d(TAG, "  location: $location")

                // Handle both Timestamp and Date for appointmentTimestamp
                val appointmentTimestamp = when (val appointmentField = document.get("appointmentTimestamp")) {
                    is Timestamp -> appointmentField.toDate()
                    is Date -> appointmentField
                    else -> null
                }

                // Handle completedDate
                val completedDate = when (val completedField = document.get("completedDate")) {
                    is Timestamp -> completedField.toDate()
                    is Date -> completedField
                    else -> null
                }

                // Handle createdAt
                val createdAt = when (val createdAtField = document.get("createdAt")) {
                    is Timestamp -> createdAtField.toDate()
                    is Date -> createdAtField
                    else -> {
                        Log.w(TAG, "createdAt field is null or unknown type: $createdAtField")
                        null
                    }
                }

                // Handle updatedAt
                val updatedAt = when (val updatedAtField = document.get("updatedAt")) {
                    is Timestamp -> updatedAtField.toDate()
                    is Date -> updatedAtField
                    else -> null
                }

                val repair = Repair(
                    id = id,
                    userId = userId,
                    deviceType = deviceType,
                    deviceModel = deviceModel,
                    issueDescription = issueDescription,
                    serviceId = serviceId,
                    technicianEmail = technicianEmail,
                    status = status,
                    estimatedCost = estimatedCost,
                    appointmentTimestamp = appointmentTimestamp,
                    completedDate = completedDate,
                    location = location,
                    deviceImageUrl = deviceImageUrl,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )

                Log.d(TAG, "✅ Successfully parsed repair:")
                Log.d(TAG, "  📱 Device: $deviceModel")
                Log.d(TAG, "  📊 Status: $status")
                Log.d(TAG, "  📅 Created: $createdAt")
                Log.d(TAG, "  📍 Location: $location")
                Log.d(TAG, "  💰 Cost: $estimatedCost")

                repair
            } catch (e: Exception) {
                Log.e(TAG, "💥 ERROR parsing repair document: ${document.id}", e)
                Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Error message: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "deviceType" to deviceType,
            "deviceModel" to deviceModel,
            "issueDescription" to issueDescription,
            "serviceId" to serviceId,
            "technicianEmail" to technicianEmail,
            "status" to status,
            "estimatedCost" to estimatedCost,
            "appointmentTimestamp" to appointmentTimestamp,
            "completedDate" to completedDate,
            "location" to location,
            "deviceImageUrl" to deviceImageUrl,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
