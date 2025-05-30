package com.example.elektronicarebeta1.utils

import android.util.Log
import com.example.elektronicarebeta1.firebase.FirebaseManager
import kotlinx.coroutines.delay

object DebugHelper {
    private const val TAG = "DebugHelper"

    /**
     * Debug profile data persistence
     */
    suspend fun debugProfilePersistence(userId: String) {
        Log.d(TAG, "=== DEBUGGING PROFILE PERSISTENCE ===")
        Log.d(TAG, "User ID: $userId")

        // Check if user is authenticated
        val isAuth = FirebaseManager.isUserAuthenticated()
        Log.d(TAG, "User authenticated: $isAuth")

        // Get current user data
        val userData = FirebaseManager.getCurrentUser()
        Log.d(TAG, "Current user data: $userData")

        // Get user document from Firestore
        val userDoc = FirebaseManager.getUserData()
        Log.d(TAG, "User document exists: ${userDoc != null}")
        Log.d(TAG, "User document data: ${userDoc?.toString()}")
    }

    /**
     * Debug booking data persistence
     */
    suspend fun debugBookingPersistence(repairId: String) {
        Log.d(TAG, "=== DEBUGGING BOOKING PERSISTENCE ===")
        Log.d(TAG, "Repair ID: $repairId")

        // Wait a moment for data to propagate
        delay(2000)

        // Check if repair exists
        val repair = FirebaseManager.getRepairById(repairId)
        Log.d(TAG, "Repair exists: ${repair != null}")
        Log.d(TAG, "Repair data: ${repair?.id}")

        // Get all user repairs
        val userRepairs = FirebaseManager.getUserRepairs()
        Log.d(TAG, "Total user repairs: ${userRepairs?.size()}")

        userRepairs?.documents?.forEach { doc ->
            Log.d(TAG, "Repair: ${doc.id} -> ${doc.toString()}")
        }
    }

    /**
     * Debug history loading
     */
    suspend fun debugHistoryLoading() {
        Log.d(TAG, "=== DEBUGGING HISTORY LOADING ===")

        val userId = FirebaseManager.getUserId()
        Log.d(TAG, "Current user ID: $userId")

        if (userId != null) {
            val repairs = FirebaseManager.getUserRepairs()
            Log.d(TAG, "Found ${repairs?.size()} repairs for user")

            repairs?.documents?.forEachIndexed { index, doc ->
                Log.d(TAG, "Repair $index: ID=${doc.id}")
                Log.d(TAG, "  Data: ${doc.toString()}")
                Log.d(TAG, "  Status: ${doc.getString("status")}")
                Log.d(TAG, "  Device: ${doc.getString("deviceModel")}")
                Log.d(TAG, "  Created: ${doc.getDate("createdAt")}")
            }
        }
    }

    /**
     * Debug Cloudinary configuration
     */
    suspend fun debugCloudinaryConfig(context: android.content.Context) {
        Log.d(TAG, "=== DEBUGGING CLOUDINARY CONFIG ===")

        val cloudName = com.example.elektronicarebeta1.cloudinary.CloudinaryConfig.getCloudName(context)
        val apiKey = com.example.elektronicarebeta1.cloudinary.CloudinaryConfig.getApiKey(context)
        val isConfigured = com.example.elektronicarebeta1.cloudinary.CloudinaryConfig.isConfigured(context)

        Log.d(TAG, "Cloud Name: $cloudName")
        Log.d(TAG, "API Key: ${apiKey.take(8)}...")
        Log.d(TAG, "Is Configured: $isConfigured")

        val managerConfigured = com.example.elektronicarebeta1.cloudinary.CloudinaryManager.isConfigured(context)
        Log.d(TAG, "Manager Configured: $managerConfigured")
    }
}
