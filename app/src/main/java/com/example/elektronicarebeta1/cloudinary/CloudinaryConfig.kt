package com.example.elektronicarebeta1.cloudinary

import android.content.Context
import android.util.Log

/**
 * Cloudinary configuration constants
 * 
 * IMPORTANT: Update these values with your actual Cloudinary credentials
 * You can find these in your Cloudinary Dashboard at https://cloudinary.com/console
 */
object CloudinaryConfig {
    private const val TAG = "CloudinaryConfig"
    
    // Default Cloudinary credentials - UPDATE THESE WITH YOUR ACTUAL VALUES
    private const val DEFAULT_CLOUD_NAME = "dcnsxpyal"
    private const val DEFAULT_API_KEY = "493695264712621"
    private const val DEFAULT_API_SECRET = "8QeRSl6aCwIwacw8NvacwMOiRpk"
    
    // Upload presets (these need to be created in Cloudinary Dashboard)
    const val PROFILE_UPLOAD_PRESET = "profile_images"
    const val REPAIR_UPLOAD_PRESET = "repair_images"
    
    // Image transformation settings
    const val PROFILE_IMAGE_WIDTH = 400
    const val PROFILE_IMAGE_HEIGHT = 400
    const val THUMBNAIL_WIDTH = 150
    const val THUMBNAIL_HEIGHT = 150
    
    /**
     * Get cloud name - can be overridden by user configuration
     */
    fun getCloudName(context: Context? = null): String {
        // Try to read from SharedPreferences first
        context?.let {
            val prefs = it.getSharedPreferences("cloudinary_config", Context.MODE_PRIVATE)
            val cloudName = prefs.getString("cloud_name", null)
            if (!cloudName.isNullOrBlank()) {
                Log.d(TAG, "Using cloud name from preferences: $cloudName")
                return cloudName
            }
        }
        
        Log.d(TAG, "Using default cloud name: $DEFAULT_CLOUD_NAME")
        return DEFAULT_CLOUD_NAME
    }
    
    /**
     * Get API key - can be overridden by user configuration
     */
    fun getApiKey(context: Context? = null): String {
        context?.let {
            val prefs = it.getSharedPreferences("cloudinary_config", Context.MODE_PRIVATE)
            val apiKey = prefs.getString("api_key", null)
            if (!apiKey.isNullOrBlank()) {
                Log.d(TAG, "Using API key from preferences")
                return apiKey
            }
        }
        
        Log.d(TAG, "Using default API key")
        return DEFAULT_API_KEY
    }
    
    /**
     * Get API secret - can be overridden by user configuration
     */
    fun getApiSecret(context: Context? = null): String {
        context?.let {
            val prefs = it.getSharedPreferences("cloudinary_config", Context.MODE_PRIVATE)
            val apiSecret = prefs.getString("api_secret", null)
            if (!apiSecret.isNullOrBlank()) {
                Log.d(TAG, "Using API secret from preferences")
                return apiSecret
            }
        }
        
        Log.d(TAG, "Using default API secret")
        return DEFAULT_API_SECRET
    }
    
    /**
     * Save Cloudinary credentials to SharedPreferences
     */
    fun saveCredentials(context: Context, cloudName: String, apiKey: String, apiSecret: String) {
        val prefs = context.getSharedPreferences("cloudinary_config", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("cloud_name", cloudName)
            putString("api_key", apiKey)
            putString("api_secret", apiSecret)
            apply()
        }
        Log.d(TAG, "Cloudinary credentials saved to preferences")
    }
    
    /**
     * Check if Cloudinary is properly configured
     */
    fun isConfigured(context: Context? = null): Boolean {
        val cloudName = getCloudName(context)
        val apiKey = getApiKey(context)
        val apiSecret = getApiSecret(context)
        
        val isConfigured = cloudName.isNotBlank() && 
                          apiKey.isNotBlank() && 
                          apiSecret.isNotBlank() &&
                          cloudName != "your_cloud_name" &&
                          apiKey != "your_api_key" &&
                          apiSecret != "your_api_secret"
        
        Log.d(TAG, "Cloudinary configuration check: $isConfigured")
        return isConfigured
    }
    
    /**
     * Get configuration map for MediaManager initialization
     */
    fun getConfigMap(context: Context? = null): Map<String, String> {
        return mapOf(
            "cloud_name" to getCloudName(context),
            "api_key" to getApiKey(context),
            "api_secret" to getApiSecret(context)
        )
    }
}