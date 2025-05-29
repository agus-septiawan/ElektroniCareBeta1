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
            val configMap = CloudinaryConfig.getConfigMap(context)
            Log.d(TAG, "Initializing Cloudinary with cloud_name: ${configMap["cloud_name"]}")
            MediaManager.init(context, configMap)
            isInitialized = true
            Log.d(TAG, "Cloudinary initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Cloudinary", e)
        }
    }

    /**
     * Upload profile image to Cloudinary
     * @param context Application context
     * @param imageUri URI of the image to upload
     * @param userId User ID for organizing uploads
     * @return URL of uploaded image or null if failed
     */
    suspend fun uploadProfileImage(context: Context, imageUri: Uri, userId: String): String? {
        Log.d(TAG, "uploadProfileImage called with imageUri: $imageUri, userId: $userId")
        // Check if Cloudinary is properly configured
        val isConfigured = CloudinaryConfig.isConfigured(context)
        Log.d(TAG, "CloudinaryConfig.isConfigured(context) result: $isConfigured")
        if (!isConfigured) {
            Log.w(TAG, "Cloudinary not configured, returning placeholder image URL")
            val placeholderUrl = generatePlaceholderImageUrl(userId)
            Log.d(TAG, "Generated placeholder URL: $placeholderUrl")
            return placeholderUrl
        }

        // Initialize if not already done
        if (!isInitialized) {
            initialize(context)
        }

        val resultUrl = uploadImage(imageUri, CloudinaryConfig.PROFILE_UPLOAD_PRESET, "profile_$userId")
        Log.d(TAG, "uploadProfileImage returning URL: $resultUrl")
        return resultUrl
    }

    /**
     * Upload repair image to Cloudinary
     * @param context Application context
     * @param imageUri URI of the image to upload
     * @param userId User ID for organizing uploads
     * @param repairId Repair ID for organizing uploads
     * @return URL of uploaded image or null if failed
     */
    suspend fun uploadRepairImage(context: Context, imageUri: Uri, userId: String, repairId: String? = null): String? {
        // Check if Cloudinary is properly configured
        if (!CloudinaryConfig.isConfigured(context)) {
            Log.w(TAG, "Cloudinary not configured, using placeholder image")
            return generatePlaceholderImageUrl("repair_$userId")
        }

        // Initialize if not already done
        if (!isInitialized) {
            initialize(context)
        }

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
        Log.d(TAG, "uploadImage called with imageUri: $imageUri, uploadPreset: $uploadPreset, publicId: $publicId")
        if (!isInitialized) {
            Log.e(TAG, "Cloudinary not initialized, cannot upload image.")
            return null
        }

        val result = suspendCancellableCoroutine<String?> { continuation ->
            try {
                Log.d(TAG, "Starting Cloudinary image upload for publicId: $publicId, preset: $uploadPreset")

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
                            val secureUrl = resultData["secure_url"] as? String
                            if (secureUrl != null) {
                                Log.d(TAG, "Upload successful for publicId: $publicId. Secure URL: $secureUrl")
                                continuation.resume(secureUrl)
                            } else {
                                Log.e(TAG, "Upload successful for publicId: $publicId but no secure_url in response: $resultData")
                                continuation.resume(null)
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            Log.e(TAG, "Upload failed for publicId: $publicId. Error: ${error.description}")
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
                Log.e(TAG, "Error starting upload for publicId: $publicId", e)
                continuation.resume(null)
            }
        }
        Log.d(TAG, "uploadImage for publicId: $publicId returning URL: $result")
        return result
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
     * Generate placeholder image URL for demo purposes
     */
    private fun generatePlaceholderImageUrl(identifier: String): String {
        // Generate a unique placeholder URL based on identifier
        val hash = identifier.hashCode().toString().replace("-", "")
        return "https://via.placeholder.com/400x400/4CAF50/FFFFFF?text=${hash.take(6)}"
    }

    /**
     * Check if Cloudinary is properly configured
     */
    fun isConfigured(context: Context? = null): Boolean {
        return CloudinaryConfig.isConfigured(context) && isInitialized
    }
}
