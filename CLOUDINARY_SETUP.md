# Cloudinary Setup Guide for ElektroniCare

## Overview
This guide explains how to set up Cloudinary for image upload functionality in the ElektroniCare Android app.

## Prerequisites
1. Cloudinary account (free tier available)
2. Android Studio
3. Internet connection for uploads

## Step 1: Create Cloudinary Account
1. Go to [Cloudinary](https://cloudinary.com/)
2. Sign up for a free account
3. Note down your credentials from the dashboard:
   - Cloud Name
   - API Key
   - API Secret

## Step 2: Configure Cloudinary Credentials
1. Open `CloudinaryConfig.kt`
2. Replace the placeholder values:
   ```kotlin
   const val CLOUD_NAME = "your_actual_cloud_name"
   const val API_KEY = "your_actual_api_key"
   const val API_SECRET = "your_actual_api_secret"
   ```

## Step 3: Set Up Upload Presets (Optional but Recommended)
1. Go to Cloudinary Dashboard > Settings > Upload
2. Create upload presets:
   - **Profile Images Preset**: `profile_images`
     - Mode: Unsigned
     - Folder: `profile_images`
     - Transformations: Auto quality, Auto format
     - Max file size: 5MB
   
   - **Repair Images Preset**: `repair_images`
     - Mode: Unsigned
     - Folder: `repair_images`
     - Transformations: Auto quality, Auto format
     - Max file size: 10MB

## Step 4: Test Configuration
Run the verification script to test your setup:
```bash
python3 verify_cloudinary_setup.py
```

## Features Implemented

### Image Upload
- Profile image upload with optimization
- Repair image upload with progress tracking
- Automatic image transformation (quality, format)
- Error handling and retry logic

### Image Management
- Automatic file naming with user ID
- Folder organization (profile_images/, repair_images/)
- Old image cleanup when updating
- URL generation for different sizes

### Security
- Secure credential management
- Upload validation
- File type restrictions
- Size limitations

## Usage in Code

### Upload Profile Image
```kotlin
val imageUrl = CloudinaryManager.uploadProfileImage(imageUri, userId)
```

### Upload Repair Image
```kotlin
val imageUrl = CloudinaryManager.uploadRepairImage(imageUri, repairId)
```

### Get Optimized Image URL
```kotlin
val optimizedUrl = CloudinaryManager.getOptimizedImageUrl(publicId, width, height)
```

## Troubleshooting

### Common Issues
1. **Upload fails**: Check internet connection and credentials
2. **Invalid credentials**: Verify Cloud Name, API Key, and API Secret
3. **File too large**: Check file size limits in upload presets
4. **Network timeout**: Increase timeout in CloudinaryManager

### Debug Mode
Enable debug logging in CloudinaryManager:
```kotlin
private const val DEBUG = true
```

## Migration from Firebase Storage
The app has been migrated from Firebase Storage to Cloudinary:
- ✅ Removed Firebase Storage dependencies
- ✅ Updated ProfileActivity to use CloudinaryManager
- ✅ Removed Firebase Storage methods from FirebaseManager
- ✅ Added Cloudinary initialization in Application class

## Cost Considerations
- Free tier: 25 GB storage, 25 GB bandwidth/month
- Paid plans available for higher usage
- Monitor usage in Cloudinary dashboard

## Support
For issues with Cloudinary setup:
1. Check Cloudinary documentation
2. Verify credentials and network connectivity
3. Review app logs for error details