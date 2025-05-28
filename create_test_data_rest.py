#!/usr/bin/env python3
"""
Test Data Creator for ElektroniCare (REST API Version)
Creates sample booking/repair data using Firestore REST API
No Firebase Admin SDK required
"""

import requests
import json
from datetime import datetime, timedelta
import random
import uuid

# Firebase project configuration
FIREBASE_PROJECT_ID = "your-project-id"  # Replace with your actual project ID
FIREBASE_API_KEY = "your-api-key"        # Replace with your Web API key

# Firestore REST API endpoints
FIRESTORE_BASE_URL = f"https://firestore.googleapis.com/v1/projects/{FIREBASE_PROJECT_ID}/databases/(default)/documents"

def create_firestore_document(collection_name, document_id, data):
    """Create a document in Firestore using REST API"""
    url = f"{FIRESTORE_BASE_URL}/{collection_name}/{document_id}"
    
    # Convert Python data to Firestore format
    firestore_data = convert_to_firestore_format(data)
    
    headers = {
        "Content-Type": "application/json"
    }
    
    try:
        response = requests.patch(url, json=firestore_data, headers=headers, params={"key": FIREBASE_API_KEY})
        
        if response.status_code in [200, 201]:
            print(f"✅ Created document: {collection_name}/{document_id}")
            return True
        else:
            print(f"❌ Error creating document: {response.status_code} - {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Exception creating document: {e}")
        return False

def convert_to_firestore_format(data):
    """Convert Python data to Firestore REST API format"""
    firestore_data = {"fields": {}}
    
    for key, value in data.items():
        if isinstance(value, str):
            firestore_data["fields"][key] = {"stringValue": value}
        elif isinstance(value, int):
            firestore_data["fields"][key] = {"integerValue": str(value)}
        elif isinstance(value, float):
            firestore_data["fields"][key] = {"doubleValue": value}
        elif isinstance(value, bool):
            firestore_data["fields"][key] = {"booleanValue": value}
        elif isinstance(value, datetime):
            firestore_data["fields"][key] = {"timestampValue": value.isoformat() + "Z"}
        elif value is None:
            firestore_data["fields"][key] = {"nullValue": None}
        elif isinstance(value, dict):
            firestore_data["fields"][key] = {"mapValue": convert_to_firestore_format(value)}
        elif isinstance(value, list):
            array_values = []
            for item in value:
                if isinstance(item, str):
                    array_values.append({"stringValue": item})
                elif isinstance(item, int):
                    array_values.append({"integerValue": str(item)})
                # Add more types as needed
            firestore_data["fields"][key] = {"arrayValue": {"values": array_values}}
    
    return firestore_data

def create_test_bookings():
    """Create test booking data"""
    USER_ID = "1rHqzlFFdId0eaWqBohXIs4ex9u2"
    
    # Sample data
    services = [
        {"id": "smartphone_repair", "name": "Smartphone Repair", "price": 150000},
        {"id": "laptop_repair", "name": "Laptop Repair", "price": 300000},
        {"id": "tablet_repair", "name": "Tablet Repair", "price": 200000},
        {"id": "smartwatch_repair", "name": "Smartwatch Repair", "price": 100000},
        {"id": "headphone_repair", "name": "Headphone Repair", "price": 75000},
    ]
    
    issues = [
        {
            "description": "Screen cracked after dropping the device. Touch functionality partially working.",
            "status": "completed",
            "notes": "Replaced LCD screen and digitizer. Device fully functional."
        },
        {
            "description": "Battery drains very quickly, device shuts down unexpectedly.",
            "status": "in_progress",
            "notes": "Battery replacement in progress. Estimated completion: 2 days."
        },
        {
            "description": "Device won't turn on, no response to power button.",
            "status": "pending_confirmation",
            "notes": "Initial diagnosis pending. Will contact customer for confirmation."
        },
        {
            "description": "Charging port loose, cable keeps disconnecting.",
            "status": "completed",
            "notes": "Replaced charging port assembly. Tested with multiple cables."
        },
        {
            "description": "Audio jack not working, no sound through headphones.",
            "status": "completed",
            "notes": "Cleaned audio jack and replaced internal connector."
        }
    ]
    
    created_count = 0
    
    for i, issue in enumerate(issues):
        service = services[i]
        
        # Generate dates
        created_date = datetime.now() - timedelta(days=random.randint(1, 30))
        appointment_date = created_date + timedelta(days=random.randint(1, 7))
        
        # Generate booking ID
        booking_id = f"EC{datetime.now().strftime('%Y%m')}{str(uuid.uuid4())[:8].upper()}"
        
        # Create booking data
        booking_data = {
            "userId": USER_ID,
            "serviceId": service["id"],
            "serviceName": service["name"],
            "issueDescription": issue["description"],
            "status": issue["status"],
            "estimatedCost": service["price"],
            "appointmentTimestamp": appointment_date,
            "createdAt": created_date,
            "updatedAt": datetime.now(),
            "location": "ElektroniCare Service Center",
            "technicianEmail": "agusseptiawanasep@gmail.com",
            "technicianName": "Agus Septiawan",
            "technicianNotes": issue["notes"],
            "paymentStatus": "paid" if issue["status"] == "completed" else "pending"
        }
        
        # Add completion data for completed bookings
        if issue["status"] == "completed":
            booking_data["completedAt"] = appointment_date + timedelta(days=random.randint(1, 3))
            booking_data["actualCost"] = service["price"]
            booking_data["customerRating"] = random.randint(4, 5)
            booking_data["warrantyExpiry"] = booking_data["completedAt"] + timedelta(days=30)
        
        # Create document
        if create_firestore_document("repairs", booking_id, booking_data):
            created_count += 1
    
    return created_count

def create_user_profile():
    """Create user profile"""
    USER_ID = "1rHqzlFFdId0eaWqBohXIs4ex9u2"
    
    user_data = {
        "userId": USER_ID,
        "fullName": "John Doe",
        "email": "john.doe@example.com",
        "phone": "+6281234567890",
        "address": "Jl. Teknologi No. 123, Jakarta Selatan",
        "joinDate": datetime.now() - timedelta(days=120),
        "lastLogin": datetime.now(),
        "totalBookings": 5,
        "completedBookings": 3,
        "averageRating": 4.7,
        "loyaltyPoints": 350
    }
    
    return create_firestore_document("users", USER_ID, user_data)

def main():
    """Main function"""
    print("🚀 Creating test data for ElektroniCare (REST API)...")
    print("=" * 60)
    
    # Check configuration
    if FIREBASE_PROJECT_ID == "your-project-id" or FIREBASE_API_KEY == "your-api-key":
        print("❌ Please update FIREBASE_PROJECT_ID and FIREBASE_API_KEY in the script")
        print("\n📋 To get these values:")
        print("1. Go to Firebase Console > Project Settings")
        print("2. Copy Project ID")
        print("3. Go to Web API Key and copy the key")
        return
    
    try:
        # Create user profile
        print("\n👤 Creating user profile...")
        if create_user_profile():
            print("✅ User profile created successfully")
        
        # Create bookings
        print("\n📅 Creating booking history...")
        created_bookings = create_test_bookings()
        
        print("\n" + "=" * 60)
        print("✅ TEST DATA CREATION COMPLETED!")
        print(f"📊 Created {created_bookings} bookings")
        print("👤 Updated user profile")
        
        print("\n📱 Test with user ID: 1rHqzlFFdId0eaWqBohXIs4ex9u2")
        
    except Exception as e:
        print(f"\n❌ Error: {e}")

if __name__ == "__main__":
    main()