#!/usr/bin/env python3
"""
Comprehensive Firebase Issue Diagnosis Script
"""

import json
import os
import subprocess
import sys

def check_file_exists(filepath, description):
    """Check if a file exists and print status"""
    if os.path.exists(filepath):
        print(f"✅ {description}: EXISTS")
        return True
    else:
        print(f"❌ {description}: MISSING")
        return False

def check_google_services_json():
    """Check google-services.json configuration"""
    print("\n🔍 CHECKING GOOGLE-SERVICES.JSON")
    print("=" * 50)
    
    filepath = "./app/google-services.json"
    if not check_file_exists(filepath, "google-services.json"):
        return False
    
    try:
        with open(filepath, 'r') as f:
            config = json.load(f)
        
        project_id = config.get('project_info', {}).get('project_id')
        print(f"📋 Project ID: {project_id}")
        
        if project_id != "elektronicare-4a4dc":
            print(f"⚠️  WARNING: Project ID mismatch!")
            print(f"   Expected: elektronicare-4a4dc")
            print(f"   Found: {project_id}")
        
        # Check client info
        clients = config.get('client', [])
        if clients:
            client = clients[0]
            package_name = client.get('client_info', {}).get('android_client_info', {}).get('package_name')
            print(f"📱 Package Name: {package_name}")
            
            if package_name != "com.example.elektronicarebeta1":
                print(f"⚠️  WARNING: Package name mismatch!")
        
        return True
        
    except Exception as e:
        print(f"❌ Error reading google-services.json: {e}")
        return False

def check_firebase_dependencies():
    """Check Firebase dependencies in build.gradle"""
    print("\n🔍 CHECKING FIREBASE DEPENDENCIES")
    print("=" * 50)
    
    build_gradle_path = "./app/build.gradle"
    if not check_file_exists(build_gradle_path, "app/build.gradle"):
        return False
    
    try:
        with open(build_gradle_path, 'r') as f:
            content = f.read()
        
        firebase_deps = [
            'firebase-auth',
            'firebase-firestore',
            'firebase-storage',
            'google-services'
        ]
        
        for dep in firebase_deps:
            if dep in content:
                print(f"✅ {dep}: FOUND")
            else:
                print(f"❌ {dep}: MISSING")
        
        return True
        
    except Exception as e:
        print(f"❌ Error reading build.gradle: {e}")
        return False

def check_network_connectivity():
    """Check network connectivity"""
    print("\n🔍 CHECKING NETWORK CONNECTIVITY")
    print("=" * 50)
    
    try:
        # Check if we can reach Google
        result = subprocess.run(['ping', '-c', '1', 'google.com'], 
                              capture_output=True, text=True, timeout=5)
        if result.returncode == 0:
            print("✅ Internet connectivity: OK")
        else:
            print("❌ Internet connectivity: FAILED")
            
        # Check if we can reach Firebase
        result = subprocess.run(['ping', '-c', '1', 'firestore.googleapis.com'], 
                              capture_output=True, text=True, timeout=5)
        if result.returncode == 0:
            print("✅ Firebase connectivity: OK")
        else:
            print("❌ Firebase connectivity: FAILED")
            
    except Exception as e:
        print(f"⚠️  Network check failed: {e}")

def generate_fix_recommendations():
    """Generate fix recommendations based on diagnosis"""
    print("\n🔧 FIX RECOMMENDATIONS")
    print("=" * 50)
    
    print("1. 🔥 VERIFY FIRESTORE DATABASE:")
    print("   • Go to Firebase Console")
    print("   • Check if Firestore Database is created and active")
    print("   • If not, create database in 'test mode'")
    
    print("\n2. 📱 RE-DOWNLOAD GOOGLE-SERVICES.JSON:")
    print("   • Go to Firebase Console > Project Settings")
    print("   • Download fresh google-services.json")
    print("   • Replace app/google-services.json")
    
    print("\n3. 🧹 CLEAN BUILD:")
    print("   • ./gradlew clean")
    print("   • ./gradlew build")
    print("   • Clear app data on device")
    
    print("\n4. 🔄 RESTART EVERYTHING:")
    print("   • Restart Android emulator")
    print("   • Reinstall app")
    print("   • Test again")
    
    print("\n5. 🆘 IF ALL FAILS:")
    print("   • Create new Firebase project")
    print("   • Setup from scratch")

def main():
    """Main diagnosis function"""
    print("🚨 FIREBASE ISSUE DIAGNOSIS")
    print("=" * 60)
    
    # Change to project directory
    if os.path.exists('./app'):
        os.chdir('.')
    else:
        print("❌ Not in project root directory!")
        sys.exit(1)
    
    # Run checks
    check_google_services_json()
    check_firebase_dependencies()
    check_network_connectivity()
    
    # Generate recommendations
    generate_fix_recommendations()
    
    print("\n" + "=" * 60)
    print("🏁 DIAGNOSIS COMPLETE")

if __name__ == "__main__":
    main()