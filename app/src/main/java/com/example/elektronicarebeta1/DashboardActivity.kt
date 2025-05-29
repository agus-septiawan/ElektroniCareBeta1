package com.example.elektronicarebeta1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.elektronicarebeta1.firebase.FirebaseDataSeeder
import com.example.elektronicarebeta1.firebase.FirebaseManager
import com.example.elektronicarebeta1.utils.UserMigrationHelper
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var userNameText: TextView
    private val TAG = "DashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        lifecycleScope.launch {
            val currentUser = FirebaseManager.getCurrentUser()
            
            // DEBUG LOGGING
            Log.d(TAG, "Current user: $currentUser")
            Log.d(TAG, "User ID: ${FirebaseManager.getUserId()}")
            Log.d(TAG, "User email: ${currentUser?.email}")
            Log.d(TAG, "User name: ${currentUser?.fullName ?: "Unknown"}")
            
            if (currentUser == null) {
                Log.d(TAG, "No current user, redirecting to login")
                startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
                return@launch
            }
            
            // Run user migration for existing users
            try {
                UserMigrationHelper.migrateExistingUsers()
            } catch (e: Exception) {
                Log.e(TAG, "Error during user migration", e)
            }
        }

        userNameText = findViewById<TextView>(R.id.welcome_text)
        val notificationIcon = findViewById<ImageView>(R.id.notification_icon)

        // TEMPORARY DISABLE DATA SEEDING FOR DEBUGGING
        // lifecycleScope.launch {
        //     FirebaseDataSeeder.seedAllData(this@DashboardActivity)
        // }

        loadUserData()
        setupBottomNavigation()
        setupRepairCards()

        notificationIcon.setOnClickListener {
            Toast.makeText(this, "Notifications coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Check authentication and force sync data when returning to dashboard
        lifecycleScope.launch {
            val currentUser = FirebaseManager.getCurrentUser()
            if (currentUser == null) {
                Log.d(TAG, "No current user in onResume, redirecting to login")
                startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            
            // Force data sync first
            val forceSyncSuccess = FirebaseManager.forceDataSync()
            Log.d(TAG, "Dashboard force data sync completed: $forceSyncSuccess")
            
            // Force sync user data
            val syncSuccess = FirebaseManager.forceSyncUserData()
            Log.d(TAG, "Dashboard force sync user data completed: $syncSuccess")
            
            // Reload user data
            loadUserData()
        }
    }

    private fun loadUserData() {
        // TEMPORARY DISABLE FIRESTORE ACCESS FOR DEBUGGING
        lifecycleScope.launch {
            val currentUser = FirebaseManager.getCurrentUser()
            val email = currentUser?.email ?: "User"
            val firstName = email.split("@").firstOrNull()?.split(".")?.firstOrNull() ?: "User"
            runOnUiThread {
                userNameText.text = "Welcome back, $firstName!"
            }
        }
        
        // lifecycleScope.launch {
        //     try {
        //         val userDoc = FirebaseManager.getUserData()
        //         
        //         if (userDoc != null && userDoc.exists()) {
        //             val fullName = userDoc.getString("fullName") ?: "User"
        //             val firstName = fullName.split(" ").firstOrNull() ?: fullName
        //             userNameText.text = "Welcome back, $firstName!"
        //         } else {
        //             userNameText.text = "Welcome back!"
        //         }
        //     } catch (e: Exception) {
        //         Log.e(TAG, "Error loading user data: ${e.message}")
        //         userNameText.text = "Welcome back!"
        //     }
        // }
    }

    private fun setupBottomNavigation() {
        val homeNav = findViewById<View>(R.id.nav_home)
        val historyNav = findViewById<View>(R.id.nav_history)
        val servicesNav = findViewById<View>(R.id.nav_services)
        val profileNav = findViewById<View>(R.id.nav_profile)

        historyNav.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        servicesNav.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        profileNav.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun setupRepairCards() {
        val viewAllRecent = findViewById<View>(R.id.view_all_recent)
        val repairCard1 = findViewById<View>(R.id.repair_card_1)
        val repairCard2 = findViewById<View>(R.id.repair_card_2)

        viewAllRecent.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        repairCard1.setOnClickListener {
            Toast.makeText(this, "Repair details coming soon", Toast.LENGTH_SHORT).show()
        }

        repairCard2.setOnClickListener {
            Toast.makeText(this, "Repair details coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
