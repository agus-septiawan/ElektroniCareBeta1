#!/usr/bin/env python3
"""
Script to verify Firebase Storage setup for ElektroniCare project
"""

import json
import os
import sys

def check_file_exists(file_path, description):
    """Check if a file exists"""
    if os.path.exists(file_path):
        print(f"✅ {description}: Found")
        return True
    else:
        print(f"❌ {description}: Missing")
        return False

def check_google_services_config():
    """Check google-services.json configuration"""
    config_path = "app/google-services.json"
    if not check_file_exists(config_path, "google-services.json"):
        return False
    
    try:
        with open(config_path, 'r') as f:
            config = json.load(f)
        
        # Check if storage bucket is configured
        storage_bucket = config.get('project_info', {}).get('storage_bucket')
        if storage_bucket:
            print(f"✅ Storage bucket configured: {storage_bucket}")
            return True
        else:
            print("❌ Storage bucket not found in google-services.json")
            return False
    except Exception as e:
        print(f"❌ Error reading google-services.json: {e}")
        return False

def check_firebase_config():
    """Check firebase.json configuration"""
    if not check_file_exists("firebase.json", "firebase.json"):
        return False
    
    try:
        with open("firebase.json", 'r') as f:
            config = json.load(f)
        
        if 'storage' in config:
            print("✅ Storage configuration found in firebase.json")
            storage_rules = config['storage'].get('rules')
            if storage_rules:
                print(f"✅ Storage rules file: {storage_rules}")
                return check_file_exists(storage_rules, "Storage rules file")
            else:
                print("❌ Storage rules not specified in firebase.json")
                return False
        else:
            print("❌ Storage configuration missing in firebase.json")
            return False
    except Exception as e:
        print(f"❌ Error reading firebase.json: {e}")
        return False

def check_android_permissions():
    """Check Android permissions in AndroidManifest.xml"""
    manifest_path = "app/src/main/AndroidManifest.xml"
    if not check_file_exists(manifest_path, "AndroidManifest.xml"):
        return False
    
    try:
        with open(manifest_path, 'r') as f:
            content = f.read()
        
        required_permissions = [
            "android.permission.INTERNET",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.READ_MEDIA_IMAGES"
        ]
        
        all_permissions_found = True
        for permission in required_permissions:
            if permission in content:
                print(f"✅ Permission found: {permission}")
            else:
                print(f"❌ Permission missing: {permission}")
                all_permissions_found = False
        
        return all_permissions_found
    except Exception as e:
        print(f"❌ Error reading AndroidManifest.xml: {e}")
        return False

def check_gradle_dependencies():
    """Check if Firebase Storage dependency is in build.gradle"""
    gradle_path = "app/build.gradle.kts"
    if not check_file_exists(gradle_path, "build.gradle.kts"):
        return False
    
    try:
        with open(gradle_path, 'r') as f:
            content = f.read()
        
        if "firebase-storage" in content:
            print("✅ Firebase Storage dependency found in build.gradle.kts")
            return True
        else:
            print("❌ Firebase Storage dependency missing in build.gradle.kts")
            return False
    except Exception as e:
        print(f"❌ Error reading build.gradle.kts: {e}")
        return False

def main():
    """Main verification function"""
    print("🔍 Verifying Firebase Storage Setup for ElektroniCare")
    print("=" * 60)
    
    checks = [
        ("Google Services Configuration", check_google_services_config),
        ("Firebase Configuration", check_firebase_config),
        ("Android Permissions", check_android_permissions),
        ("Gradle Dependencies", check_gradle_dependencies)
    ]
    
    all_passed = True
    for check_name, check_func in checks:
        print(f"\n📋 Checking {check_name}:")
        if not check_func():
            all_passed = False
    
    print("\n" + "=" * 60)
    if all_passed:
        print("🎉 All checks passed! Firebase Storage should be ready to use.")
        print("\n📋 Next steps:")
        print("1. Make sure Firebase Storage is enabled in Firebase Console")
        print("2. Deploy storage rules: python deploy_storage_rules.py")
        print("3. Test profile image upload in your app")
    else:
        print("❌ Some checks failed. Please fix the issues above.")
        print("\n🛠️ Common fixes:")
        print("1. Ensure Firebase Storage is enabled in Firebase Console")
        print("2. Add missing permissions to AndroidManifest.xml")
        print("3. Add firebase-storage dependency to build.gradle.kts")
        print("4. Configure storage rules in firebase.json")

if __name__ == "__main__":
    main()