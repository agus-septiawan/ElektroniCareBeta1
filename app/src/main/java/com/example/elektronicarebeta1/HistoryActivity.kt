package com.example.elektronicarebeta1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.example.elektronicarebeta1.firebase.FirebaseManager
import com.example.elektronicarebeta1.models.Repair
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var repairsContainer: LinearLayout
    private lateinit var noRepairsView: View
    private lateinit var statusFilterChips: ChipGroup
    private var allRepairs: List<Repair> = emptyList()
    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize views
        repairsContainer = findViewById(R.id.repairs_container)
        noRepairsView = findViewById(R.id.no_repairs_view)
        statusFilterChips = findViewById(R.id.status_filter_chips)

        // Setup filter chips
        setupFilterChips()

        // Set up back button
        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        // Add debug refresh button (temporary)
        try {
            val refreshButton = findViewById<ImageView>(R.id.refresh_button)
            refreshButton?.setOnClickListener {
                forceRefresh()
            }
            Log.d("HistoryActivity", "✅ Refresh button found and configured")
        } catch (e: Exception) {
            Log.w("HistoryActivity", "⚠️ Refresh button not found: ${e.message}")
        }

        // Alternative: Long press on title to refresh (backup method)
        try {
            val titleText = findViewById<TextView>(R.id.title_text)
            titleText?.setOnLongClickListener {
                Toast.makeText(this, "🔄 Force refreshing...", Toast.LENGTH_SHORT).show()
                forceRefresh()
                true
            }
        } catch (e: Exception) {
            Log.w("HistoryActivity", "⚠️ Title text not found for long press: ${e.message}")
        }

        // Set up bottom navigation
        setupBottomNavigation()

        // Debug authentication
        debugAuthentication()

        // Load repair history
        loadRepairHistory()
    }

    override fun onResume() {
        super.onResume()
        // Check if user is still authenticated
        if (!FirebaseManager.isUserAuthenticated()) {
            Log.w("HistoryActivity", "User not authenticated, redirecting to login")
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }
        // Refresh data when returning to this activity
        lifecycleScope.launch {
            loadRepairHistory()
        }
    }

    private fun setupBottomNavigation() {
        val homeNav = findViewById<View>(R.id.nav_home)
        val historyNav = findViewById<View>(R.id.nav_history)
        val servicesNav = findViewById<View>(R.id.nav_services)
        val profileNav = findViewById<View>(R.id.nav_profile)

        homeNav.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        servicesNav.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        profileNav.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun setupFilterChips() {
        val filters = listOf(
            "all" to "All",
            "pending" to "Pending",
            "pending_confirmation" to "Pending Confirmation",
            "in_progress" to "In Progress",
            "completed" to "Completed",
            "cancelled" to "Cancelled"
        )

        filters.forEach { (value, label) ->
            val chip = Chip(this).apply {
                text = label
                isCheckable = true
                isChecked = value == "all"
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        currentFilter = value
                        filterRepairs()
                        // Uncheck other chips
                        for (i in 0 until statusFilterChips.childCount) {
                            val otherChip = statusFilterChips.getChildAt(i) as Chip
                            if (otherChip != this) {
                                otherChip.isChecked = false
                            }
                        }
                    }
                }
            }
            statusFilterChips.addView(chip)
        }
    }

    private fun filterRepairs() {
        val filteredRepairs = if (currentFilter == "all") {
            allRepairs
        } else {
            allRepairs.filter { it.status == currentFilter }
        }

        displayRepairs(filteredRepairs)
    }

    private fun displayRepairs(repairs: List<Repair>) {
        repairsContainer.removeAllViews()

        if (repairs.isEmpty()) {
            noRepairsView.visibility = View.VISIBLE
            repairsContainer.visibility = View.GONE
        } else {
            noRepairsView.visibility = View.GONE
            repairsContainer.visibility = View.VISIBLE

            repairs.forEach { repair ->
                addRepairToView(repair)
            }
        }
    }

    private fun loadRepairHistory() {
        lifecycleScope.launch {
            try {
                Log.d("HistoryActivity", "=== LOADING REPAIR HISTORY ===")

                // Check if user is authenticated
                val userId = FirebaseManager.getUserId()
                if (userId == null) {
                    Log.e("HistoryActivity", "❌ User not authenticated")
                    runOnUiThread {
                        Toast.makeText(this@HistoryActivity, "User not authenticated", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                Log.d("HistoryActivity", "✅ User authenticated - ID: $userId")

                // Check if user is still authenticated with Firebase
                val currentUser = FirebaseManager.getCurrentFirebaseUser()
                if (currentUser == null) {
                    Log.e("HistoryActivity", "❌ Firebase user is null")
                    runOnUiThread {
                        Toast.makeText(this@HistoryActivity, "Firebase authentication failed", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                Log.d("HistoryActivity", "✅ Firebase user: ${currentUser.email}")

                Log.d("HistoryActivity", "🔍 Calling FirebaseManager.getUserRepairs()...")
                var repairsSnapshot = FirebaseManager.getUserRepairs()

                if (repairsSnapshot == null) {
                    Log.e("HistoryActivity", "❌ repairsSnapshot is NULL on first attempt")
                } else if (repairsSnapshot.isEmpty) {
                    Log.w("HistoryActivity", "⚠️ repairsSnapshot is EMPTY on first attempt (${repairsSnapshot.size()} documents)")
                } else {
                    Log.d("HistoryActivity", "✅ repairsSnapshot SUCCESS on first attempt (${repairsSnapshot.size()} documents)")
                }

                if (repairsSnapshot == null || repairsSnapshot.isEmpty) {
                    Log.d("HistoryActivity", "🔄 Retrying after 2 seconds...")

                    // Add delay before retry
                    kotlinx.coroutines.delay(2000)

                    Log.d("HistoryActivity", "🔍 Second attempt - calling FirebaseManager.getUserRepairs()...")
                    repairsSnapshot = FirebaseManager.getUserRepairs()

                    if (repairsSnapshot != null && !repairsSnapshot.isEmpty) {
                        Log.d("HistoryActivity", "✅ Retry successful, found ${repairsSnapshot.size()} repairs")
                    } else {
                        Log.e("HistoryActivity", "❌ Retry also failed - repairsSnapshot: $repairsSnapshot")
                        if (repairsSnapshot != null) {
                            Log.e("HistoryActivity", "❌ Snapshot exists but empty: ${repairsSnapshot.size()} documents")
                        }
                    }
                }

                // Process the snapshot if it's valid, otherwise handle empty state
                if (repairsSnapshot != null && !repairsSnapshot.isEmpty) {
                    Log.d("HistoryActivity", "📝 Processing ${repairsSnapshot.size()} repairs...")
                    processRepairSnapshot(repairsSnapshot)
                } else {
                    Log.e("HistoryActivity", "❌ No repairs found after all attempts - displaying empty state")
                    runOnUiThread {
                        allRepairs = emptyList()
                        displayRepairs(allRepairs)
                        Toast.makeText(this@HistoryActivity, "No repair history found. Try the migration tool to add sample data.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoryActivity", "💥 CRITICAL ERROR loading repair history", e)
                Log.e("HistoryActivity", "Error details: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@HistoryActivity, "Error loading repair history: ${e.message}", Toast.LENGTH_LONG).show()
                    allRepairs = emptyList()
                    displayRepairs(allRepairs)
                }
            }
        }
    }

    private fun processRepairSnapshot(repairsSnapshot: com.google.firebase.firestore.QuerySnapshot) {
        Log.d("HistoryActivity", "Processing ${repairsSnapshot.size()} repairs")
        val repairs = mutableListOf<Repair>()

        for (document in repairsSnapshot.documents) {
            Log.d("HistoryActivity", "Processing document: ${document.id}, data: ${document.data}")
            val repair = Repair.fromDocument(document)
            if (repair != null) {
                Log.d("HistoryActivity", "Successfully parsed repair: ${repair.deviceModel} - ${repair.status} - ${repair.createdAt}")
                repairs.add(repair)
            } else {
                Log.e("HistoryActivity", "Failed to parse repair from document: ${document.id}")
            }
        }

        Log.d("HistoryActivity", "Total repairs parsed: ${repairs.size}")

        runOnUiThread {
            // Sort by date (newest first) - use createdAt if appointmentTimestamp is null
            allRepairs = repairs.sortedByDescending { it.createdAt ?: it.appointmentTimestamp }
            Log.d("HistoryActivity", "Sorted repairs, displaying ${allRepairs.size} repairs")
            filterRepairs()
        }
    }

    private fun addRepairToView(repair: Repair) {
        val repairView = layoutInflater.inflate(R.layout.item_repair_history, repairsContainer, false)

        // Set repair details
        val deviceNameText = repairView.findViewById<TextView>(R.id.device_name)
        val serviceTypeText = repairView.findViewById<TextView>(R.id.service_type)
        val dateText = repairView.findViewById<TextView>(R.id.repair_date)
        val locationText = repairView.findViewById<TextView>(R.id.repair_location)
        val statusText = repairView.findViewById<TextView>(R.id.repair_status)
        val priceText = repairView.findViewById<TextView>(R.id.repair_price)
        val cancelButton = repairView.findViewById<Button>(R.id.cancel_button)

        deviceNameText.text = repair.deviceModel
        serviceTypeText.text = repair.issueDescription

        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val dateString = repair.appointmentTimestamp?.let { dateFormat.format(it) } ?: "Not scheduled"
        dateText.text = dateString

        locationText.text = repair.location ?: "Not specified"

        // Set status dengan styling yang lebih baik dan kontras tinggi
        when (repair.status) {
            "completed" -> {
                statusText.text = "✓ Completed"
                statusText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                statusText.setBackgroundResource(R.drawable.status_completed_bg)
                statusText.setPadding(
                    resources.getDimensionPixelSize(R.dimen.status_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.status_padding_vertical),
                    resources.getDimensionPixelSize(R.dimen.status_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.status_padding_vertical)
                )
            }
            "in_progress" -> {
                statusText.text = "⚡ In Progress"
                statusText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                statusText.setBackgroundResource(R.drawable.status_inprogress_bg)
                statusText.setPadding(
                    resources.getDimensionPixelSize(R.dimen.status_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.status_padding_vertical),
                    resources.getDimensionPixelSize(R.dimen.status_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.status_padding_vertical)
                )
            }
            "cancelled" -> {
                statusText.text = "✗ Cancelled"
                statusText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                statusText.setBackgroundResource(R.drawable.status_cancelled_bg)
                statusText.setPadding(
                    resources.getDimensionPixelSize(R.dimen.status_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.status_padding_vertical),
                    resources.getDimensionPixelSize(R.dimen.status_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.status_padding_vertical)
                )
            }
            "pending_confirmation" -> {
                statusText.text = "⏳ Pending Confirmation"
                statusText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                statusText.setBackgroundResource(R.drawable.status_inprogress_bg)
                statusText.setPadding(
                    resources.getDimensionPixelSize(R.dimen.status_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.status_padding_vertical),
                    resources.getDimensionPixelSize(R.dimen.status_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.status_padding_vertical)
                )
            }
            else -> {
                statusText.text = "⏰ Pending"
                statusText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                statusText.setBackgroundResource(R.drawable.status_pending_bg)
                statusText.setPadding(
                    resources.getDimensionPixelSize(R.dimen.status_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.status_padding_vertical),
                    resources.getDimensionPixelSize(R.dimen.status_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.status_padding_vertical)
                )
            }
        }

        // Set price
        val priceString = repair.estimatedCost?.let { "Rp${String.format("%,.0f", it)}" } ?: "TBD"
        priceText.text = priceString

        // Show cancel button only for pending, pending_confirmation, or in_progress repairs
        if (repair.status == "pending" || repair.status == "pending_confirmation" || repair.status == "in_progress") {
            cancelButton.visibility = View.VISIBLE
            cancelButton.setOnClickListener {
                cancelRepairRequest(repair)
            }
        } else {
            cancelButton.visibility = View.GONE
        }

        // Set click listener to open repair details
        repairView.setOnClickListener {
            val intent = Intent(this, RepairDetailsActivity::class.java).apply {
                putExtra(RepairDetailsActivity.EXTRA_REPAIR_ID, repair.id)
            }
            startActivity(intent)
        }

        repairsContainer.addView(repairView)
    }

    private fun cancelRepairRequest(repair: Repair) {
        lifecycleScope.launch {
            try {
                val success = FirebaseManager.cancelRepairRequest(repair.id)
                if (success) {
                    Toast.makeText(this@HistoryActivity, "Repair request cancelled successfully", Toast.LENGTH_SHORT).show()
                    // Refresh the repair history
                    loadRepairHistory()
                } else {
                    Toast.makeText(this@HistoryActivity, "Failed to cancel repair request", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("HistoryActivity", "Error cancelling repair request", e)
                Toast.makeText(this@HistoryActivity, "Error cancelling repair request", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun debugAuthentication() {
        Log.d("HistoryActivity", "=== DEBUG AUTHENTICATION ===")

        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            Log.d("HistoryActivity", "✅ Firebase User authenticated:")
            Log.d("HistoryActivity", "  UID: ${firebaseUser.uid}")
            Log.d("HistoryActivity", "  Email: ${firebaseUser.email}")
            Log.d("HistoryActivity", "  Display Name: ${firebaseUser.displayName}")
            Log.d("HistoryActivity", "  Is Anonymous: ${firebaseUser.isAnonymous}")

            // Check if this user ID exists in our test data
            val knownUsers = mapOf(
                "1rHqzlFFdId0eaWqBohXIs4ex9u2" to "azka Azka (32 repairs)",
                "SNTZpfi0QcczNxWIJ8UHklYsYwy1" to "Ahmad Bonchan (27 repairs)",
                "VXE0EJHcnuV62s1JuD2pPR32BEt1" to "Agus Septiawan asep (23 repairs)",
                "s1CZ9Sf1fyZ28TA8IkSpxjA3Dhh1" to "Fadli Ahmad Yazid (17 repairs)"
            )

            val userInfo = knownUsers[firebaseUser.uid]
            if (userInfo != null) {
                Log.d("HistoryActivity", "✅ User found in test data: $userInfo")
            } else {
                Log.w("HistoryActivity", "⚠️ User NOT found in test data!")
                Log.w("HistoryActivity", "Known user IDs:")
                knownUsers.forEach { (uid, info) ->
                    Log.w("HistoryActivity", "  $uid: $info")
                }
            }
        } else {
            Log.e("HistoryActivity", "❌ No Firebase user authenticated!")
        }

        // Also check FirebaseManager
        val managerUserId = FirebaseManager.getUserId()
        Log.d("HistoryActivity", "FirebaseManager.getUserId(): $managerUserId")

        val managerUser = FirebaseManager.getCurrentFirebaseUser()
        Log.d("HistoryActivity", "FirebaseManager.getCurrentFirebaseUser(): ${managerUser?.email}")
    }

    private fun forceRefresh() {
        lifecycleScope.launch {
            try {
                Log.d("HistoryActivity", "🔄 FORCE REFRESH STARTED")

                runOnUiThread {
                    Toast.makeText(this@HistoryActivity, "Refreshing data...", Toast.LENGTH_SHORT).show()
                }

                // Clear Firestore offline cache
                try {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance().clearPersistence()
                    Log.d("HistoryActivity", "✅ Firestore cache cleared")
                } catch (e: Exception) {
                    Log.w("HistoryActivity", "⚠️ Failed to clear cache: ${e.message}")
                }

                // Wait a bit for cache to clear
                kotlinx.coroutines.delay(1000)

                // Force reload data
                Log.d("HistoryActivity", "🔄 Reloading repair history...")
                loadRepairHistory()

                runOnUiThread {
                    Toast.makeText(this@HistoryActivity, "Data refreshed!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("HistoryActivity", "💥 Error during force refresh", e)
                runOnUiThread {
                    Toast.makeText(this@HistoryActivity, "Refresh failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
