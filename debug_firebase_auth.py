#!/usr/bin/env python3
"""
Debug script to check Firebase Authentication and Firestore permissions
"""

import firebase_admin
from firebase_admin import credentials, auth, firestore
import json

def debug_firebase():
    """Debug Firebase authentication and permissions"""
    
    print("🔍 FIREBASE DEBUG SCRIPT")
    print("=" * 50)
    
    # Initialize Firebase Admin SDK
    try:
        if not firebase_admin._apps:
            # Use default credentials or service account
            firebase_admin.initialize_app()
        
        # Get Firestore client
        db = firestore.client()
        
        print("✅ Firebase Admin SDK initialized successfully")
        
        # Test user ID from logs
        user_id = "SNTZpfi0QcczNxWIJ8UHklYsYwy1"
        
        print(f"\n🔍 Testing user: {user_id}")
        
        # Try to get user info
        try:
            user = auth.get_user(user_id)
            print(f"✅ User found in Authentication:")
            print(f"   Email: {user.email}")
            print(f"   UID: {user.uid}")
            print(f"   Email verified: {user.email_verified}")
            print(f"   Disabled: {user.disabled}")
        except Exception as e:
            print(f"❌ Error getting user from Authentication: {e}")
        
        # Try to read from Firestore
        try:
            doc_ref = db.collection('users').document(user_id)
            doc = doc_ref.get()
            if doc.exists:
                print(f"✅ User document exists in Firestore")
                print(f"   Data: {doc.to_dict()}")
            else:
                print(f"⚠️  User document does not exist in Firestore")
        except Exception as e:
            print(f"❌ Error reading from Firestore: {e}")
        
        # Try to write to Firestore
        try:
            test_data = {
                'test': True,
                'timestamp': firestore.SERVER_TIMESTAMP
            }
            db.collection('test').document('debug').set(test_data)
            print(f"✅ Write test successful")
        except Exception as e:
            print(f"❌ Error writing to Firestore: {e}")
            
    except Exception as e:
        print(f"❌ Error initializing Firebase: {e}")
    
    print("\n📋 NEXT STEPS:")
    print("1. Copy the super permissive rules to Firebase Console")
    print("2. Clear app data completely")
    print("3. Restart the app")
    print("4. If still failing, check network connectivity")

if __name__ == "__main__":
    debug_firebase()