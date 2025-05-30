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
                Log.d(TAG, "Creating new user document with createdAt: ${dataWithTimestamp["createdAt"]}")
            } else {
                // Existing user - preserve createdAt, add updatedAt
                val existingCreatedAt = userDoc.getDate("createdAt") ?: userDoc.getTimestamp("createdAt")?.toDate()
                if (existingCreatedAt != null) {
                    dataWithTimestamp["createdAt"] = existingCreatedAt
                    Log.d(TAG, "Preserving existing createdAt: $existingCreatedAt")
                } else {
                    // If somehow createdAt is missing, add it now
                    dataWithTimestamp["createdAt"] = Date()
                    Log.w(TAG, "Missing createdAt, adding current date")
                }
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
            Log.d(TAG, "=== FIREBASE MANAGER: getUserRepairs ===")
            Log.d(TAG, "🔍 Getting repairs for userId: $userId")
            Log.d(TAG, "📂 Collection: $REPAIRS_COLLECTION")

            // Check if Firebase is initialized
            try {
                // Test if db is accessible
                db.app
                Log.d(TAG, "✅ Firestore database is initialized")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Firestore database not initialized: ${e.message}")
                return null
            }

            // First try with orderBy createdAt
            var querySnapshot = try {
                Log.d(TAG, "🔍 Attempting query with orderBy createdAt...")
                val query = db.collection(REPAIRS_COLLECTION)
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)

                Log.d(TAG, "📝 Query built successfully, executing...")
                val result = query.get().await()
                Log.d(TAG, "✅ Query with orderBy successful: ${result.size()} documents")
                result
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Failed to query with orderBy createdAt: ${e.message}", e)
                Log.d(TAG, "🔄 Trying without orderBy...")

                // If orderBy fails (missing index), try without orderBy
                val query = db.collection(REPAIRS_COLLECTION)
                    .whereEqualTo("userId", userId)

                Log.d(TAG, "📝 Simple query built, executing...")
                val result = query.get().await()
                Log.d(TAG, "✅ Query without orderBy successful: ${result.size()} documents")
                result
            }

            Log.d(TAG, "📊 FINAL RESULT: userId=$userId, count=${querySnapshot.size()}")

            // Log each document for debugging
            if (querySnapshot.isEmpty) {
                Log.w(TAG, "⚠️ No documents found for user $userId")

                // Let's also check if there are ANY documents in the collection
                try {
                    val allDocs = db.collection(REPAIRS_COLLECTION).limit(5).get().await()
                    Log.d(TAG, "🔍 Total documents in repairs collection (sample): ${allDocs.size()}")
                    allDocs.documents.forEach { doc ->
                        val docUserId = doc.getString("userId")
                        Log.d(TAG, "📄 Sample doc ${doc.id}: userId=$docUserId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to check collection contents", e)
                }
            } else {
                Log.d(TAG, "📝 Found ${querySnapshot.size()} documents:")
                querySnapshot.documents.forEachIndexed { index, doc ->
                    val docUserId = doc.getString("userId")
                    val deviceModel = doc.getString("deviceModel")
                    val status = doc.getString("status")
                    Log.d(TAG, "  ${index + 1}. ${doc.id}: $deviceModel ($status) - userId: $docUserId")
                }
            }

            querySnapshot
        } catch (e: Exception) {
            Log.e(TAG, "💥 CRITICAL ERROR getting user repairs", e)
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Error message: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun getRepairById(repairId: String): com.example.elektronicarebeta1.models.Repair? {
        return try {
            val document = db.collection(REPAIRS_COLLECTION).document(repairId).get().await()
            if (document.exists()) {
                com.example.elektronicarebeta1.models.Repair.fromDocument(document)
            } else {
                Log.w(TAG, "Repair document not found for ID: $repairId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting repair by id", e)
            null
        }
    }

    suspend fun getRepairDocumentById(repairId: String): DocumentSnapshot? {
        return try {
            db.collection(REPAIRS_COLLECTION).document(repairId).get().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting repair document by id", e)
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

    suspend fun updateRepairStatus(repairId: String, status: String): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to Date()
            )

            // Add specific timestamp for certain statuses
            when (status) {
                "cancelled" -> updates["cancelledAt"] = Date()
                "completed" -> updates["completedDate"] = Date()
                "in_progress" -> updates["startedAt"] = Date()
            }

            db.collection(REPAIRS_COLLECTION).document(repairId).update(updates).await()
            Log.d(TAG, "Repair status updated successfully: $repairId -> $status")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating repair status", e)
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
