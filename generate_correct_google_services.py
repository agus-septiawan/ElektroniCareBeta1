#!/usr/bin/env python3
"""
Generate correct google-services.json for project contoh-85232
"""

import json

def generate_google_services_json():
    """Generate google-services.json for contoh-85232 project"""
    
    # Template google-services.json for contoh-85232
    google_services = {
        "project_info": {
            "project_number": "YOUR_PROJECT_NUMBER_HERE",  # Need to get from Firebase Console
            "project_id": "contoh-85232",
            "storage_bucket": "contoh-85232.firebasestorage.app"
        },
        "client": [
            {
                "client_info": {
                    "mobilesdk_app_id": "1:YOUR_PROJECT_NUMBER:android:YOUR_APP_ID",  # Need from Firebase Console
                    "android_client_info": {
                        "package_name": "com.example.elektronicarebeta1"
                    }
                },
                "oauth_client": [],
                "api_key": [
                    {
                        "current_key": "YOUR_API_KEY_HERE"  # Need from Firebase Console
                    }
                ],
                "services": {
                    "appinvite_service": {
                        "other_platform_oauth_client": []
                    }
                }
            }
        ],
        "configuration_version": "1"
    }
    
    return google_services

def main():
    print("🔧 GOOGLE-SERVICES.JSON GENERATOR FOR PROJECT: contoh-85232")
    print("=" * 70)
    
    print("📋 CURRENT PROJECT MISMATCH:")
    print("   App uses: elektronicare-4a4dc")
    print("   Your rules in: contoh-85232")
    print()
    
    print("🎯 TO FIX THIS, YOU NEED TO:")
    print("1. Go to Firebase Console: https://console.firebase.google.com/project/contoh-85232")
    print("2. Project Settings → General → Your apps")
    print("3. Add Android app if not exists:")
    print("   - Package name: com.example.elektronicarebeta1")
    print("   - App nickname: ElektroniCareBeta1")
    print("4. Download google-services.json")
    print("5. Replace app/google-services.json with downloaded file")
    print()
    
    print("🔗 DIRECT LINKS:")
    print("   Project Console: https://console.firebase.google.com/project/contoh-85232")
    print("   Project Settings: https://console.firebase.google.com/project/contoh-85232/settings/general")
    print("   Firestore: https://console.firebase.google.com/project/contoh-85232/firestore")
    print("   Firestore Rules: https://console.firebase.google.com/project/contoh-85232/firestore/rules")
    print()
    
    print("⚠️ IMPORTANT STEPS:")
    print("1. Ensure Firestore Database is created in contoh-85232")
    print("2. Apply super permissive rules in contoh-85232")
    print("3. Download correct google-services.json")
    print("4. Replace app/google-services.json")
    print("5. Rebuild app")
    print()
    
    # Generate template
    template = generate_google_services_json()
    
    print("📄 TEMPLATE google-services.json:")
    print("   (You still need to download the real one from Firebase Console)")
    print()
    print(json.dumps(template, indent=2))
    
    # Save template
    with open('google-services-template.json', 'w') as f:
        json.dump(template, f, indent=2)
    
    print()
    print("✅ Template saved as: google-services-template.json")
    print("🚨 REMEMBER: Download the REAL file from Firebase Console!")

if __name__ == "__main__":
    main()