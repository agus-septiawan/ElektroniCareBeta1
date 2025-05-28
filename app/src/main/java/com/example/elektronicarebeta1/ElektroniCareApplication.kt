package com.example.elektronicarebeta1

import android.app.Application
import android.util.Log
import com.example.elektronicarebeta1.cloudinary.CloudinaryManager

/**
 * Application class for ElektroniCare
 * Handles global initialization
 */
class ElektroniCareApplication : Application() {
    
    companion object {
        private const val TAG = "ElektroniCareApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Application starting...")
        
        // Initialize Cloudinary
        initializeCloudinary()
    }
    
    private fun initializeCloudinary() {
        try {
            CloudinaryManager.initialize(this)
            Log.d(TAG, "Cloudinary initialization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Cloudinary", e)
        }
    }
}