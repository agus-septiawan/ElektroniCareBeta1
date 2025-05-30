package com.example.elektronicarebeta1.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.example.elektronicarebeta1.models.User
import kotlinx.coroutines.tasks.await
import java.util.Date

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private const val USERS_COLLECTION = "users"
    private const val REPAIRS_COLLECTION = "repairs"
    private const val TECHNICIANS_COLLECTION = "technicians"
    private const val SERVICES_COLLECTION = "services"

    // User operations
    fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser

    fun getUserId(): String? = auth.currentUser?.uid

    suspend fun getCurrentUser(): User? {
        val userId = getUserId() ?: return null
        return try {
            val document = db.collection(USERS_COLLECTION).document(userId).get().await()
            if (document.exists()) {
                User.fromDocument(document)
            } else {
                Log.w(TAG, "User document not found for ID: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            null
        }
    }

    suspend fun getUserData(): DocumentSnapshot? {
        val userId = getUserId() ?: return null
        return try {
            val document = db.collection(USERS_COLLECTION).document(userId).get().await()
            Log.d(TAG, "getUserData: userId=$userId, exists=${document.exists()}")
            document
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user data", e)
            null
        }
    }

    // Create or update user data
    suspend fun createOrUpdateUserData(userData: Map<String, Any>): Boolean {
        val userId = getUserId() ?: return false
        return try {
            Log.d(TAG, "Creating/updating user data for userId: $userId")

            val dataWithTimestamp = userData.toMutableMap()

            // Check if user document exists
            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()

            if (!userDoc.exists()) {
                // New user - add createdAt
                dataWithTimestamp["createdAt"] = Date()
                Log.d(TAG, "Creating new user document")
            } else {
                // Existing user - add updatedAt
                dataWithTimestamp["updatedAt"] = Date()
                Log.d(TAG, "Updating existing user document")
            }

            db.collection(USERS_COLLECTION).document(userId).set(dataWithTimestamp).await()
            Log.d(TAG, "User data saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating/updating user data", e)
            false
        }
    }

    suspend fun updateUserData(userData: Map<String, Any>): Boolean {
        val userId = getUserId() ?: return false
        return try {
            Log.d(TAG, "Updating user data for userId: $userId")

            val dataWithTimestamp = userData.toMutableMap()
            dataWithTimestamp["updatedAt"] = Date()

            db.collection(USERS_COLLECTION).document(userId).update(dataWithTimestamp).await()
            Log.d(TAG, "User data updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user data", e)
            false
        }
    }

    // Repair operations
    suspend fun getUserRepairs(): QuerySnapshot? {
        val userId = getUserId() ?: return null
        return try {
            val querySnapshot = db.collection(REPAIRS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            Log.d(TAG, "getUserRepairs: userId=$userId, count=${querySnapshot.size()}")
            querySnapshot
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user repairs", e)
            null
        }
    }

    suspend fun getRepairById(repairId: String): DocumentSnapshot? {
        return try {
            db.collection(REPAIRS_COLLECTION).document(repairId).get().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting repair by id", e)
            null
        }
    }

    suspend fun createRepairRequest(repairData: Map<String, Any?>): String? {
        val userId = getUserId() ?: return null
        val repairWithUser = repairData.toMutableMap()

        // Ensure required fields
        repairWithUser["userId"] = userId
        repairWithUser["createdAt"] = Date()

        return try {
            Log.d(TAG, "Creating repair request for userId: $userId")
            Log.d(TAG, "Repair data: $repairWithUser")

            val docRef = db.collection(REPAIRS_COLLECTION).add(repairWithUser).await()
            Log.d(TAG, "Repair request created successfully with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating repair request", e)
            null
        }
    }

    suspend fun cancelRepairRequest(repairId: String): Boolean {
        return try {
            val updates = mapOf(
                "status" to "cancelled",
                "cancelledAt" to Date()
            )
            db.collection(REPAIRS_COLLECTION).document(repairId).update(updates).await()
            Log.d(TAG, "Repair request cancelled successfully: $repairId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling repair request", e)
            false
        }
    }

    // Technician operations
    suspend fun getAllTechnicians(): QuerySnapshot? {
        return try {
            db.collection(TECHNICIANS_COLLECTION).get().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting technicians", e)
            null
        }
    }

    suspend fun getTechnicianById(technicianId: String): DocumentSnapshot? {
        return try {
            db.collection(TECHNICIANS_COLLECTION).document(technicianId).get().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting technician by id", e)
            null
        }
    }

    // Service operations
    suspend fun getAllServices(): QuerySnapshot? {
        return try {
            db.collection(SERVICES_COLLECTION).get().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting services", e)
            null
        }
    }

    suspend fun getServiceById(serviceId: String): DocumentSnapshot? {
        return try {
            db.collection(SERVICES_COLLECTION).document(serviceId).get().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting service by id", e)
            null
        }
    }

    // Authentication operations
    fun signOut() {
        auth.signOut()
    }

    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }
}
