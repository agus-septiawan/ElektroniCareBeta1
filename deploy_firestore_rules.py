#!/usr/bin/env python3
"""
Script to deploy Firestore security rules using Firebase Admin SDK
"""

import firebase_admin
from firebase_admin import credentials, firestore
import json
import os

def deploy_firestore_rules():
    """Display Firestore security rules for manual deployment"""
    
    # Read the rules file
    with open('firestore.rules', 'r') as f:
        rules_content = f.read()
    
    print("🔥 FIRESTORE SECURITY RULES 🔥")
    print("=" * 60)
    print(rules_content)
    print("=" * 60)
    
    print("\n📋 DEPLOYMENT OPTIONS:")
    print("\n1️⃣ FIREBASE CONSOLE (RECOMMENDED - FASTEST):")
    print("   • Go to: https://console.firebase.google.com/")
    print("   • Select your project")
    print("   • Click 'Firestore Database' → 'Rules' tab")
    print("   • Copy-paste the rules above")
    print("   • Click 'Publish'")
    
    print("\n2️⃣ FIREBASE CLI:")
    print("   • npm install -g firebase-tools")
    print("   • firebase login")
    print("   • firebase init firestore")
    print("   • firebase deploy --only firestore:rules")
    
    print("\n⚠️  IMPORTANT:")
    print("   These are DEVELOPMENT rules - all authenticated users can read/write")
    print("   For production, use more restrictive rules!")
    
    print("\n✅ After deployment:")
    print("   • Restart your Android app")
    print("   • Try logging in again")
    print("   • The PERMISSION_DENIED error should be resolved")

if __name__ == "__main__":
    deploy_firestore_rules()