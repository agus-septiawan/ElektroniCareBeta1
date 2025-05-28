#!/usr/bin/env python3
"""
Test Data Creator for ElektroniCare
Creates sample booking/repair data for user: 1rHqzlFFdId0eaWqBohXIs4ex9u2
"""

import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime, timedelta
import random
import uuid

def initialize_firebase():
    """Initialize Firebase Admin SDK"""
    try:
        # Try to get existing app
        app = firebase_admin.get_app()
        print("✅ Firebase already initialized")
    except ValueError:
        # Initialize new app
        cred = credentials.Certificate("path/to/serviceAccountKey.json")  # Update path
        firebase_admin.initialize_app(cred)
        print("✅ Firebase initialized successfully")
    
    return firestore.client()

def create_test_data():
    """Create comprehensive test data for user history"""
    
    # Target user ID
    USER_ID = "1rHqzlFFdId0eaWqBohXIs4ex9u2"
    
    # Initialize Firestore
    db = initialize_firebase()
    
    # Sample services data
    services = [
        {"id": "smartphone_repair", "name": "Smartphone Repair", "price": 150000},
        {"id": "laptop_repair", "name": "Laptop Repair", "price": 300000},
        {"id": "tablet_repair", "name": "Tablet Repair", "price": 200000},
        {"id": "smartwatch_repair", "name": "Smartwatch Repair", "price": 100000},
        {"id": "headphone_repair", "name": "Headphone Repair", "price": 75000},
        {"id": "gaming_console", "name": "Gaming Console Repair", "price": 250000},
    ]
    
    # Sample issues and descriptions
    issues = [
        {
            "description": "Screen cracked after dropping the device. Touch functionality partially working.",
            "status": "completed",
            "technician_notes": "Replaced LCD screen and digitizer. Device fully functional."
        },
        {
            "description": "Battery drains very quickly, device shuts down unexpectedly.",
            "status": "in_progress", 
            "technician_notes": "Battery replacement in progress. Estimated completion: 2 days."
        },
        {
            "description": "Device won't turn on, no response to power button.",
            "status": "pending_confirmation",
            "technician_notes": "Initial diagnosis pending. Will contact customer for confirmation."
        },
        {
            "description": "Charging port loose, cable keeps disconnecting.",
            "status": "completed",
            "technician_notes": "Replaced charging port assembly. Tested with multiple cables."
        },
        {
            "description": "Overheating issues during heavy usage and gaming.",
            "status": "cancelled",
            "technician_notes": "Customer cancelled due to cost concerns."
        },
        {
            "description": "Audio jack not working, no sound through headphones.",
            "status": "completed",
            "technician_notes": "Cleaned audio jack and replaced internal connector."
        },
        {
            "description": "Camera app crashes, rear camera shows black screen.",
            "status": "in_progress",
            "technician_notes": "Camera module replacement ordered. ETA: 3-5 business days."
        }
    ]
    
    # Create test bookings
    created_bookings = []
    
    for i in range(7):  # Create 7 test bookings
        service = random.choice(services)
        issue = issues[i]
        
        # Generate dates (mix of past and recent)
        if i < 3:
            # Past completed bookings
            created_date = datetime.now() - timedelta(days=random.randint(30, 90))
            appointment_date = created_date + timedelta(days=random.randint(1, 7))
        elif i < 5:
            # Recent bookings
            created_date = datetime.now() - timedelta(days=random.randint(1, 15))
            appointment_date = created_date + timedelta(days=random.randint(1, 3))
        else:
            # Future appointments
            created_date = datetime.now() - timedelta(days=random.randint(1, 5))
            appointment_date = datetime.now() + timedelta(days=random.randint(1, 10))
        
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
            "actualCost": service["price"] if issue["status"] == "completed" else None,
            "appointmentTimestamp": appointment_date,
            "createdAt": created_date,
            "updatedAt": datetime.now(),
            "location": "ElektroniCare Service Center",
            "technicianEmail": "agusseptiawanasep@gmail.com",
            "technicianName": "Agus Septiawan",
            "technicianNotes": issue["technician_notes"],
            "customerRating": random.randint(4, 5) if issue["status"] == "completed" else None,
            "deviceImageUrl": f"https://res.cloudinary.com/elektronicare/image/upload/v1/repair_images/{booking_id.lower()}.jpg" if random.choice([True, False]) else None,
            "paymentStatus": "paid" if issue["status"] == "completed" else "pending",
            "paymentMethod": "cash" if issue["status"] == "completed" else None,
        }
        
        # Add status-specific fields
        if issue["status"] == "completed":
            booking_data["completedAt"] = appointment_date + timedelta(days=random.randint(1, 3))
            booking_data["warrantyExpiry"] = booking_data["completedAt"] + timedelta(days=30)
        elif issue["status"] == "cancelled":
            booking_data["cancelledAt"] = appointment_date - timedelta(days=1)
            booking_data["cancellationReason"] = "Customer request"
        
        try:
            # Add to Firestore
            doc_ref = db.collection("repairs").document(booking_id)
            doc_ref.set(booking_data)
            created_bookings.append(booking_id)
            print(f"✅ Created booking: {booking_id} - {service['name']} ({issue['status']})")
            
        except Exception as e:
            print(f"❌ Error creating booking {booking_id}: {e}")
    
    return created_bookings

def create_user_profile():
    """Create/update user profile data"""
    USER_ID = "1rHqzlFFdId0eaWqBohXIs4ex9u2"
    
    db = initialize_firebase()
    
    user_data = {
        "userId": USER_ID,
        "fullName": "John Doe",
        "email": "john.doe@example.com",
        "phone": "+6281234567890",
        "address": "Jl. Teknologi No. 123, Jakarta Selatan",
        "profileImageUrl": "https://res.cloudinary.com/elektronicare/image/upload/v1/profile_images/john_doe.jpg",
        "joinDate": datetime.now() - timedelta(days=120),
        "lastLogin": datetime.now(),
        "totalBookings": 7,
        "completedBookings": 3,
        "averageRating": 4.7,
        "preferredContactMethod": "whatsapp",
        "notificationSettings": {
            "email": True,
            "sms": False,
            "push": True,
            "whatsapp": True
        },
        "loyaltyPoints": 350,
        "membershipLevel": "Silver"
    }
    
    try:
        doc_ref = db.collection("users").document(USER_ID)
        doc_ref.set(user_data, merge=True)
        print(f"✅ Updated user profile: {USER_ID}")
        return True
    except Exception as e:
        print(f"❌ Error updating user profile: {e}")
        return False

def create_notification_history():
    """Create notification history for the user"""
    USER_ID = "1rHqzlFFdId0eaWqBohXIs4ex9u2"
    
    db = initialize_firebase()
    
    notifications = [
        {
            "title": "Booking Confirmed",
            "message": "Your smartphone repair booking has been confirmed for tomorrow at 10:00 AM",
            "type": "booking_confirmation",
            "timestamp": datetime.now() - timedelta(hours=2),
            "read": False,
            "actionUrl": "/booking/details"
        },
        {
            "title": "Repair Completed",
            "message": "Great news! Your laptop repair has been completed. You can pick it up anytime.",
            "type": "repair_completed",
            "timestamp": datetime.now() - timedelta(days=1),
            "read": True,
            "actionUrl": "/booking/completed"
        },
        {
            "title": "Payment Reminder",
            "message": "Don't forget to complete payment for your recent tablet repair service.",
            "type": "payment_reminder",
            "timestamp": datetime.now() - timedelta(days=3),
            "read": True,
            "actionUrl": "/payment"
        },
        {
            "title": "Technician Update",
            "message": "Your device diagnosis is complete. Replacement parts have been ordered.",
            "type": "status_update",
            "timestamp": datetime.now() - timedelta(days=5),
            "read": True,
            "actionUrl": "/booking/progress"
        }
    ]
    
    created_notifications = []
    
    for notification in notifications:
        notification["userId"] = USER_ID
        notification_id = str(uuid.uuid4())
        
        try:
            doc_ref = db.collection("notifications").document(notification_id)
            doc_ref.set(notification)
            created_notifications.append(notification_id)
            print(f"✅ Created notification: {notification['title']}")
        except Exception as e:
            print(f"❌ Error creating notification: {e}")
    
    return created_notifications

def main():
    """Main function to create all test data"""
    print("🚀 Creating test data for ElektroniCare...")
    print("=" * 50)
    
    try:
        # Create user profile
        print("\n📋 Creating user profile...")
        create_user_profile()
        
        # Create booking history
        print("\n📅 Creating booking history...")
        bookings = create_test_data()
        
        # Create notifications
        print("\n🔔 Creating notification history...")
        notifications = create_notification_history()
        
        print("\n" + "=" * 50)
        print("✅ TEST DATA CREATION COMPLETED!")
        print(f"📊 Created {len(bookings)} bookings")
        print(f"🔔 Created {len(notifications)} notifications")
        print("👤 Updated user profile")
        
        print("\n📱 You can now test the app with user ID:")
        print("1rHqzlFFdId0eaWqBohXIs4ex9u2")
        
    except Exception as e:
        print(f"\n❌ Error in main execution: {e}")
        print("Please check your Firebase configuration and try again.")

if __name__ == "__main__":
    main()