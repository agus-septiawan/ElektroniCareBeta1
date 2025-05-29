package com.example.elektronicarebeta1

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
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
import com.google.android.material.textfield.TextInputLayout
import com.example.elektronicarebeta1.firebase.FirebaseManager
import com.example.elektronicarebeta1.models.User
import com.example.elektronicarebeta1.utils.DataPersistenceHelper
import com.example.elektronicarebeta1.cloudinary.CloudinaryManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var emailText: TextView
    private lateinit var editTextUserName: com.google.android.material.textfield.TextInputEditText
    private lateinit var editTextPhone: com.google.android.material.textfield.TextInputEditText
    private lateinit var editTextAddress: com.google.android.material.textfield.TextInputEditText
    private lateinit var saveProfileButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var editPhotoIcon: ImageView
    private lateinit var userDateJoinedText: TextView 
    private lateinit var profileSaveProgressBar: ProgressBar 
    
    private var selectedImageUri: Uri? = null
    private var originalUser: User? = null
    
    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 100
        private const val CAMERA_PERMISSION_REQUEST_CODE = 101
    } 
    
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                updateProfileImage()
            }
        }
    }
    
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri?.let { uri ->
                updateProfileImage()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        editTextUserName = findViewById(R.id.edit_text_user_name)
        emailText = findViewById(R.id.user_email)
        editTextPhone = findViewById(R.id.edit_text_phone)
        editTextAddress = findViewById(R.id.edit_text_address)
        profileImageView = findViewById(R.id.profile_image)
        editPhotoIcon = findViewById(R.id.edit_photo_icon)
        saveProfileButton = findViewById(R.id.save_profile_button)
        userDateJoinedText = findViewById(R.id.user_date_joined) 
        profileSaveProgressBar = findViewById(R.id.profile_save_progress_bar)
        
        val backButton = findViewById<ImageView>(R.id.back_button)
        val logoutButton = findViewById<Button>(R.id.logout_button)
        
        backButton.setOnClickListener {
            finish()
        }
        
        editPhotoIcon.setOnClickListener {
            showImagePickerDialog()
        }
        
        saveProfileButton.setOnClickListener {
            handleSaveChanges()
        }
        
        logoutButton.setOnClickListener {
            showSignOutConfirmationDialog()
        }
        
        setupBottomNavigation()
        loadUserProfile()

        // Add TextChangedListeners for immediate error clearing
        val textInputLayoutUserName = findViewById<TextInputLayout>(R.id.text_input_layout_user_name)
        editTextUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutUserName.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val textInputLayoutPhone = findViewById<TextInputLayout>(R.id.text_input_layout_phone)
        editTextPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayoutPhone.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    override fun onResume() {
        super.onResume()
        // Check if user is still authenticated
        if (!FirebaseManager.isUserAuthenticated()) {
            Log.w("ProfileActivity", "User not authenticated, redirecting to login")
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }
        // Force refresh profile data when returning to this activity
        lifecycleScope.launch {
            val refreshSuccess = DataPersistenceHelper.forceRefreshUserData()
            Log.d("ProfileActivity", "Force refresh completed: $refreshSuccess")
            loadUserProfile()
        }
    }
    
    private fun setupBottomNavigation() {
        val homeNav = findViewById<View>(R.id.nav_home)
        val historyNav = findViewById<View>(R.id.nav_history)
        val servicesNav = findViewById<View>(R.id.nav_services)
        
        homeNav.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
        
        historyNav.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
        
        servicesNav.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
    
    private fun loadUserProfile() {
        lifecycleScope.launch {
            Log.d("ProfileActivity", "Loading user profile...")
            val userDoc = FirebaseManager.getUserData()
            
            if (userDoc != null && userDoc.exists()) {
                Log.d("ProfileActivity", "User document found, parsing...")
                originalUser = User.fromDocument(userDoc) 
                
                originalUser?.let {
                    Log.d("ProfileActivity", "User parsed successfully: ${it.fullName}")
                    editTextUserName.setText(it.fullName)
                    emailText.text = it.email 
                    editTextPhone.setText(it.phone ?: "")
                    editTextAddress.setText(it.address ?: "")
                    
                    it.createdAt?.let { date ->
                        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                        userDateJoinedText.text = dateFormat.format(date)
                    } ?: run {
                        userDateJoinedText.text = "N/A" 
                    }
                    
                    if (!it.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this@ProfileActivity)
                            .load(it.profileImageUrl)
                            .placeholder(R.drawable.profile_placeholder)
                            .error(R.drawable.profile_placeholder)
                            .circleCrop()
                            .into(profileImageView)
                    } else {
                         Glide.with(this@ProfileActivity)
                            .load(R.drawable.profile_placeholder) 
                            .circleCrop()
                            .into(profileImageView)
                    }
                } ?: run {
                    Log.e("ProfileActivity", "Failed to parse user from document")
                    Toast.makeText(this@ProfileActivity, "Failed to parse profile data", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("ProfileActivity", "User document not found or doesn't exist")
                Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openImagePicker()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun openCamera() {
        if (checkCameraPermission()) {
            launchCamera()
        } else {
            requestCameraPermission()
        }
    }
    
    private fun openImagePicker() {
        // Check for storage permission
        if (checkStoragePermission()) {
            launchImagePicker()
        } else {
            requestStoragePermission()
        }
    }
    
    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 and below uses READ_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            STORAGE_PERMISSION_REQUEST_CODE
        )
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }
    
    private fun launchCamera() {
        val photoFile = createImageFile()
        selectedImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri)
        
        try {
            takePictureLauncher.launch(takePictureIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchImagePicker()
                } else {
                    Toast.makeText(
                        this,
                        "Storage permission is required to select profile image",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera()
                } else {
                    Toast.makeText(
                        this,
                        "Camera permission is required to take photos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun updateProfileImage() {
        selectedImageUri?.let { uri ->
            Log.d("ProfileActivity", "Updating profile image with URI: $uri")
            Glide.with(this@ProfileActivity)
                .load(uri)
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .circleCrop()
                .into(profileImageView)
        }
    }
    
    private fun handleSaveChanges() {
        lifecycleScope.launch {
            saveProfileButton.isEnabled = false
            profileSaveProgressBar.visibility = View.VISIBLE

            val textInputLayoutUserName = findViewById<TextInputLayout>(R.id.text_input_layout_user_name)
            val textInputLayoutPhone = findViewById<TextInputLayout>(R.id.text_input_layout_phone)

            val newFullName = editTextUserName.text.toString().trim()
            if (newFullName.isEmpty()) {
                textInputLayoutUserName.error = "Full name cannot be empty"
                profileSaveProgressBar.visibility = View.GONE
                saveProfileButton.isEnabled = true
                return@launch
            } else {
                textInputLayoutUserName.error = null
            }

            val newPhone = editTextPhone.text.toString().trim()
            // Validate phone only if it's not empty, to allow users to clear it if they wish
            if (newPhone.isNotEmpty() && (!newPhone.matches(Regex("^\\+?[0-9]{10,13}$")))) { // Regex for basic international phone format
                textInputLayoutPhone.error = "Enter a valid phone number (e.g., +6281234567890 or 081234567890)"
                profileSaveProgressBar.visibility = View.GONE
                saveProfileButton.isEnabled = true
                return@launch
            } else {
                textInputLayoutPhone.error = null
            }

            try {
                var uploadedImageUrl: String? = null // Will store the URL of a newly uploaded image.

                // Image Upload Handling using Cloudinary
                if (selectedImageUri != null) {
                    Log.d("ProfileActivity", "Starting image upload for selected URI: $selectedImageUri")
                    val userId = FirebaseManager.getUserId()
                    if (userId == null) {
                        Toast.makeText(this@ProfileActivity, "User not authenticated", Toast.LENGTH_LONG).show()
                        profileSaveProgressBar.visibility = View.GONE
                        saveProfileButton.isEnabled = true
                        return@launch
                    }
                    
                    Log.d("ProfileActivity", "Uploading image for user: $userId")
                    try {
                        val resultUrl = CloudinaryManager.uploadProfileImage(selectedImageUri!!, userId)
                        Log.d("ProfileActivity", "Upload result URL: $resultUrl")
                        
                        if (resultUrl.isNullOrEmpty()) {
                            Log.w("ProfileActivity", "Image upload returned null/empty URL, continuing without image update")
                            runOnUiThread {
                                Toast.makeText(this@ProfileActivity, "Image upload failed, but profile data will still be saved", Toast.LENGTH_LONG).show()
                            }
                            // Don't return here - continue with profile update without image
                        } else {
                            uploadedImageUrl = resultUrl // Store the new image URL
                            Log.d("ProfileActivity", "Image uploaded successfully: $uploadedImageUrl")
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileActivity", "Error uploading image", e)
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Image upload failed, but profile data will still be saved", Toast.LENGTH_LONG).show()
                        }
                        // Continue without image update
                    }
                    
                    // Update the image view immediately with the new URL if upload was successful
                    if (!uploadedImageUrl.isNullOrEmpty()) {
                        runOnUiThread {
                            Glide.with(this@ProfileActivity)
                                .load(uploadedImageUrl)
                                .placeholder(R.drawable.profile_placeholder)
                                .error(R.drawable.profile_placeholder)
                                .circleCrop()
                                .into(profileImageView)
                        }
                    }
                    
                    selectedImageUri = null // Clear URI after processing
                }

                // Consolidate Data for Update
                val updatedData = mutableMapOf<String, Any>()
                val newAddress = editTextAddress.text.toString().trim()

                // Populate updatedData with fullName, phone, and address only if they have changed
                if (originalUser?.fullName != newFullName) {
                    updatedData["fullName"] = newFullName
                }
                if (originalUser?.phone != newPhone) {
                    updatedData["phone"] = newPhone
                }
                if (originalUser?.address != newAddress) {
                    updatedData["address"] = newAddress
                }

                // If a new image was successfully uploaded, add its URL to updatedData
                if (uploadedImageUrl != null) {
                    updatedData["profileImageUrl"] = uploadedImageUrl
                }

                // Firestore Update Call
                if (updatedData.isNotEmpty()) {
                    Log.d("ProfileActivity", "Updating profile with data: $updatedData")
                    val updateSuccess = FirebaseManager.updateUserData(updatedData)
                    if (updateSuccess) {
                        Log.d("ProfileActivity", "Profile update successful")
                        // Update the original user data to reflect changes
                        originalUser = originalUser?.copy(
                            fullName = newFullName,
                            phone = newPhone,
                            address = newAddress,
                            profileImageUrl = uploadedImageUrl ?: originalUser?.profileImageUrl
                        )
                        
                        // Verify data was saved correctly
                        val verificationSuccess = DataPersistenceHelper.verifyUserDataSaved(updatedData)
                        if (verificationSuccess) {
                            Log.d("ProfileActivity", "Data persistence verification successful")
                        } else {
                            Log.w("ProfileActivity", "Data persistence verification failed")
                        }
                        
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Profile saved successfully!", Toast.LENGTH_LONG).show()
                        }
                        
                        // Refresh data from server to ensure consistency
                        loadUserProfile()
                    } else {
                        Log.e("ProfileActivity", "Profile update failed")
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Failed to update profile details.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    // This means no text fields changed and no new image was uploaded.
                    Log.d("ProfileActivity", "No changes detected to save")
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "No changes to save.", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                profileSaveProgressBar.visibility = View.GONE
                saveProfileButton.isEnabled = true
            }
        }
    }

    private fun showSignOutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_signout, null)
        builder.setView(dialogView)

        val dialog = builder.create()

        val cancelButton = dialogView.findViewById<Button>(R.id.button_dialog_cancel)
        val signOutButton = dialogView.findViewById<Button>(R.id.button_dialog_signout)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        signOutButton.setOnClickListener {
            FirebaseManager.signOut()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
            dialog.dismiss()
        }
        
        dialog.setCancelable(true)
        dialog.show()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}