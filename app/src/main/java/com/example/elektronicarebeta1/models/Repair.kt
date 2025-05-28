package com.example.elektronicarebeta1.models

import android.util.Log
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
    val createdAt: Date? = null
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): Repair? {
            return try {
                Log.d("Repair", "Parsing repair document: ${document.id}")
                Log.d("Repair", "Document data: ${document.data}")
                
                val id = document.id
                val userId = document.getString("userId") ?: ""
                val deviceType = document.getString("deviceType") ?: ""
                val deviceModel = document.getString("deviceModel") ?: ""
                val issueDescription = document.getString("issueDescription") ?: ""
                val serviceId = document.getString("serviceId")
                val technicianEmail = document.getString("technicianEmail")
                val status = document.getString("status") ?: "pending"
                val estimatedCost = document.getDouble("estimatedCost")
                val appointmentTimestamp = document.getDate("appointmentTimestamp")
                val completedDate = document.getDate("completedDate")
                val location = document.getString("location")
                val createdAt = document.getDate("createdAt")
                
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
                    createdAt = createdAt
                )
                
                Log.d("Repair", "Successfully parsed repair: $deviceModel - $status")
                repair
            } catch (e: Exception) {
                Log.e("Repair", "Error parsing repair document", e)
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
            "createdAt" to createdAt
        )
    }
}