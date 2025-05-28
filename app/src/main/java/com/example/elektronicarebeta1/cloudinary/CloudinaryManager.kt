package com.example.elektronicarebeta1.cloudinary

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Singleton class to manage Cloudinary operations for image uploads
 */
object CloudinaryManager {
    private const val TAG = "CloudinaryManager"
    
    // Use configuration from CloudinaryConfig
    
    private var isInitialized = false
    
    /**
     * Initialize Cloudinary MediaManager
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            MediaManager.init(context, CloudinaryConfig.getConfigMap())
            isInitialized = true
            Log.d(TAG, "Cloudinary initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Cloudinary", e)
        }
    }
    
    /**
     * Upload profile image to Cloudinary
     * @param imageUri URI of the image to upload
     * @param userId User ID for organizing uploads
     * @return URL of uploaded image or null if failed
     */
    suspend fun uploadProfileImage(imageUri: Uri, userId: String): String? {
        return uploadImage(imageUri, CloudinaryConfig.PROFILE_UPLOAD_PRESET, "profile_$userId")
    }
    
    /**
     * Upload repair image to Cloudinary
     * @param imageUri URI of the image to upload
     * @param userId User ID for organizing uploads
     * @param repairId Repair ID for organizing uploads
     * @return URL of uploaded image or null if failed
     */
    suspend fun uploadRepairImage(imageUri: Uri, userId: String, repairId: String? = null): String? {
        val publicId = if (repairId != null) "repair_${userId}_$repairId" else "repair_${userId}_${UUID.randomUUID()}"
        return uploadImage(imageUri, CloudinaryConfig.REPAIR_UPLOAD_PRESET, publicId)
    }
    
    /**
     * Generic image upload function
     * @param imageUri URI of the image to upload
     * @param uploadPreset Cloudinary upload preset to use
     * @param publicId Public ID for the uploaded image
     * @return URL of uploaded image or null if failed
     */
    private suspend fun uploadImage(imageUri: Uri, uploadPreset: String, publicId: String): String? {
        if (!isInitialized) {
            Log.e(TAG, "Cloudinary not initialized")
            return null
        }
        
        return suspendCancellableCoroutine { continuation ->
            try {
                Log.d(TAG, "Starting image upload with publicId: $publicId")
                
                val requestId = MediaManager.get().upload(imageUri)
                    .unsigned(uploadPreset)
                    .option("public_id", publicId)
                    .option("resource_type", "image")
                    .option("quality", "auto")
                    .option("fetch_format", "auto")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d(TAG, "Upload started with requestId: $requestId")
                        }
                        
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100 / totalBytes).toInt()
                            Log.d(TAG, "Upload progress: $progress%")
                        }
                        
                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            Log.d(TAG, "Upload successful")
                            val secureUrl = resultData["secure_url"] as? String
                            if (secureUrl != null) {
                                Log.d(TAG, "Image uploaded successfully: $secureUrl")
                                continuation.resume(secureUrl)
                            } else {
                                Log.e(TAG, "No secure_url in response")
                                continuation.resume(null)
                            }
                        }
                        
                        override fun onError(requestId: String, error: ErrorInfo) {
                            Log.e(TAG, "Upload failed: ${error.description}")
                            continuation.resume(null)
                        }
                        
                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            Log.w(TAG, "Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch()
                
                // Handle cancellation
                continuation.invokeOnCancellation {
                    try {
                        MediaManager.get().cancelRequest(requestId)
                        Log.d(TAG, "Upload cancelled")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error cancelling upload", e)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error starting upload", e)
                continuation.resume(null)
            }
        }
    }
    
    /**
     * Delete image from Cloudinary
     * @param publicId Public ID of the image to delete
     * @return true if successful, false otherwise
     */
    suspend fun deleteImage(publicId: String): Boolean {
        // TODO: Implement proper delete functionality with Cloudinary Admin API
        // The destroy method is not available in the current SDK version
        Log.w(TAG, "Delete functionality not implemented yet for: $publicId")
        return false
    }
    
    /**
     * Get optimized image URL with transformations
     * @param imageUrl Original Cloudinary URL
     * @param width Desired width
     * @param height Desired height
     * @param crop Crop mode (fill, fit, scale, etc.)
     * @return Optimized image URL
     */
    fun getOptimizedImageUrl(
        imageUrl: String,
        width: Int = 400,
        height: Int = 400,
        crop: String = "fill"
    ): String {
        if (!imageUrl.contains("cloudinary.com")) {
            return imageUrl // Not a Cloudinary URL
        }
        
        // Extract public ID from URL
        val parts = imageUrl.split("/")
        val uploadIndex = parts.indexOf("upload")
        if (uploadIndex == -1 || uploadIndex + 1 >= parts.size) {
            return imageUrl // Invalid Cloudinary URL
        }
        
        val publicIdWithExtension = parts.subList(uploadIndex + 1, parts.size).joinToString("/")
        val publicId = publicIdWithExtension.substringBeforeLast(".")
        
        // Build optimized URL
        val baseUrl = parts.subList(0, uploadIndex + 1).joinToString("/")
        return "$baseUrl/w_$width,h_$height,c_$crop,q_auto,f_auto/$publicIdWithExtension"
    }
    
    /**
     * Check if Cloudinary is properly configured
     */
    fun isConfigured(): Boolean {
        return CloudinaryConfig.isConfigured() && isInitialized
    }
}