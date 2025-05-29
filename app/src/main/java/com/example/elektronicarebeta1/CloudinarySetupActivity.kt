package com.example.elektronicarebeta1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.elektronicarebeta1.cloudinary.CloudinaryConfig

class CloudinarySetupActivity : AppCompatActivity() {

    private lateinit var etCloudName: EditText
    private lateinit var etApiKey: EditText
    private lateinit var etApiSecret: EditText
    private lateinit var btnSave: Button
    private lateinit var btnSkip: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloudinary_setup)

        initializeViews()
        setupClickListeners()
        loadExistingCredentials()
    }

    private fun initializeViews() {
        etCloudName = findViewById(R.id.etCloudName)
        etApiKey = findViewById(R.id.etApiKey)
        etApiSecret = findViewById(R.id.etApiSecret)
        btnSave = findViewById(R.id.btnSave)
        btnSkip = findViewById(R.id.btnSkip)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveCredentials()
        }

        btnSkip.setOnClickListener {
            finishSetup()
        }
    }

    private fun loadExistingCredentials() {
        etCloudName.setText(CloudinaryConfig.getCloudName(this))
        etApiKey.setText(CloudinaryConfig.getApiKey(this))
        etApiSecret.setText(CloudinaryConfig.getApiSecret(this))
    }

    private fun saveCredentials() {
        val cloudName = etCloudName.text.toString().trim()
        val apiKey = etApiKey.text.toString().trim()
        val apiSecret = etApiSecret.text.toString().trim()

        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        CloudinaryConfig.saveCredentials(this, cloudName, apiKey, apiSecret)
        Toast.makeText(this, "Cloudinary credentials saved successfully", Toast.LENGTH_SHORT).show()
        finishSetup()
    }

    private fun finishSetup() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
