package com.example.elektronicarebeta1

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.elektronicarebeta1.firebase.FirebaseManager
import com.example.elektronicarebeta1.cloudinary.CloudinaryManager
import com.example.elektronicarebeta1.utils.DataPersistenceHelper
import com.example.elektronicarebeta1.utils.DebugHelper
import com.example.elektronicarebeta1.utils.EmailManager
import com.example.elektronicarebeta1.utils.WhatsAppManager
import com.example.elektronicarebeta1.models.User
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private lateinit var issueDescriptionEdit: EditText
    private lateinit var selectedDateText: TextView
    private lateinit var selectedTimeText: TextView
    private lateinit var submitButton: Button
    private lateinit var backButton: ImageView
    private lateinit var imagePreview: ImageView

    private var selectedDate: Date? = null
    private var selectedImageUri: Uri? = null
    private var serviceId: String? = null
    private var serviceName: String? = null
    private var servicePrice: Double? = null

    private val calendar = Calendar.getInstance()

    // Activity result launcher for image selection from gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                // Update UI to show selected image
                updateImagePreview()
            }
        }
    }

    // Activity result launcher for camera
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("BookingActivity", "Camera result: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("BookingActivity", "Camera photo taken successfully")
            selectedImageUri?.let { uri ->
                Log.d("BookingActivity", "Photo saved to: $uri")
                updateImagePreview()
            } ?: run {
                Log.e("BookingActivity", "selectedImageUri is null after taking photo")
            }
        } else {
            Log.d("BookingActivity", "Camera cancelled or failed")
        }
    }

    // Permission launcher for camera
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_request)

        // Get service details from intent
        serviceId = intent.getStringExtra("SERVICE_ID")
        serviceName = intent.getStringExtra("SERVICE_NAME")
        servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)

        if (serviceId == null || serviceName == null) {
            Toast.makeText(this, "Invalid service information", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupListeners()
    }
    
    override fun onResume() {
        super.onResume()
        // Check if user is still authenticated
        if (!FirebaseManager.isUserAuthenticated()) {
            Log.w("BookingActivity", "User not authenticated, redirecting to login")
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }
        // Force refresh auth token to ensure valid session
        lifecycleScope.launch {
            val tokenRefreshed = FirebaseManager.refreshAuthToken()
            Log.d("BookingActivity", "Auth token refresh: $tokenRefreshed")
        }
    }

    private fun initializeViews() {
        issueDescriptionEdit = findViewById(R.id.etProblemDescription)
        selectedDateText = findViewById<TextView>(R.id.tvSelectedDate)
        selectedTimeText = findViewById<TextView>(R.id.tvSelectedTime)
        submitButton = findViewById(R.id.btnSubmitRequest)
        backButton = findViewById(R.id.ivBackArrow)
        imagePreview = findViewById(R.id.ivImagePreview)

        // Initialize click listeners for photo options
        val takePhotoLayout = findViewById<LinearLayout>(R.id.takePhotoLayout)
        val uploadLayout = findViewById<LinearLayout>(R.id.llUploadLayout)

        takePhotoLayout.setOnClickListener { 
            Log.d("BookingActivity", "Take photo layout clicked")
            takePhoto() 
        }
        uploadLayout.setOnClickListener { 
            Log.d("BookingActivity", "Upload layout clicked")
            selectImageFromGallery() 
        }

        // Initialize date and time selection layouts
        val dateLayout = findViewById<LinearLayout>(R.id.llDateLayout)
        val timeLayout = findViewById<LinearLayout>(R.id.llTimeLayout)

        dateLayout.setOnClickListener { showDatePicker() }
        timeLayout.setOnClickListener { showTimePicker() }
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finish() }

        submitButton.setOnClickListener { submitBooking() }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDate = calendar.time
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set minimum date to tomorrow
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)
        datePickerDialog.datePicker.minDate = tomorrow.timeInMillis

        datePickerDialog.show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                updateTimeDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        selectedDateText.text = dateFormat.format(calendar.time)
    }

    private fun updateTimeDisplay() {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        selectedTimeText.text = timeFormat.format(calendar.time)
    }

    private fun takePhoto() {
        Log.d("BookingActivity", "takePhoto() called")
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("BookingActivity", "Camera permission granted, opening camera")
                openCamera()
            }
            else -> {
                Log.d("BookingActivity", "Requesting camera permission")
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            Log.d("BookingActivity", "Creating image file...")
            val photoFile = createImageFile()
            Log.d("BookingActivity", "Image file created: ${photoFile.absolutePath}")
            
            selectedImageUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            Log.d("BookingActivity", "FileProvider URI: $selectedImageUri")

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri)

            // Check if camera app is available
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                Log.d("BookingActivity", "Launching camera intent")
                takePictureLauncher.launch(takePictureIntent)
            } else {
                Log.e("BookingActivity", "No camera app found")
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("BookingActivity", "Error opening camera", e)
            Toast.makeText(this, "Unable to open camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        
        // Create the storage directory if it doesn't exist
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun updateImagePreview() {
        selectedImageUri?.let { uri ->
            Log.d("BookingActivity", "Updating image preview with URI: $uri")
            imagePreview.visibility = View.VISIBLE
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(imagePreview)
            Log.d("BookingActivity", "Image preview updated successfully")
        } ?: run {
            Log.d("BookingActivity", "No image URI to preview")
        }
    }

    private fun submitBooking() {
        val issueDescription = issueDescriptionEdit.text.toString().trim()

        if (issueDescription.isEmpty()) {
            Toast.makeText(this, "Please describe the issue", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable submit button to prevent multiple submissions
        submitButton.isEnabled = false

        lifecycleScope.launch {
            // First upload image if selected using Cloudinary
            var imageUrl: String? = null
            if (selectedImageUri != null) {
                Log.d("BookingActivity", "Uploading device image...")
                val userId = FirebaseManager.getUserId()
                if (userId == null) {
                    runOnUiThread {
                        Toast.makeText(this@BookingActivity, "User not authenticated", Toast.LENGTH_SHORT).show()
                        submitButton.isEnabled = true
                    }
                    return@launch
                }
                
                try {
                    val uploadResult = CloudinaryManager.uploadRepairImage(this@BookingActivity, selectedImageUri!!, userId, null)
                    imageUrl = uploadResult
                    Log.d("BookingActivity", "Image upload result: $imageUrl")
                    
                    if (imageUrl.isNullOrEmpty()) {
                        Log.w("BookingActivity", "Image upload failed, continuing without image")
                        // Don't return here - continue with booking without image
                    }
                } catch (e: Exception) {
                    Log.e("BookingActivity", "Error uploading image", e)
                    // Continue without image
                }
            }

            // Create repair request data with proper structure for Repair model
            val repairData = hashMapOf(
                "issueDescription" to issueDescription,
                "serviceId" to serviceId,
                "status" to "pending_confirmation", // Use pending_confirmation as initial status
                "estimatedCost" to servicePrice,
                "appointmentTimestamp" to calendar.time,
                "location" to "ElektroniCare Service Center",
                "technicianEmail" to "agusseptiawanasep@gmail.com",
                "deviceType" to (serviceName ?: "Electronic Device"),
                "deviceModel" to (serviceName ?: "Electronic Repair Service"),
                "createdAt" to Date(),
                "updatedAt" to Date()
            )
            
            Log.d("BookingActivity", "Creating repair request with data: $repairData")

            // Add image URL if available
            if (imageUrl != null) {
                repairData["deviceImageUrl"] = imageUrl
            }

            // Submit repair request
            Log.d("BookingActivity", "Submitting repair request to Firebase...")
            val repairId = FirebaseManager.createRepairRequest(repairData)
            Log.d("BookingActivity", "Firebase response - Repair ID: $repairId")

            if (repairId != null) {
                Log.d("BookingActivity", "Repair request created successfully with ID: $repairId")
                
                // Verify data was saved correctly
                val verificationSuccess = DataPersistenceHelper.verifyRepairRequestSaved(repairId)
                if (verificationSuccess) {
                    Log.d("BookingActivity", "Repair request persistence verification successful")
                } else {
                    Log.w("BookingActivity", "Repair request persistence verification failed")
                    // Try to force save again if verification failed
                    Log.d("BookingActivity", "Attempting to force save booking data again")
                    val retryId = FirebaseManager.createRepairRequest(bookingData)
                    if (retryId != null) {
                        Log.d("BookingActivity", "Retry booking save successful with ID: $retryId")
                    } else {
                        Log.e("BookingActivity", "Retry booking save also failed")
                    }
                }
                
                // Debug booking persistence
                DebugHelper.debugBookingPersistence(repairId)
                
                // Add delay to ensure data is fully persisted
                kotlinx.coroutines.delay(2000)
                
                // Ensure data persistence
                val persistenceEnsured = FirebaseManager.ensureDataPersistence()
                Log.d("BookingActivity", "Data persistence ensured: $persistenceEnsured")
                
                // Final verification after delay
                val finalVerification = DataPersistenceHelper.verifyRepairRequestSaved(repairId)
                Log.d("BookingActivity", "Final verification after delay: $finalVerification")
                
                // Get user data for email and WhatsApp
                val currentUser = FirebaseManager.getCurrentUser()
                if (currentUser != null) {
                    Log.d("BookingActivity", "Sending email notifications for booking: $repairId")
                    try {
                        // Send email notifications
                        EmailManager.sendBookingNotification(
                            context = this@BookingActivity,
                            bookingId = repairId,
                            user = currentUser,
                            serviceName = serviceName ?: "Electronic Repair",
                            issueDescription = issueDescription,
                            appointmentDate = calendar.time,
                            estimatedCost = servicePrice,
                            deviceImageUrl = imageUrl
                        )
                        
                        EmailManager.sendCustomerConfirmation(
                            context = this@BookingActivity,
                            bookingId = repairId,
                            user = currentUser,
                            serviceName = serviceName ?: "Electronic Repair",
                            appointmentDate = calendar.time,
                            estimatedCost = servicePrice
                        )
                        Log.d("BookingActivity", "Email notifications sent successfully")
                    } catch (e: Exception) {
                        Log.e("BookingActivity", "Error sending email notifications", e)
                    }
                }
                
                runOnUiThread {
                    Toast.makeText(this@BookingActivity, "Booking submitted successfully!", Toast.LENGTH_SHORT).show()
                }
                
                showSuccessDialog(repairId, currentUser)
            } else {
                Log.e("BookingActivity", "Failed to create repair request - repairId is null")
                runOnUiThread {
                    Toast.makeText(this@BookingActivity, "Failed to submit booking. Please check your connection and try again.", Toast.LENGTH_LONG).show()
                    submitButton.isEnabled = true
                }
            }
        }
    }

    private fun showSuccessDialog(repairId: String, user: User? = null) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_submission_success, null)
            builder.setView(dialogView)

            val repairIdText = dialogView.findViewById<TextView>(R.id.repair_id_text)
            val whatsappButton = dialogView.findViewById<Button>(R.id.whatsapp_button)
            val doneButton = dialogView.findViewById<Button>(R.id.done_button)

            repairIdText.text = "Your Repair ID: $repairId"

            val dialog = builder.create()
            dialog.setCancelable(false)

            whatsappButton.setOnClickListener {
                Log.d("BookingActivity", "WhatsApp button clicked for booking: $repairId")
                try {
                    if (user != null) {
                        WhatsAppManager.sendBookingToTechnician(
                            context = this@BookingActivity,
                            bookingId = repairId,
                            customerName = user.fullName,
                            serviceName = serviceName ?: "Electronic Repair",
                            issueDescription = issueDescriptionEdit.text.toString(),
                            appointmentDate = calendar.time,
                            estimatedCost = servicePrice,
                            customerPhone = user.phone
                        )
                        Log.d("BookingActivity", "WhatsApp message sent to technician")
                    } else {
                        // Fallback to old method if user data not available
                        val message = "Hello, I've submitted a repair request through ElektroniCare.\n" +
                                "My Repair ID: $repairId\n" +
                                "Issue: ${issueDescriptionEdit.text}\n\n" +
                                "Please provide assistance."

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")

                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                            Log.d("BookingActivity", "WhatsApp fallback method used")
                        } else {
                            Toast.makeText(this@BookingActivity, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BookingActivity", "Error opening WhatsApp", e)
                    Toast.makeText(this@BookingActivity, "Failed to open WhatsApp", Toast.LENGTH_SHORT).show()
                }

                navigateToDashboard()
                dialog.dismiss()
            }

            doneButton.setOnClickListener {
                navigateToDashboard()
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
