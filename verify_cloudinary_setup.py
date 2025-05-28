#!/usr/bin/env python3
"""
Cloudinary Setup Verification Script for ElektroniCare
Verifies that Cloudinary is properly configured and accessible.
"""

import requests
import json
import base64
import hashlib
import time
from urllib.parse import urlencode

# Cloudinary credentials (replace with your actual values)
CLOUD_NAME = "your_cloud_name"
API_KEY = "your_api_key"
API_SECRET = "your_api_secret"

def check_credentials():
    """Check if credentials are properly configured"""
    print("🔍 Checking Cloudinary credentials...")
    
    if CLOUD_NAME == "your_cloud_name":
        print("❌ Cloud Name not configured")
        return False
    
    if API_KEY == "your_api_key":
        print("❌ API Key not configured")
        return False
        
    if API_SECRET == "your_api_secret":
        print("❌ API Secret not configured")
        return False
    
    print("✅ Credentials configured")
    return True

def test_api_connection():
    """Test connection to Cloudinary API"""
    print("\n🌐 Testing API connection...")
    
    try:
        # Test with a simple API call to get account details
        timestamp = str(int(time.time()))
        params = {
            'timestamp': timestamp,
            'api_key': API_KEY
        }
        
        # Create signature
        params_string = '&'.join([f'{k}={v}' for k, v in sorted(params.items())])
        signature_string = params_string + API_SECRET
        signature = hashlib.sha1(signature_string.encode()).hexdigest()
        
        params['signature'] = signature
        
        url = f"https://api.cloudinary.com/v1_1/{CLOUD_NAME}/resources/image"
        response = requests.get(url, params=params, timeout=10)
        
        if response.status_code == 200:
            print("✅ API connection successful")
            data = response.json()
            print(f"   Account has {len(data.get('resources', []))} images")
            return True
        else:
            print(f"❌ API connection failed: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Network error: {e}")
        return False
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return False

def check_upload_presets():
    """Check if upload presets exist"""
    print("\n📋 Checking upload presets...")
    
    try:
        # Get upload presets
        timestamp = str(int(time.time()))
        params = {
            'timestamp': timestamp,
            'api_key': API_KEY
        }
        
        params_string = '&'.join([f'{k}={v}' for k, v in sorted(params.items())])
        signature_string = params_string + API_SECRET
        signature = hashlib.sha1(signature_string.encode()).hexdigest()
        params['signature'] = signature
        
        url = f"https://api.cloudinary.com/v1_1/{CLOUD_NAME}/upload_presets"
        response = requests.get(url, params=params, timeout=10)
        
        if response.status_code == 200:
            presets = response.json()
            preset_names = [preset['name'] for preset in presets.get('presets', [])]
            
            # Check for our specific presets
            profile_preset = 'profile_images' in preset_names
            repair_preset = 'repair_images' in preset_names
            
            if profile_preset:
                print("✅ Profile images preset found")
            else:
                print("⚠️  Profile images preset not found (optional)")
                
            if repair_preset:
                print("✅ Repair images preset found")
            else:
                print("⚠️  Repair images preset not found (optional)")
                
            print(f"   Total presets: {len(preset_names)}")
            return True
        else:
            print(f"❌ Failed to get upload presets: {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ Error checking presets: {e}")
        return False

def test_unsigned_upload():
    """Test unsigned upload capability"""
    print("\n📤 Testing unsigned upload...")
    
    try:
        # Create a simple test image data (1x1 pixel PNG)
        test_image_data = base64.b64decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAGA4nEKtAAAAABJRU5ErkJggg=="
        )
        
        url = f"https://api.cloudinary.com/v1_1/{CLOUD_NAME}/image/upload"
        
        files = {'file': ('test.png', test_image_data, 'image/png')}
        data = {
            'upload_preset': 'ml_default',  # Default unsigned preset
            'folder': 'test_uploads',
            'public_id': f'test_{int(time.time())}'
        }
        
        response = requests.post(url, files=files, data=data, timeout=30)
        
        if response.status_code == 200:
            result = response.json()
            print("✅ Unsigned upload successful")
            print(f"   Image URL: {result.get('secure_url', 'N/A')}")
            
            # Clean up test image
            cleanup_test_image(result.get('public_id'))
            return True
        else:
            print(f"❌ Upload failed: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Upload error: {e}")
        return False

def cleanup_test_image(public_id):
    """Clean up test image"""
    if not public_id:
        return
        
    try:
        timestamp = str(int(time.time()))
        params = {
            'public_id': public_id,
            'timestamp': timestamp,
            'api_key': API_KEY
        }
        
        params_string = '&'.join([f'{k}={v}' for k, v in sorted(params.items())])
        signature_string = params_string + API_SECRET
        signature = hashlib.sha1(signature_string.encode()).hexdigest()
        params['signature'] = signature
        
        url = f"https://api.cloudinary.com/v1_1/{CLOUD_NAME}/image/destroy"
        response = requests.post(url, data=params, timeout=10)
        
        if response.status_code == 200:
            print("   Test image cleaned up")
        
    except Exception as e:
        print(f"   Warning: Could not clean up test image: {e}")

def check_android_config():
    """Check Android configuration files"""
    print("\n📱 Checking Android configuration...")
    
    try:
        # Check CloudinaryConfig.kt
        with open('app/src/main/java/com/example/elektronicarebeta1/cloudinary/CloudinaryConfig.kt', 'r') as f:
            config_content = f.read()
            
        if 'your_cloud_name' in config_content:
            print("❌ CloudinaryConfig.kt still has placeholder values")
            return False
        else:
            print("✅ CloudinaryConfig.kt appears configured")
            
        # Check if CloudinaryManager exists
        try:
            with open('app/src/main/java/com/example/elektronicarebeta1/cloudinary/CloudinaryManager.kt', 'r') as f:
                manager_content = f.read()
            print("✅ CloudinaryManager.kt found")
        except FileNotFoundError:
            print("❌ CloudinaryManager.kt not found")
            return False
            
        # Check Application class
        try:
            with open('app/src/main/java/com/example/elektronicarebeta1/ElektroniCareApplication.kt', 'r') as f:
                app_content = f.read()
            if 'CloudinaryManager.initialize' in app_content:
                print("✅ Application class configured for Cloudinary")
            else:
                print("❌ Application class not configured for Cloudinary")
                return False
        except FileNotFoundError:
            print("❌ ElektroniCareApplication.kt not found")
            return False
            
        return True
        
    except Exception as e:
        print(f"❌ Error checking Android config: {e}")
        return False

def main():
    """Main verification function"""
    print("🚀 Cloudinary Setup Verification for ElektroniCare")
    print("=" * 50)
    
    all_checks_passed = True
    
    # Run all checks
    checks = [
        check_credentials,
        check_android_config,
        test_api_connection,
        check_upload_presets,
        test_unsigned_upload
    ]
    
    for check in checks:
        if not check():
            all_checks_passed = False
    
    print("\n" + "=" * 50)
    if all_checks_passed:
        print("🎉 All checks passed! Cloudinary is ready to use.")
        print("\nNext steps:")
        print("1. Build and run your Android app")
        print("2. Test profile image upload")
        print("3. Monitor usage in Cloudinary dashboard")
    else:
        print("❌ Some checks failed. Please review the issues above.")
        print("\nCommon solutions:")
        print("1. Update CloudinaryConfig.kt with your actual credentials")
        print("2. Check internet connection")
        print("3. Verify Cloudinary account is active")
        print("4. Create upload presets in Cloudinary dashboard")

if __name__ == "__main__":
    main()