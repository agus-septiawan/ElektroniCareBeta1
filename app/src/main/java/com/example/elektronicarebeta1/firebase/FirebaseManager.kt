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
            db.collection(USERS_COLLECTION).document(userId).update(userData).await()
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
        repairWithUser.putIfAbsent("status", "pending") // Default status if not in repairData
        repairWithUser["userId"] = userId // Current user's ID always takes precedence
        
        return try {
            val docRef = db.collection(REPAIRS_COLLECTION).add(repairWithUser).await()
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
    
    // Storage operations moved to CloudinaryManager
    // These methods are kept for backward compatibility but now use Cloudinary
}