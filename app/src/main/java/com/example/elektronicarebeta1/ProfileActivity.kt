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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.example.elektronicarebeta1.firebase.FirebaseManager
import com.example.elektronicarebeta1.models.User
import com.example.elektronicarebeta1.cloudinary.CloudinaryManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
    } 
    
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(this@ProfileActivity)
                    .load(uri)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(profileImageView)
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
            openImagePicker()
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
                    val userId = FirebaseManager.getUserId()
                    if (userId == null) {
                        Toast.makeText(this@ProfileActivity, "User not authenticated", Toast.LENGTH_LONG).show()
                        profileSaveProgressBar.visibility = View.GONE
                        saveProfileButton.isEnabled = true
                        return@launch
                    }
                    
                    val resultUrl = CloudinaryManager.uploadProfileImage(selectedImageUri!!, userId)
                    if (resultUrl == null) {
                        Toast.makeText(this@ProfileActivity, "Profile image upload failed. Please try again.", Toast.LENGTH_LONG).show()
                        profileSaveProgressBar.visibility = View.GONE
                        saveProfileButton.isEnabled = true
                        return@launch // Stop further processing
                    }
                    uploadedImageUrl = resultUrl // Store the new image URL
                    selectedImageUri = null // Clear URI after successful upload
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
                    if (FirebaseManager.updateUserData(updatedData)) {
                        Toast.makeText(this@ProfileActivity, "Profile saved successfully!", Toast.LENGTH_LONG).show()
                        loadUserProfile() // Refresh data
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to update profile details.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // This means no text fields changed and no new image was uploaded.
                    Toast.makeText(this@ProfileActivity, "No changes to save.", Toast.LENGTH_SHORT).show()
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