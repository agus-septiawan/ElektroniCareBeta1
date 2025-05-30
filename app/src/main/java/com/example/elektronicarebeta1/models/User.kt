package com.example.elektronicarebeta1.models

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.Timestamp
import java.util.Date

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val address: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: Date? = null
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): User? {
            return try {
                Log.d("User", "Parsing user document: ${document.id}")
                Log.d("User", "Document data: ${document.data}")

                val id = document.id
                val fullName = document.getString("fullName") ?: ""
                val email = document.getString("email") ?: ""
                val phone = document.getString("phone")
                val address = document.getString("address")
                val profileImageUrl = document.getString("profileImageUrl")

                // Handle both Date and Timestamp for createdAt
                val createdAt = document.getDate("createdAt") ?: run {
                    val timestamp = document.getTimestamp("createdAt")
                    timestamp?.toDate()
                }

                val user = User(
                    id = id,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    address = address,
                    profileImageUrl = profileImageUrl,
                    createdAt = createdAt
                )

                Log.d("User", "Successfully parsed user: $fullName")
                user
            } catch (e: Exception) {
                Log.e("User", "Error parsing user document", e)
                null
            }
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "fullName" to fullName,
            "email" to email,
            "phone" to phone,
            "address" to address,
            "profileImageUrl" to profileImageUrl,
            "createdAt" to createdAt
        )
    }
}
