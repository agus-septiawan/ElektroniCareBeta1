package com.example.elektronicarebeta1.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
// Removed Firebase Storage import - now using Cloudinary
import com.example.elektronicarebeta1.models.User
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

/**
 * Singleton class to manage all Firebase operations
 */
object FirebaseManager {
    private const val TAG = "FirebaseManager"
    
    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    // Removed Firebase Storage - now using Cloudinary
    
    // Collection references
    private const val USERS_COLLECTION = "users"
    private const val REPAIRS_COLLECTION = "repairs"
    private const val TECHNICIANS_COLLECTION = "technicians"
    private const val SERVICES_COLLECTION = "services"
    
    // Removed storage references - now using Cloudinary
    
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
            if (document.exists()) {
                Log.d(TAG, "User data: ${document.data}")
            }
            document
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user data", e)
            null
        }
    }
    
    suspend fun updateUserData(userData: Map<String, Any>): Boolean {
        val userId = getUserId() ?: return false
        return try {
            Log.d(TAG, "Updating user data for userId: $userId")
            Log.d(TAG, "Update data: $userData")
            
            // Add timestamp for tracking
            val dataWithTimestamp = userData.toMutableMap()
            dataWithTimestamp["updatedAt"] = Date()
            
            db.collection(USERS_COLLECTION).document(userId).update(dataWithTimestamp).await()
            
            Log.d(TAG, "User data updated successfully")
            
            // Verify the update by reading back the data
            val updatedDoc = db.collection(USERS_COLLECTION).document(userId).get().await()
            if (updatedDoc.exists()) {
                Log.d(TAG, "Verification: Updated document exists with data: ${updatedDoc.data}")
                true
            } else {
                Log.e(TAG, "Verification failed: Document does not exist after update")
                false
            }
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
            for (doc in querySnapshot.documents) {
                Log.d(TAG, "Repair: ${doc.id} -> ${doc.data}")
            }
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
        repairWithUser.putIfAbsent("createdAt", Date()) // Default if not provided
        // Don't override status if it's already provided in repairData
        repairWithUser["userId"] = userId // Current user's ID always takes precedence
        
        return try {
            Log.d(TAG, "Creating repair request for userId: $userId")
            Log.d(TAG, "Repair data: $repairWithUser")
            
            val docRef = db.collection(REPAIRS_COLLECTION).add(repairWithUser).await()
            
            Log.d(TAG, "Repair request created successfully with ID: ${docRef.id}")
            
            // Verify the creation by reading back the data
            val createdDoc = docRef.get().await()
            if (createdDoc.exists()) {
                Log.d(TAG, "Verification: Created repair document exists with data: ${createdDoc.data}")
                docRef.id
            } else {
                Log.e(TAG, "Verification failed: Repair document does not exist after creation")
                null
            }
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
    
    suspend fun refreshAuthToken(): Boolean {
        return try {
            val user = auth.currentUser
            if (user != null) {
                // Force refresh the token
                val tokenResult = user.getIdToken(true).await()
                Log.d(TAG, "Auth token refreshed successfully. Token: ${tokenResult.token?.take(20)}...")
                
                // Verify the user is still valid
                val userDoc = getUserDocument()
                if (userDoc?.exists() == true) {
                    Log.d(TAG, "User document verified after token refresh")
                    true
                } else {
                    Log.w(TAG, "User document not found after token refresh")
                    false
                }
            } else {
                Log.w(TAG, "Cannot refresh token: user is null")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing auth token", e)
            false
        }
    }
    
    suspend fun forceSyncUserData(): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                Log.d(TAG, "Force syncing user data for: ${currentUser.uid}")
                
                // Force reload user from Firebase Auth
                currentUser.reload().await()
                Log.d(TAG, "User reloaded successfully")
                
                // Refresh auth token
                val tokenRefreshed = refreshAuthToken()
                if (tokenRefreshed) {
                    Log.d(TAG, "Force sync completed successfully")
                    true
                } else {
                    Log.w(TAG, "Force sync failed: token refresh failed")
                    false
                }
            } else {
                Log.w(TAG, "No current user to sync")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error force syncing user data", e)
            false
        }
    }
    
    suspend fun ensureDataPersistence(): Boolean {
        return try {
            Log.d(TAG, "Ensuring data persistence")
            
            // Force sync first
            val syncSuccess = forceSyncUserData()
            if (!syncSuccess) {
                Log.w(TAG, "Data persistence check failed: sync failed")
                return false
            }
            
            // Wait for any pending writes to complete
            firestore.waitForPendingWrites().await()
            Log.d(TAG, "All pending writes completed")
            
            // Verify user document exists and is accessible
            val userDoc = getUserDocument()
            if (userDoc?.exists() == true) {
                Log.d(TAG, "Data persistence verified successfully")
                true
            } else {
                Log.w(TAG, "Data persistence check failed: user document not found")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring data persistence", e)
            false
        }
    }
    
    // Storage operations moved to CloudinaryManager
    // These methods are kept for backward compatibility but now use Cloudinary
}