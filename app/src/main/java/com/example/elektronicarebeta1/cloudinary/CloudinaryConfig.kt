package com.example.elektronicarebeta1.cloudinary

/**
 * Cloudinary configuration constants
 * 
 * IMPORTANT: Replace these values with your actual Cloudinary credentials
 * You can find these in your Cloudinary Dashboard at https://cloudinary.com/console
 */
object CloudinaryConfig {
    
    // TODO: Replace with your actual Cloudinary credentials
    const val CLOUD_NAME = "dcnsxpyal"
    const val API_KEY = "493695264712621"
    const val API_SECRET = "8QeRSl6aCwIwacw8NvacwMOiRpk"
    
    // Upload presets (these need to be created in Cloudinary Dashboard)
    const val PROFILE_UPLOAD_PRESET = "profile_images"
    const val REPAIR_UPLOAD_PRESET = "repair_images"
    
    // Image transformation settings
    const val PROFILE_IMAGE_WIDTH = 400
    const val PROFILE_IMAGE_HEIGHT = 400
    const val THUMBNAIL_WIDTH = 150
    const val THUMBNAIL_HEIGHT = 150
    
    /**
     * Check if Cloudinary is properly configured
     */
    fun isConfigured(): Boolean {
        return CLOUD_NAME != "your_cloud_name" && 
               API_KEY != "your_api_key" && 
               API_SECRET != "your_api_secret"
    }
    
    /**
     * Get configuration map for MediaManager initialization
     */
    fun getConfigMap(): Map<String, String> {
        return mapOf(
            "cloud_name" to CLOUD_NAME,
            "api_key" to API_KEY,
            "api_secret" to API_SECRET
        )
    }
}