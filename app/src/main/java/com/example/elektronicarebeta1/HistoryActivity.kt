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

        // Set up bottom navigation
        setupBottomNavigation()

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
            Log.d("HistoryActivity", "Loading repair history...")

            var repairsSnapshot = FirebaseManager.getUserRepairs()

            if (repairsSnapshot == null || repairsSnapshot.isEmpty()) {
                Log.d("HistoryActivity", "No repairs found or snapshot is null on first attempt")

                // Add delay before retry
                kotlinx.coroutines.delay(1000)

                repairsSnapshot = FirebaseManager.getUserRepairs()
                if (repairsSnapshot != null && !repairsSnapshot.isEmpty()) {
                    Log.d("HistoryActivity", "Retry successful, found ${repairsSnapshot.size()} repairs")
                } else {
                    Log.d("HistoryActivity", "Retry also failed to fetch repairs.")
                }
            }

            // Process the snapshot if it's valid, otherwise handle empty state
            if (repairsSnapshot != null && !repairsSnapshot.isEmpty()) {
                processRepairSnapshot(repairsSnapshot)
            } else {
                Log.d("HistoryActivity", "Ultimately no repairs found, displaying empty state.")
                allRepairs = emptyList()
                displayRepairs(allRepairs)
            }
        }
    }

    private fun processRepairSnapshot(repairsSnapshot: com.google.firebase.firestore.QuerySnapshot) {
        Log.d("HistoryActivity", "Processing ${repairsSnapshot.size()} repairs")
        val repairs = mutableListOf<Repair>()

        for (document in repairsSnapshot.documents) {
            val repair = Repair.fromDocument(document)
            if (repair != null) {
                Log.d("HistoryActivity", "Adding repair: ${repair.deviceModel} - ${repair.status}")
                repairs.add(repair)
            } else {
                Log.e("HistoryActivity", "Failed to parse repair from document: ${document.id}")
            }
        }

        // Sort by date (newest first)
        allRepairs = repairs.sortedByDescending { it.appointmentTimestamp }
        filterRepairs()
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

        // Set status with appropriate color
        statusText.text = when (repair.status) {
            "completed" -> "Completed"
            "in_progress" -> "In Progress"
            "cancelled" -> "Cancelled"
            "pending_confirmation" -> "Pending Confirmation"
            else -> "Pending"
        }

        statusText.setBackgroundResource(
            when (repair.status) {
                "completed" -> R.drawable.status_completed_bg
                "in_progress" -> R.drawable.status_inprogress_bg
                "cancelled" -> R.drawable.status_cancelled_bg
                "pending_confirmation" -> R.drawable.status_inprogress_bg
                else -> R.drawable.status_inprogress_bg
            }
        )

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

        // Set click listener
        repairView.setOnClickListener {
            Toast.makeText(this, "Repair details coming soon", Toast.LENGTH_SHORT).show()
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

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
