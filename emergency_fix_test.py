#!/usr/bin/env python3
"""
Emergency Fix Test Script
"""

import subprocess
import time
import sys

def run_command(cmd, description=""):
    """Run command and return result"""
    print(f"🔧 {description}")
    print(f"   Command: {cmd}")
    try:
        result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
        if result.returncode == 0:
            print(f"   ✅ Success")
            if result.stdout.strip():
                print(f"   Output: {result.stdout.strip()}")
        else:
            print(f"   ❌ Failed: {result.stderr.strip()}")
        return result.returncode == 0
    except Exception as e:
        print(f"   ❌ Error: {e}")
        return False

def main():
    print("🚨 EMERGENCY AUTHENTICATION FIX")
    print("=" * 50)
    
    # Step 1: Clear app data completely
    print("\n📱 STEP 1: CLEARING APP DATA")
    run_command("adb shell pm clear com.example.elektronicarebeta1", "Clear app data")
    
    # Step 2: Kill app process
    print("\n🔄 STEP 2: KILLING APP PROCESS")
    run_command("adb shell am force-stop com.example.elektronicarebeta1", "Force stop app")
    
    # Step 3: Clear shared preferences
    print("\n🗑️ STEP 3: CLEARING SHARED PREFERENCES")
    run_command("adb shell rm -rf /data/data/com.example.elektronicarebeta1/shared_prefs/", "Clear shared prefs")
    
    # Step 4: Build and install fresh
    print("\n🔨 STEP 4: BUILDING FRESH APK")
    run_command("./gradlew clean", "Clean project")
    run_command("./gradlew assembleDebug", "Build debug APK")
    run_command("adb install -r app/build/outputs/apk/debug/app-debug.apk", "Install fresh APK")
    
    # Step 5: Start app
    print("\n🚀 STEP 5: STARTING APP")
    run_command("adb shell am start -n com.example.elektronicarebeta1/.SplashActivity", "Start app")
    
    print("\n" + "=" * 50)
    print("🎯 EXPECTED BEHAVIOR:")
    print("1. App should show Welcome/Onboarding screen (NOT dashboard)")
    print("2. User must login with email/password")
    print("3. After login, dashboard should work without PERMISSION_DENIED")
    print("\n📋 NEXT STEPS:")
    print("1. Check if app shows login screen")
    print("2. If still goes to dashboard, check Firebase Console:")
    print("   https://console.firebase.google.com/project/elektronicare-4a4dc/firestore")
    print("3. Ensure Firestore Database is created")
    print("4. Apply super permissive rules")

if __name__ == "__main__":
    main()