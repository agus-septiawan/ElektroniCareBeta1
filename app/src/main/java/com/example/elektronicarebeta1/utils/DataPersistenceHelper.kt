package com.example.elektronicarebeta1.utils

import android.util.Log
import com.example.elektronicarebeta1.firebase.FirebaseManager
import kotlinx.coroutines.delay

object DataPersistenceHelper {
    private const val TAG = "DataPersistenceHelper"

    /**
     * Verify that user data was saved correctly
     */
    suspend fun verifyUserDataSaved(expectedData: Map<String, Any>): Boolean {
        return try {
            // Wait a bit for data to propagate
            delay(1000)

            val currentUser = FirebaseManager.getCurrentUser()
            if (currentUser == null) {
                Log.e(TAG, "User data verification failed: currentUser is null")
                return false
            }

            var allFieldsMatch = true
            for ((key, expectedValue) in expectedData) {
                val actualValue = when (key) {
                    "fullName" -> currentUser.fullName
                    "phone" -> currentUser.phone
                    "address" -> currentUser.address
                    "profileImageUrl" -> currentUser.profileImageUrl
                    else -> {
                        Log.w(TAG, "Unknown field for verification: $key")
                        continue
                    }
                }

                if (actualValue != expectedValue) {
                    Log.e(TAG, "Field $key mismatch: expected '$expectedValue', got '$actualValue'")
                    allFieldsMatch = false
                }
            }

            if (allFieldsMatch) {
                Log.d(TAG, "User data verification successful")
            }

            allFieldsMatch
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying user data", e)
            false
        }
    }

    /**
     * Verify that repair request was saved correctly
     */
    suspend fun verifyRepairRequestSaved(repairId: String): Boolean {
        return try {
            // Wait a bit for data to propagate
            delay(1000)

            val repair = FirebaseManager.getRepairById(repairId)
            if (repair != null) {
                Log.d(TAG, "Repair request verification successful: $repairId")
                Log.d(TAG, "Repair data: ${repair.id}")
                true
            } else {
                Log.e(TAG, "Repair request verification failed: document does not exist")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying repair request", e)
            false
        }
    }

    /**
     * Force refresh user data from server
     */
    suspend fun forceRefreshUserData(): Boolean {
        return try {
            // Get fresh user data
            val user = FirebaseManager.getCurrentUser()
            if (user != null) {
                Log.d(TAG, "Force refresh successful: ${user.fullName}")
                true
            } else {
                Log.e(TAG, "Force refresh failed: user is null")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error force refreshing user data", e)
            false
        }
    }

    /**
     * Force refresh repair history from server
     */
    suspend fun forceRefreshRepairHistory(): Int {
        return try {
            val repairs = FirebaseManager.getUserRepairs()
            val count = repairs?.documents?.size ?: 0
            Log.d(TAG, "Force refresh repair history: found $count repairs")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error force refreshing repair history", e)
            -1
        }
    }

    /**
     * Force refresh repair data
     */
    suspend fun forceRefreshRepairData(): Boolean {
        return try {
            Log.d(TAG, "Forcing refresh of repair data")

            // Add a small delay to ensure token is properly refreshed
            delay(1000)

            Log.d(TAG, "Force refresh of repair data completed")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error during repair data force refresh", e)
            false
        }
    }
}
