#!/usr/bin/env python3
"""
Check Firestore Database Status using Firebase Admin SDK
"""

import json
import os

def check_google_services_config():
    """Extract project info from google-services.json"""
    print("🔍 CHECKING GOOGLE-SERVICES.JSON CONFIGURATION")
    print("=" * 60)
    
    try:
        with open('./app/google-services.json', 'r') as f:
            config = json.load(f)
        
        project_info = config.get('project_info', {})
        project_id = project_info.get('project_id')
        project_number = project_info.get('project_number')
        
        print(f"📋 Project ID: {project_id}")
        print(f"📋 Project Number: {project_number}")
        
        # Check client configuration
        clients = config.get('client', [])
        if clients:
            client = clients[0]
            client_info = client.get('client_info', {})
            android_info = client_info.get('android_client_info', {})
            
            package_name = android_info.get('package_name')
            print(f"📱 Package Name: {package_name}")
            
            # Check API keys
            api_keys = client.get('api_key', [])
            if api_keys:
                print(f"🔑 API Keys: {len(api_keys)} found")
            
            # Check OAuth client
            oauth_client = client.get('oauth_client', [])
            if oauth_client:
                print(f"🔐 OAuth Clients: {len(oauth_client)} found")
        
        return project_id, project_number
        
    except FileNotFoundError:
        print("❌ google-services.json not found!")
        return None, None
    except json.JSONDecodeError:
        print("❌ Invalid JSON in google-services.json!")
        return None, None
    except Exception as e:
        print(f"❌ Error reading google-services.json: {e}")
        return None, None

def generate_firebase_console_urls(project_id):
    """Generate Firebase Console URLs for easy access"""
    if not project_id:
        return
    
    print(f"\n🔗 FIREBASE CONSOLE LINKS FOR PROJECT: {project_id}")
    print("=" * 60)
    
    base_url = f"https://console.firebase.google.com/project/{project_id}"
    
    urls = {
        "Project Overview": f"{base_url}/overview",
        "Firestore Database": f"{base_url}/firestore",
        "Firestore Rules": f"{base_url}/firestore/rules",
        "Authentication": f"{base_url}/authentication",
        "Project Settings": f"{base_url}/settings/general",
        "Usage and Billing": f"{base_url}/usage"
    }
    
    for name, url in urls.items():
        print(f"📎 {name}:")
        print(f"   {url}")
        print()

def check_firestore_setup_steps():
    """Provide step-by-step Firestore setup verification"""
    print("🔥 FIRESTORE SETUP VERIFICATION CHECKLIST")
    print("=" * 60)
    
    steps = [
        {
            "step": "1. Firestore Database Creation",
            "description": "Database must be created in Firebase Console",
            "action": "Go to Firestore Database → Create database → Test mode"
        },
        {
            "step": "2. Security Rules Configuration", 
            "description": "Rules must allow read/write operations",
            "action": "Go to Firestore Rules → Apply super permissive rules"
        },
        {
            "step": "3. Rules Propagation",
            "description": "Rules need time to propagate (2-3 minutes)",
            "action": "Wait after publishing rules before testing"
        },
        {
            "step": "4. Authentication Setup",
            "description": "Firebase Auth must be configured",
            "action": "Go to Authentication → Sign-in method → Enable Email/Password"
        },
        {
            "step": "5. App Configuration",
            "description": "google-services.json must be correct",
            "action": "Download fresh google-services.json from Project Settings"
        }
    ]
    
    for step_info in steps:
        print(f"✅ {step_info['step']}")
        print(f"   📝 {step_info['description']}")
        print(f"   🔧 {step_info['action']}")
        print()

def generate_super_permissive_rules():
    """Generate super permissive Firestore rules for debugging"""
    print("🔥 SUPER PERMISSIVE FIRESTORE RULES (FOR DEBUGGING)")
    print("=" * 60)
    
    rules = '''rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // SUPER PERMISSIVE - ALLOWS ALL OPERATIONS
    // ⚠️ WARNING: Only use for debugging!
    match /{document=**} {
      allow read, write: if true;
    }
  }
}'''
    
    print("📋 Copy these rules to Firebase Console > Firestore > Rules:")
    print()
    print(rules)
    print()
    print("🚨 IMPORTANT:")
    print("   1. Copy rules above to Firebase Console")
    print("   2. Click 'Publish' button")
    print("   3. Wait 2-3 minutes for propagation")
    print("   4. Test app again")

def main():
    """Main function"""
    print("🚨 FIRESTORE DATABASE STATUS CHECK")
    print("=" * 70)
    
    # Check google-services.json
    project_id, project_number = check_google_services_config()
    
    # Generate console URLs
    if project_id:
        generate_firebase_console_urls(project_id)
    
    # Show setup verification steps
    check_firestore_setup_steps()
    
    # Show super permissive rules
    generate_super_permissive_rules()
    
    print("=" * 70)
    print("🎯 NEXT STEPS:")
    print("1. Open Firestore Database link above")
    print("2. Verify database is created and active")
    print("3. Apply super permissive rules")
    print("4. Wait 2-3 minutes")
    print("5. Test app again")

if __name__ == "__main__":
    main()