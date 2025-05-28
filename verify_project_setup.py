#!/usr/bin/env python3
"""
Verify Firebase project setup and configuration
"""

import json
import os

def check_current_project():
    """Check current google-services.json project"""
    print("🔍 CHECKING CURRENT PROJECT CONFIGURATION")
    print("=" * 60)
    
    try:
        with open('./app/google-services.json', 'r') as f:
            config = json.load(f)
        
        project_info = config.get('project_info', {})
        current_project_id = project_info.get('project_id')
        current_project_number = project_info.get('project_number')
        
        print(f"📋 Current Project ID: {current_project_id}")
        print(f"📋 Current Project Number: {current_project_number}")
        
        return current_project_id
        
    except Exception as e:
        print(f"❌ Error reading google-services.json: {e}")
        return None

def check_service_account_project():
    """Check service account project from user input"""
    print("\n🔍 CHECKING SERVICE ACCOUNT PROJECT")
    print("=" * 60)
    
    # From user's service account JSON
    service_account_project = "contoh-85232"
    print(f"📋 Service Account Project: {service_account_project}")
    
    return service_account_project

def compare_projects(app_project, service_project):
    """Compare projects and show mismatch"""
    print(f"\n⚖️ PROJECT COMPARISON")
    print("=" * 60)
    
    print(f"📱 App Project:     {app_project}")
    print(f"🔑 Service Project: {service_project}")
    
    if app_project == service_project:
        print("✅ PROJECTS MATCH - Configuration is correct!")
        return True
    else:
        print("❌ PROJECT MISMATCH - This is the root cause of PERMISSION_DENIED!")
        return False

def show_fix_instructions(service_project):
    """Show instructions to fix project mismatch"""
    print(f"\n🔧 FIX INSTRUCTIONS")
    print("=" * 60)
    
    print(f"🎯 OPTION 1: Update app to use project '{service_project}' (RECOMMENDED)")
    print("   1. Go to: https://console.firebase.google.com/project/contoh-85232")
    print("   2. Project Settings → General → Your apps")
    print("   3. Add Android app:")
    print("      - Package name: com.example.elektronicarebeta1")
    print("      - App nickname: ElektroniCareBeta1")
    print("   4. Download google-services.json")
    print("   5. Replace app/google-services.json")
    print("   6. Rebuild app")
    print()
    
    print("🎯 OPTION 2: Setup rules in current app project")
    print("   1. Go to: https://console.firebase.google.com/project/elektronicare-4a4dc")
    print("   2. Create Firestore Database")
    print("   3. Apply super permissive rules")
    print()
    
    print("🚨 CRITICAL STEPS FOR OPTION 1:")
    print("   1. Ensure Firestore Database exists in contoh-85232")
    print("   2. Ensure Authentication is enabled in contoh-85232")
    print("   3. Apply these rules in contoh-85232:")
    print()
    print("   rules_version = '2';")
    print("   service cloud.firestore {")
    print("     match /databases/{database}/documents {")
    print("       match /{document=**} {")
    print("         allow read, write: if true;")
    print("       }")
    print("     }")
    print("   }")

def main():
    """Main verification function"""
    print("🚨 FIREBASE PROJECT SETUP VERIFICATION")
    print("=" * 70)
    
    # Check current app project
    app_project = check_current_project()
    
    # Check service account project
    service_project = check_service_account_project()
    
    # Compare projects
    if app_project and service_project:
        projects_match = compare_projects(app_project, service_project)
        
        if not projects_match:
            show_fix_instructions(service_project)
            
            print(f"\n🔗 QUICK LINKS:")
            print(f"   Current App Project: https://console.firebase.google.com/project/{app_project}")
            print(f"   Service Account Project: https://console.firebase.google.com/project/{service_project}")
            
            print(f"\n⚡ IMMEDIATE ACTION REQUIRED:")
            print("   1. Choose Option 1 or Option 2 above")
            print("   2. Follow the steps exactly")
            print("   3. Test app after changes")
        else:
            print("\n🎉 Configuration looks correct!")
            print("   If you still get PERMISSION_DENIED, check:")
            print("   1. Firestore Database is created")
            print("   2. Rules are published and propagated")
            print("   3. Authentication is properly configured")

if __name__ == "__main__":
    main()