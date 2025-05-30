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

object CloudinaryManager {
    private const val TAG = "CloudinaryManager"
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "Cloudinary already initialized")
            return
        }

        try {
            val configMap = CloudinaryConfig.getConfigMap(context)
            Log.d(TAG, "Initializing Cloudinary with cloud_name: ${configMap["cloud_name"]}")

            // Check if configuration is valid
            if (!CloudinaryConfig.isConfigured(context)) {
                Log.w(TAG, "Cloudinary not properly configured, using default values")
            }

            MediaManager.init(context, configMap)
            isInitialized = true
            Log.d(TAG, "Cloudinary initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Cloudinary", e)
            isInitialized = false
        }
    }

    suspend fun uploadProfileImage(context: Context, imageUri: Uri, userId: String): String? {
        Log.d(TAG, "uploadProfileImage called with imageUri: $imageUri, userId: $userId")

        // Check if Cloudinary is configured
        if (!CloudinaryConfig.isConfigured(context)) {
            Log.w(TAG, "Cloudinary not configured, returning null")
            return null
        }

        // Initialize if not already done
        if (!isInitialized) {
            initialize(context)
        }

        if (!isInitialized) {
            Log.e(TAG, "Failed to initialize Cloudinary for profile image upload")
            return null
        }

        return uploadImage(imageUri, CloudinaryConfig.PROFILE_UPLOAD_PRESET, "profile_$userId")
    }

    suspend fun uploadRepairImage(context: Context, imageUri: Uri, userId: String, repairId: String? = null): String? {
        Log.d(TAG, "uploadRepairImage called with imageUri: $imageUri, userId: $userId, repairId: $repairId")

        // Check if Cloudinary is configured
        if (!CloudinaryConfig.isConfigured(context)) {
            Log.w(TAG, "Cloudinary not configured, returning null")
            return null
        }

        // Initialize if not already done
        if (!isInitialized) {
            initialize(context)
        }

        if (!isInitialized) {
            Log.e(TAG, "Failed to initialize Cloudinary for repair image upload")
            return null
        }

        val publicId = if (repairId != null) "repair_${userId}_$repairId" else "repair_${userId}_${UUID.randomUUID()}"
        return uploadImage(imageUri, CloudinaryConfig.REPAIR_UPLOAD_PRESET, publicId)
    }

    private suspend fun uploadImage(imageUri: Uri, uploadPreset: String, publicId: String): String? {
        Log.d(TAG, "uploadImage called with imageUri: $imageUri, uploadPreset: $uploadPreset, publicId: $publicId")

        if (!isInitialized) {
            Log.e(TAG, "Cloudinary not initialized")
            return null
        }

        return suspendCancellableCoroutine<String?> { continuation ->
            try {
                Log.d(TAG, "Starting Cloudinary image upload for publicId: $publicId")

                val requestId = MediaManager.get().upload(imageUri)
                    .unsigned(uploadPreset)
                    .option("public_id", publicId)
                    .option("resource_type", "image")
                    .option("quality", "auto")
                    .option("fetch_format", "auto")
                    .option("overwrite", true) // Allow overwriting existing images
                    .option("timeout", 60) // 60 seconds timeout
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d(TAG, "Upload started with requestId: $requestId")
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100 / totalBytes).toInt()
                            Log.d(TAG, "Upload progress: $progress%")
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            Log.d(TAG, "Upload successful. Full response: $resultData")
                            val secureUrl = resultData["secure_url"] as? String
                            val url = resultData["url"] as? String

                            val finalUrl = secureUrl ?: url
                            if (finalUrl != null) {
                                Log.d(TAG, "Upload successful. URL: $finalUrl")
                                continuation.resume(finalUrl)
                            } else {
                                Log.e(TAG, "Upload successful but no URL in response")
                                Log.d(TAG, "Response data keys: ${resultData.keys}")
                                continuation.resume(null)
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            Log.e(TAG, "Upload failed. Error code: ${error.code}, description: ${error.description}")
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
    }

    fun isConfigured(context: Context? = null): Boolean {
        return CloudinaryConfig.isConfigured(context) && isInitialized
    }

    fun getOptimizedImageUrl(
        imageUrl: String,
        width: Int = 400,
        height: Int = 400,
        crop: String = "fill"
    ): String {
        if (!imageUrl.contains("cloudinary.com")) {
            return imageUrl
        }

        val parts = imageUrl.split("/")
        val uploadIndex = parts.indexOf("upload")
        if (uploadIndex == -1 || uploadIndex + 1 >= parts.size) {
            return imageUrl
        }

        val publicIdWithExtension = parts.subList(uploadIndex + 1, parts.size).joinToString("/")
        val baseUrl = parts.subList(0, uploadIndex + 1).joinToString("/")
        return "$baseUrl/w_$width,h_$height,c_$crop,q_auto,f_auto/$publicIdWithExtension"
    }
}
