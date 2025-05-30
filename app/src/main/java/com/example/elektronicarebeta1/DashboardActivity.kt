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
import com.example.elektronicarebeta1.firebase.FirebaseManager
import com.example.elektronicarebeta1.firebase.FirebaseDataSeeder
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var userNameText: TextView
    private val TAG = "DashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Force check authentication first
        lifecycleScope.launch {
            if (!FirebaseManager.isUserAuthenticated()) {
                Log.d(TAG, "No current user, redirecting to login")
                redirectToLogin()
                return@launch
            }

            // Seed data if needed
            FirebaseDataSeeder.seedAllData(this@DashboardActivity)
        }

        userNameText = findViewById<TextView>(R.id.welcome_text)
        val notificationIcon = findViewById<ImageView>(R.id.notification_icon)

        loadUserData()
        loadRecentRepairs()
        setupBottomNavigation()
        setupRepairCards()

        notificationIcon.setOnClickListener {
            Toast.makeText(this, "Notifications coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Always force refresh data when returning to dashboard
        lifecycleScope.launch {
            if (!FirebaseManager.isUserAuthenticated()) {
                Log.d(TAG, "No current user in onResume, redirecting to login")
                redirectToLogin()
                return@launch
            }

            // Force refresh all data
            loadUserData()
            loadRecentRepairs()
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading user data from Firestore...")

                val userDoc = FirebaseManager.getUserData()
                if (userDoc != null && userDoc.exists()) {
                    Log.d(TAG, "User document found, loading data...")
                    val fullName = userDoc.getString("fullName") ?: "User"
                    val firstName = fullName.split(" ").firstOrNull() ?: fullName

                    runOnUiThread {
                        userNameText.text = "Welcome back, $firstName!"
                    }

                    Log.d(TAG, "User data loaded successfully: $firstName")
                } else {
                    Log.w(TAG, "User document not found, using fallback")
                    // Fallback to email-based name if Firestore data not available
                    val currentFirebaseUser = FirebaseManager.getCurrentFirebaseUser()
                    val email = currentFirebaseUser?.email ?: "User"
                    val firstName = email.split("@").firstOrNull()?.split(".")?.firstOrNull() ?: "User"

                    runOnUiThread {
                        userNameText.text = "Welcome back, $firstName!"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user data: ${e.message}")
                runOnUiThread {
                    userNameText.text = "Welcome back!"
                }
            }
        }
    }

    private fun loadRecentRepairs() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading recent repairs from Firestore...")

                val repairsSnapshot = FirebaseManager.getUserRepairs()
                if (repairsSnapshot != null && !repairsSnapshot.isEmpty) {
                    Log.d(TAG, "Found ${repairsSnapshot.size()} repairs")

                    // Get the most recent repairs (limit to 2 for dashboard)
                    val recentRepairs = repairsSnapshot.documents.take(2)

                    runOnUiThread {
                        updateRepairCards(recentRepairs)
                    }
                } else {
                    Log.d(TAG, "No repairs found for user")
                    runOnUiThread {
                        // Hide repair cards or show empty state
                        hideRepairCards()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recent repairs: ${e.message}")
            }
        }
    }

    private fun updateRepairCards(repairs: List<com.google.firebase.firestore.DocumentSnapshot>) {
        val repairCard1 = findViewById<View>(R.id.repair_card_1)
        val repairCard2 = findViewById<View>(R.id.repair_card_2)

        // Update first repair card if available
        if (repairs.isNotEmpty()) {
            val repair1 = repairs[0]
            repairCard1.visibility = View.VISIBLE

            // Update repair card 1 with data
            val deviceType1 = repair1.getString("deviceType") ?: "Unknown Device"
            val status1 = repair1.getString("status") ?: "Unknown Status"

            // Find TextViews in repair card and update them
            val deviceText1 = repairCard1.findViewById<TextView>(R.id.device_type_text)
            val statusText1 = repairCard1.findViewById<TextView>(R.id.status_text)

            deviceText1?.text = deviceType1
            statusText1?.text = status1.replaceFirstChar { it.uppercase() }

            Log.d(TAG, "Updated repair card 1: $deviceType1 - $status1")
        } else {
            repairCard1.visibility = View.GONE
        }

        // Update second repair card if available
        if (repairs.size > 1) {
            val repair2 = repairs[1]
            repairCard2.visibility = View.VISIBLE

            // Update repair card 2 with data
            val deviceType2 = repair2.getString("deviceType") ?: "Unknown Device"
            val status2 = repair2.getString("status") ?: "Unknown Status"

            // Find TextViews in repair card and update them
            val deviceText2 = repairCard2.findViewById<TextView>(R.id.device_type_text)
            val statusText2 = repairCard2.findViewById<TextView>(R.id.status_text)

            deviceText2?.text = deviceType2
            statusText2?.text = status2.replaceFirstChar { it.uppercase() }

            Log.d(TAG, "Updated repair card 2: $deviceType2 - $status2")
        } else {
            repairCard2.visibility = View.GONE
        }
    }

    private fun hideRepairCards() {
        val repairCard1 = findViewById<View>(R.id.repair_card_1)
        val repairCard2 = findViewById<View>(R.id.repair_card_2)

        repairCard1.visibility = View.GONE
        repairCard2.visibility = View.GONE

        Log.d(TAG, "Repair cards hidden - no repairs found")
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

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
