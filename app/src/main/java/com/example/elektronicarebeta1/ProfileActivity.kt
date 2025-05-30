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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.DataSource
import com.google.android.material.textfield.TextInputLayout
import com.example.elektronicarebeta1.firebase.FirebaseManager
import com.example.elektronicarebeta1.models.User
import com.example.elektronicarebeta1.cloudinary.CloudinaryManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
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
    private var currentUser: User? = null

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 100
        private const val CAMERA_PERMISSION_REQUEST_CODE = 101
        private const val TAG = "ProfileActivity"
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                updateProfileImagePreview(uri)
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri?.let { uri ->
                updateProfileImagePreview(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Cloudinary
        CloudinaryManager.initialize(this)

        initializeViews()
        setupListeners()
        setupBottomNavigation()
        loadUserProfile()
    }

    override fun onResume() {
        super.onResume()
        if (!FirebaseManager.isUserAuthenticated()) {
            redirectToLogin()
            return
        }

        // Force reload user profile to get latest data
        loadUserProfile()
    }

    private fun initializeViews() {
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

        backButton.setOnClickListener { finish() }
        logoutButton.setOnClickListener { showSignOutConfirmationDialog() }
    }

    private fun setupListeners() {
        editPhotoIcon.setOnClickListener { showImagePickerDialog() }
        saveProfileButton.setOnClickListener { handleSaveChanges() }

        // Clear errors on text change
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
            try {
                Log.d(TAG, "Loading user profile...")

                val userDoc = FirebaseManager.getUserData()
                if (userDoc != null && userDoc.exists()) {
                    Log.d(TAG, "User document data: ${userDoc.data}")
                    currentUser = User.fromDocument(userDoc)
                    if (currentUser != null) {
                        Log.d(TAG, "Successfully parsed user: ${currentUser!!.fullName}")
                        displayUserData(currentUser!!)
                    } else {
                        Log.e(TAG, "Failed to parse user document")
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Failed to load profile data", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e(TAG, "User document not found or doesn't exist")
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "User profile not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user profile", e)
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayUserData(user: User) {
        runOnUiThread {
            Log.d(TAG, "Displaying user data for: ${user.fullName}")

            editTextUserName.setText(user.fullName)
            emailText.text = user.email
            editTextPhone.setText(user.phone ?: "")
            editTextAddress.setText(user.address ?: "")

            // Display join date with better formatting
            user.createdAt?.let { date ->
                Log.d(TAG, "User created at: $date")
                val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                userDateJoinedText.text = "Joined ${dateFormat.format(date)}"
            } ?: run {
                Log.w(TAG, "No createdAt date found for user")
                userDateJoinedText.text = "Join date not available"
            }

            // Load profile image with proper cache handling
            Log.d(TAG, "Loading profile image: ${user.profileImageUrl}")
            loadProfileImage(user.profileImageUrl)
        }
    }

    private fun loadProfileImage(imageUrl: String?) {
        Log.d(TAG, "loadProfileImage called with URL: $imageUrl")

        // Clear Glide cache first to ensure fresh image load
        Glide.with(this).clear(profileImageView)

        val imageToLoad = if (!imageUrl.isNullOrEmpty() &&
            !imageUrl.contains("placeholder") &&
            imageUrl.startsWith("http")) {
            Log.d(TAG, "Loading profile image from URL: $imageUrl")
            imageUrl
        } else {
            Log.d(TAG, "Using placeholder image (URL was: $imageUrl)")
            R.drawable.profile_placeholder
        }

        Glide.with(this)
            .load(imageToLoad)
            .placeholder(R.drawable.profile_placeholder)
            .error(R.drawable.profile_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable disk cache for fresh reload
            .skipMemoryCache(true) // Skip memory cache to force reload
            .circleCrop()
            .listener(object : RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<android.graphics.drawable.Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(TAG, "Failed to load profile image: $e")
                    return false
                }

                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable,
                    model: Any,
                    target: Target<android.graphics.drawable.Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "Profile image loaded successfully")
                    return false
                }
            })
            .into(profileImageView)
    }

    private fun updateProfileImagePreview(uri: Uri) {
        Log.d(TAG, "Updating profile image preview with URI: $uri")

        // Clear previous image and load new one
        Glide.with(this).clear(profileImageView)

        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.profile_placeholder)
            .error(R.drawable.profile_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .circleCrop()
            .into(profileImageView)
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
        if (checkStoragePermission()) {
            launchImagePicker()
        } else {
            requestStoragePermission()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        ActivityCompat.requestPermissions(this, arrayOf(permission), STORAGE_PERMISSION_REQUEST_CODE)
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    private fun launchCamera() {
        val photoFile = createImageFile()
        selectedImageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchImagePicker()
                } else {
                    Toast.makeText(this, "Storage permission required", Toast.LENGTH_LONG).show()
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera()
                } else {
                    Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleSaveChanges() {
        lifecycleScope.launch {
            saveProfileButton.isEnabled = false
            profileSaveProgressBar.visibility = View.VISIBLE

            try {
                // Validate input
                if (!validateInput()) {
                    return@launch
                }

                val userId = FirebaseManager.getUserId()
                if (userId == null) {
                    Toast.makeText(this@ProfileActivity, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Upload image if selected
                var imageUrl: String? = null
                selectedImageUri?.let { uri ->
                    Log.d(TAG, "Uploading profile image...")

                    try {
                        // Check if Cloudinary is configured
                        if (!CloudinaryManager.isConfigured(this@ProfileActivity)) {
                            Log.w(TAG, "Cloudinary not configured, skipping image upload")
                            runOnUiThread {
                                Toast.makeText(this@ProfileActivity, "Image upload not configured", Toast.LENGTH_SHORT).show()
                            }
                            return@let
                        }

                        imageUrl = CloudinaryManager.uploadProfileImage(this@ProfileActivity, uri, userId)
                        Log.d(TAG, "Upload result: $imageUrl")

                        if (imageUrl.isNullOrEmpty()) {
                            Log.w(TAG, "Image upload failed, using local URI as fallback")
                            // Use the local URI as fallback if cloud upload fails
                            imageUrl = uri.toString()
                            runOnUiThread {
                                Toast.makeText(this@ProfileActivity, "Image saved locally (cloud upload failed)", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.d(TAG, "Image uploaded successfully: $imageUrl")
                            runOnUiThread {
                                Toast.makeText(this@ProfileActivity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error uploading image", e)
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // Prepare update data
                val updateData = mutableMapOf<String, Any>()
                val newFullName = editTextUserName.text.toString().trim()
                val newPhone = editTextPhone.text.toString().trim()
                val newAddress = editTextAddress.text.toString().trim()

                // Only update changed fields
                if (currentUser?.fullName != newFullName) {
                    updateData["fullName"] = newFullName
                }
                if (currentUser?.phone != newPhone) {
                    updateData["phone"] = newPhone
                }
                if (currentUser?.address != newAddress) {
                    updateData["address"] = newAddress
                }

                // Add image URL if successfully uploaded
                if (!imageUrl.isNullOrEmpty()) {
                    updateData["profileImageUrl"] = imageUrl
                }

                // Update Firebase if there are changes
                if (updateData.isNotEmpty()) {
                    Log.d(TAG, "Updating user data: $updateData")
                    val success = FirebaseManager.updateUserData(updateData)

                    if (success) {
                        Log.d(TAG, "Profile updated successfully")
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        }

                        // Update current user object
                        currentUser = currentUser?.copy(
                            fullName = newFullName,
                            phone = newPhone,
                            address = newAddress,
                            profileImageUrl = if (!imageUrl.isNullOrEmpty()) imageUrl else currentUser?.profileImageUrl
                        )

                        // Clear selected image
                        selectedImageUri = null

                        // Force reload profile image if new image was uploaded
                        if (!imageUrl.isNullOrEmpty()) {
                            runOnUiThread {
                                Log.d(TAG, "Reloading profile image after successful upload")
                                loadProfileImage(imageUrl)
                            }
                        }

                        // Reload the entire profile to ensure all data is fresh
                        runOnUiThread {
                            loadUserProfile()
                        }
                    } else {
                        Log.e(TAG, "Failed to update profile")
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "No changes to save", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error saving profile", e)
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                runOnUiThread {
                    profileSaveProgressBar.visibility = View.GONE
                    saveProfileButton.isEnabled = true
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val textInputLayoutUserName = findViewById<TextInputLayout>(R.id.text_input_layout_user_name)
        val textInputLayoutPhone = findViewById<TextInputLayout>(R.id.text_input_layout_phone)

        val newFullName = editTextUserName.text.toString().trim()
        if (newFullName.isEmpty()) {
            runOnUiThread {
                textInputLayoutUserName.error = "Full name cannot be empty"
            }
            return false
        } else {
            runOnUiThread {
                textInputLayoutUserName.error = null
            }
        }

        val newPhone = editTextPhone.text.toString().trim()
        if (newPhone.isNotEmpty() && !newPhone.matches(Regex("^\\+?[0-9]{10,13}$"))) {
            runOnUiThread {
                textInputLayoutPhone.error = "Enter a valid phone number"
            }
            return false
        } else {
            runOnUiThread {
                textInputLayoutPhone.error = null
            }
        }

        return true
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
            // Clear Glide cache before sign out
            Glide.get(this).clearMemory()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Glide.get(this@ProfileActivity).clearDiskCache()
                } catch (e: Exception) {
                    Log.e(TAG, "Error clearing disk cache: $e")
                }

                // Switch back to main thread for UI operations
                withContext(Dispatchers.Main) {
                    FirebaseManager.signOut()
                    redirectToLogin()
                    dialog.dismiss()
                }
            }
        }

        dialog.setCancelable(true)
        dialog.show()
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
