package com.example.elektronicarebeta1.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Helper class to migrate existing users to include createdAt field
 */
object UserMigrationHelper {
    private const val TAG = "UserMigrationHelper"
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Add createdAt field to existing users who don't have it
     * This should be called once during app initialization
     */
    suspend fun migrateExistingUsers() {
        try {
            Log.d(TAG, "Starting user migration...")
            
            val usersCollection = db.collection("users")
            val querySnapshot = usersCollection.get().await()
            
            var migratedCount = 0
            
            for (document in querySnapshot.documents) {
                val data = document.data
                if (data != null && !data.containsKey("createdAt")) {
                    // Add createdAt field with current timestamp
                    // In real scenario, you might want to use account creation date if available
                    val updates = mapOf("createdAt" to Date())
                    
                    document.reference.update(updates).await()
                    migratedCount++
                    
                    Log.d(TAG, "Added createdAt to user: ${document.id}")
                }
            }
            
            Log.d(TAG, "Migration completed. Updated $migratedCount users.")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during user migration", e)
        }
    }
    
    /**
     * Check if a specific user has createdAt field
     */
    suspend fun checkUserHasCreatedAt(userId: String): Boolean {
        return try {
            val document = db.collection("users").document(userId).get().await()
            document.data?.containsKey("createdAt") ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user createdAt field", e)
            false
        }
    }
}