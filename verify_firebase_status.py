#!/usr/bin/env python3
"""
Real-time Firebase Status Verification Script
"""

import json
import subprocess
import time
import re

def run_adb_command(command):
    """Run ADB command and return output"""
    try:
        result = subprocess.run(f"adb {command}", shell=True, 
                              capture_output=True, text=True, timeout=10)
        return result.stdout, result.stderr, result.returncode
    except Exception as e:
        return "", str(e), 1

def check_firebase_logs():
    """Check Firebase-related logs from the app"""
    print("🔍 CHECKING FIREBASE LOGS...")
    print("=" * 50)
    
    # Clear logcat first
    run_adb_command("logcat -c")
    
    print("📱 Starting app and monitoring logs...")
    print("   (This will monitor for 30 seconds)")
    
    # Start monitoring logs
    stdout, stderr, returncode = run_adb_command("logcat -s FirebaseManager:* FirebaseAuth:* Firestore:* FirebaseDataSeeder:* -v time")
    
    if returncode == 0:
        lines = stdout.split('\n')
        firebase_logs = []
        
        for line in lines:
            if any(tag in line for tag in ['FirebaseManager', 'FirebaseAuth', 'Firestore', 'FirebaseDataSeeder']):
                firebase_logs.append(line.strip())
        
        if firebase_logs:
            print("📋 FIREBASE LOGS FOUND:")
            for log in firebase_logs[-20:]:  # Show last 20 logs
                if "PERMISSION_DENIED" in log:
                    print(f"❌ {log}")
                elif "Error" in log or "error" in log:
                    print(f"⚠️  {log}")
                else:
                    print(f"ℹ️  {log}")
        else:
            print("❌ No Firebase logs found")
    else:
        print(f"❌ Error getting logs: {stderr}")

def check_app_status():
    """Check if app is running and Firebase is initialized"""
    print("\n🔍 CHECKING APP STATUS...")
    print("=" * 50)
    
    # Check if app is running
    stdout, stderr, returncode = run_adb_command("shell ps | grep elektronicarebeta1")
    
    if returncode == 0 and stdout.strip():
        print("✅ App is running")
        
        # Check app info
        stdout, stderr, returncode = run_adb_command("shell dumpsys package com.example.elektronicarebeta1 | grep -A 5 'versionName'")
        if returncode == 0:
            print(f"📱 App info: {stdout.strip()}")
    else:
        print("❌ App is not running")
        print("💡 Try starting the app first")

def check_network_from_device():
    """Check network connectivity from Android device"""
    print("\n🔍 CHECKING DEVICE NETWORK...")
    print("=" * 50)
    
    # Check if device can reach internet
    stdout, stderr, returncode = run_adb_command("shell ping -c 1 8.8.8.8")
    
    if returncode == 0:
        print("✅ Device has internet connectivity")
    else:
        print("❌ Device has no internet connectivity")
        print("💡 Check emulator network settings")

def monitor_realtime_logs():
    """Monitor Firebase logs in real-time"""
    print("\n🔍 REAL-TIME FIREBASE LOG MONITORING")
    print("=" * 50)
    print("📱 Monitoring Firebase logs... (Press Ctrl+C to stop)")
    print("   Start using the app now to see logs")
    print()
    
    try:
        # Start real-time monitoring
        process = subprocess.Popen(
            ["adb", "logcat", "-s", "FirebaseManager:*", "FirebaseAuth:*", "Firestore:*", "FirebaseDataSeeder:*", "-v", "time"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1,
            universal_newlines=True
        )
        
        for line in iter(process.stdout.readline, ''):
            line = line.strip()
            if line:
                timestamp = time.strftime("%H:%M:%S")
                if "PERMISSION_DENIED" in line:
                    print(f"[{timestamp}] ❌ {line}")
                elif "Error" in line or "error" in line:
                    print(f"[{timestamp}] ⚠️  {line}")
                elif "success" in line.lower() or "Success" in line:
                    print(f"[{timestamp}] ✅ {line}")
                else:
                    print(f"[{timestamp}] ℹ️  {line}")
                    
    except KeyboardInterrupt:
        print("\n🛑 Monitoring stopped")
        process.terminate()
    except Exception as e:
        print(f"❌ Error monitoring logs: {e}")

def main():
    """Main function"""
    print("🚨 FIREBASE STATUS VERIFICATION")
    print("=" * 60)
    
    # Check basic status
    check_app_status()
    check_network_from_device()
    
    print("\n" + "=" * 60)
    print("Choose an option:")
    print("1. Check recent Firebase logs")
    print("2. Monitor real-time Firebase logs")
    print("3. Exit")
    
    choice = input("\nEnter choice (1-3): ").strip()
    
    if choice == "1":
        check_firebase_logs()
    elif choice == "2":
        monitor_realtime_logs()
    elif choice == "3":
        print("👋 Goodbye!")
    else:
        print("❌ Invalid choice")

if __name__ == "__main__":
    main()