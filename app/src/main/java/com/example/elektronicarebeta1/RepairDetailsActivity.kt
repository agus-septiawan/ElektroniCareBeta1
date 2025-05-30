package com.example.elektronicarebeta1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.elektronicarebeta1.firebase.FirebaseManager
import com.example.elektronicarebeta1.models.Repair
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class RepairDetailsActivity : AppCompatActivity() {

    private lateinit var deviceImageView: ImageView
    private lateinit var deviceNameText: TextView
    private lateinit var deviceTypeText: TextView
    private lateinit var issueDescriptionText: TextView
    private lateinit var statusChip: Chip
    private lateinit var estimatedCostText: TextView
    private lateinit var appointmentDateText: TextView
    private lateinit var locationText: TextView
    private lateinit var technicianEmailText: TextView
    private lateinit var createdAtText: TextView
    private lateinit var updatedAtText: TextView
    private lateinit var cancelButton: Button
    private lateinit var contactTechnicianButton: Button

    private var repairId: String? = null
    private var currentRepair: Repair? = null

    companion object {
        const val EXTRA_REPAIR_ID = "repair_id"
        private const val TAG = "RepairDetailsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_details)

        initializeViews()
        setupClickListeners()

        repairId = intent.getStringExtra(EXTRA_REPAIR_ID)
        if (repairId == null) {
            Log.e(TAG, "No repair ID provided")
            Toast.makeText(this, "Error: No repair ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadRepairDetails()
    }

    private fun initializeViews() {
        deviceImageView = findViewById(R.id.device_image)
        deviceNameText = findViewById(R.id.device_name)
        deviceTypeText = findViewById(R.id.device_type)
        issueDescriptionText = findViewById(R.id.issue_description)
        statusChip = findViewById(R.id.status_chip)
        estimatedCostText = findViewById(R.id.estimated_cost)
        appointmentDateText = findViewById(R.id.appointment_date)
        locationText = findViewById(R.id.location)
        technicianEmailText = findViewById(R.id.technician_email)
        createdAtText = findViewById(R.id.created_at)
        updatedAtText = findViewById(R.id.updated_at)
        cancelButton = findViewById(R.id.cancel_button)
        contactTechnicianButton = findViewById(R.id.contact_technician_button)
    }

    private fun setupClickListeners() {
        // Back button
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Cancel repair button
        cancelButton.setOnClickListener {
            showCancelConfirmationDialog()
        }

        // Contact technician button
        contactTechnicianButton.setOnClickListener {
            contactTechnician()
        }
    }

    private fun loadRepairDetails() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading repair details for ID: $repairId")

                val repair = FirebaseManager.getRepairById(repairId!!)
                if (repair != null) {
                    currentRepair = repair
                    runOnUiThread {
                        displayRepairDetails(repair)
                    }
                } else {
                    Log.e(TAG, "Repair not found")
                    runOnUiThread {
                        Toast.makeText(this@RepairDetailsActivity, "Repair not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading repair details", e)
                runOnUiThread {
                    Toast.makeText(this@RepairDetailsActivity, "Error loading repair details: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun displayRepairDetails(repair: Repair) {
        // Device information
        deviceNameText.text = repair.deviceModel
        deviceTypeText.text = repair.deviceType
        issueDescriptionText.text = repair.issueDescription

        // Load device image
        if (!repair.deviceImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(repair.deviceImageUrl)
                .apply(RequestOptions().transform(RoundedCorners(16)))
                .placeholder(R.drawable.ic_phone_outline)
                .error(R.drawable.ic_phone_outline)
                .into(deviceImageView)
        } else {
            deviceImageView.setImageResource(R.drawable.ic_phone_outline)
        }

        // Status
        setupStatusChip(repair.status)

        // Cost
        if (repair.estimatedCost != null) {
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            estimatedCostText.text = formatter.format(repair.estimatedCost)
            estimatedCostText.visibility = View.VISIBLE
        } else {
            estimatedCostText.text = "Cost not estimated yet"
            estimatedCostText.visibility = View.VISIBLE
        }

        // Dates
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' HH:mm", Locale.getDefault())
        val shortDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

        appointmentDateText.text = repair.appointmentTimestamp?.let {
            shortDateFormat.format(it)
        } ?: "Not scheduled yet"

        createdAtText.text = repair.createdAt?.let {
            dateFormat.format(it)
        } ?: "Unknown"

        updatedAtText.text = repair.updatedAt?.let {
            dateFormat.format(it)
        } ?: "Not updated"

        // Location
        locationText.text = repair.location ?: "Location not specified"

        // Technician
        if (!repair.technicianEmail.isNullOrEmpty()) {
            technicianEmailText.text = repair.technicianEmail
            technicianEmailText.visibility = View.VISIBLE
            contactTechnicianButton.visibility = View.VISIBLE
        } else {
            technicianEmailText.text = "No technician assigned yet"
            technicianEmailText.visibility = View.VISIBLE
            contactTechnicianButton.visibility = View.GONE
        }

        // Show/hide cancel button based on status
        when (repair.status) {
            "pending", "pending_confirmation" -> {
                cancelButton.visibility = View.VISIBLE
            }
            else -> {
                cancelButton.visibility = View.GONE
            }
        }
    }

    private fun setupStatusChip(status: String) {
        when (status) {
            "completed" -> {
                statusChip.text = "✓ Completed"
                statusChip.setChipBackgroundColorResource(R.color.color_localhost_Jewel)
                statusChip.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            "in_progress" -> {
                statusChip.text = "⚡ In Progress"
                statusChip.setChipBackgroundColorResource(R.color.color_localhost_Persian_Blue)
                statusChip.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            "cancelled" -> {
                statusChip.text = "✗ Cancelled"
                statusChip.setChipBackgroundColorResource(R.color.destructive_red_fg)
                statusChip.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            "pending_confirmation" -> {
                statusChip.text = "⏳ Pending Confirmation"
                statusChip.setChipBackgroundColorResource(R.color.color_localhost_Flamingo)
                statusChip.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            else -> {
                statusChip.text = "⏰ Pending"
                statusChip.setChipBackgroundColorResource(R.color.color_localhost_Pale_Sky)
                statusChip.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }
    }

    private fun showCancelConfirmationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Cancel Repair Request")
        builder.setMessage("Are you sure you want to cancel this repair request? This action cannot be undone.")
        builder.setPositiveButton("Yes, Cancel") { _, _ ->
            cancelRepair()
        }
        builder.setNegativeButton("No, Keep") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun cancelRepair() {
        lifecycleScope.launch {
            try {
                val success = FirebaseManager.updateRepairStatus(repairId!!, "cancelled")
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this@RepairDetailsActivity, "Repair request cancelled successfully", Toast.LENGTH_SHORT).show()
                        // Refresh the display
                        loadRepairDetails()
                    } else {
                        Toast.makeText(this@RepairDetailsActivity, "Failed to cancel repair request", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling repair", e)
                runOnUiThread {
                    Toast.makeText(this@RepairDetailsActivity, "Error cancelling repair: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun contactTechnician() {
        currentRepair?.technicianEmail?.let { email ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "Regarding Repair Request - ${currentRepair?.deviceModel}")
                putExtra(Intent.EXTRA_TEXT, "Hello,\n\nI would like to discuss my repair request for ${currentRepair?.deviceModel}.\n\nRepair ID: $repairId\nIssue: ${currentRepair?.issueDescription}\n\nThank you.")
            }

            try {
                startActivity(Intent.createChooser(intent, "Send Email"))
            } catch (e: Exception) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
