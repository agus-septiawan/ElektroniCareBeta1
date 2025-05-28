#!/usr/bin/env python3
"""
Simple Test Data Creator for ElektroniCare
Creates JSON data that can be imported to Firestore manually
"""

import json
from datetime import datetime, timedelta
import random
import uuid

def generate_test_data():
    """Generate comprehensive test data for user history"""
    
    # Target user ID
    USER_ID = "1rHqzlFFdId0eaWqBohXIs4ex9u2"
    
    # Sample services data
    services = [
        {"id": "smartphone_repair", "name": "Smartphone Repair", "price": 150000},
        {"id": "laptop_repair", "name": "Laptop Repair", "price": 300000},
        {"id": "tablet_repair", "name": "Tablet Repair", "price": 200000},
        {"id": "smartwatch_repair", "name": "Smartwatch Repair", "price": 100000},
        {"id": "headphone_repair", "name": "Headphone Repair", "price": 75000},
    ]
    
    # Sample issues and descriptions
    issues = [
        {
            "description": "Screen cracked after dropping the device. Touch functionality partially working.",
            "status": "completed",
            "device": "iPhone 13 Pro",
            "location": "Home Service - Jl. Sudirman No. 123, Jakarta"
        },
        {
            "description": "Battery drains very quickly, device shuts down unexpectedly.",
            "status": "in_progress", 
            "device": "Samsung Galaxy S22",
            "location": "Workshop - ElektroniCare Center"
        },
        {
            "description": "Laptop won't turn on, power button not responding.",
            "status": "pending",
            "device": "MacBook Air M1",
            "location": "Home Service - Jl. Thamrin No. 456, Jakarta"
        },
        {
            "description": "Water damage, device got wet in rain. Screen flickering.",
            "status": "cancelled",
            "device": "iPad Pro 11",
            "location": "Workshop - ElektroniCare Center"
        },
        {
            "description": "Charging port loose, cable keeps disconnecting.",
            "status": "completed",
            "device": "OnePlus 9 Pro",
            "location": "Home Service - Jl. Gatot Subroto No. 789, Jakarta"
        }
    ]
    
    # Generate repair documents
    repairs = []
    
    for i, issue in enumerate(issues):
        # Generate dates (spread over last 3 months)
        days_ago = random.randint(1, 90)
        appointment_date = datetime.now() - timedelta(days=days_ago)
        created_date = appointment_date - timedelta(days=random.randint(1, 7))
        
        # Select random service
        service = random.choice(services)
        
        repair_id = str(uuid.uuid4())
        
        repair_data = {
            "id": repair_id,
            "userId": USER_ID,
            "serviceId": service["id"],
            "serviceName": service["name"],
            "deviceModel": issue["device"],
            "issueDescription": issue["description"],
            "location": issue["location"],
            "status": issue["status"],
            "price": service["price"],
            "appointmentTimestamp": appointment_date.isoformat(),
            "createdAt": created_date.isoformat(),
            "updatedAt": appointment_date.isoformat(),
            "customerName": "John Doe",
            "customerPhone": "+6281234567890",
            "customerEmail": "john.doe@example.com",
            "technicianId": f"tech_{random.randint(1, 5)}",
            "technicianName": f"Technician {random.randint(1, 5)}",
            "estimatedDuration": random.choice(["1-2 hours", "2-4 hours", "1 day", "2-3 days"]),
            "notes": f"Repair notes for {issue['device']} - {issue['description'][:50]}...",
            "imageUrl": f"https://res.cloudinary.com/elektronicare/image/upload/v1/repairs/{repair_id}.jpg"
        }
        
        # Add status-specific fields
        if issue["status"] == "completed":
            repair_data["completedAt"] = (appointment_date + timedelta(hours=random.randint(2, 48))).isoformat()
            repair_data["rating"] = random.randint(4, 5)
            repair_data["feedback"] = "Great service! Very professional and quick repair."
        elif issue["status"] == "in_progress":
            repair_data["startedAt"] = (appointment_date + timedelta(hours=1)).isoformat()
            repair_data["progress"] = random.randint(30, 80)
        elif issue["status"] == "cancelled":
            repair_data["cancelledAt"] = (appointment_date + timedelta(hours=random.randint(1, 24))).isoformat()
            repair_data["cancellationReason"] = "Customer requested cancellation"
        
        repairs.append(repair_data)
    
    # Generate user profile data
    user_data = {
        "id": USER_ID,
        "fullName": "John Doe",
        "email": "john.doe@example.com", 
        "phone": "+6281234567890",
        "address": "Jl. Sudirman No. 123, Jakarta Pusat, DKI Jakarta",
        "profileImageUrl": "https://res.cloudinary.com/elektronicare/image/upload/v1/profiles/john_doe.jpg",
        "createdAt": (datetime.now() - timedelta(days=120)).isoformat(),
        "updatedAt": datetime.now().isoformat(),
        "isVerified": True,
        "totalRepairs": len(repairs),
        "completedRepairs": len([r for r in repairs if r["status"] == "completed"]),
        "memberSince": "March 2024"
    }
    
    # Generate notifications data
    notifications = []
    for repair in repairs:
        # Booking confirmation notification
        notifications.append({
            "id": str(uuid.uuid4()),
            "userId": USER_ID,
            "type": "booking_confirmed",
            "title": "Booking Confirmed",
            "message": f"Your repair booking for {repair['deviceModel']} has been confirmed.",
            "repairId": repair["id"],
            "isRead": True,
            "createdAt": repair["createdAt"],
            "data": {
                "repairId": repair["id"],
                "deviceModel": repair["deviceModel"]
            }
        })
        
        # Status update notifications
        if repair["status"] == "completed":
            notifications.append({
                "id": str(uuid.uuid4()),
                "userId": USER_ID,
                "type": "repair_completed",
                "title": "Repair Completed",
                "message": f"Your {repair['deviceModel']} repair has been completed successfully!",
                "repairId": repair["id"],
                "isRead": random.choice([True, False]),
                "createdAt": repair.get("completedAt", repair["updatedAt"]),
                "data": {
                    "repairId": repair["id"],
                    "deviceModel": repair["deviceModel"]
                }
            })
    
    return {
        "user": user_data,
        "repairs": repairs,
        "notifications": notifications,
        "services": services
    }

def save_test_data():
    """Generate and save test data to JSON files"""
    
    print("🚀 Generating ElektroniCare Test Data...")
    
    data = generate_test_data()
    
    # Save to separate JSON files
    with open("test_user_data.json", "w") as f:
        json.dump(data["user"], f, indent=2)
    
    with open("test_repairs_data.json", "w") as f:
        json.dump(data["repairs"], f, indent=2)
    
    with open("test_notifications_data.json", "w") as f:
        json.dump(data["notifications"], f, indent=2)
    
    with open("test_services_data.json", "w") as f:
        json.dump(data["services"], f, indent=2)
    
    # Save complete data
    with open("complete_test_data.json", "w") as f:
        json.dump(data, f, indent=2)
    
    print("✅ Test data generated successfully!")
    print(f"📊 Generated:")
    print(f"   - 1 User profile")
    print(f"   - {len(data['repairs'])} Repair records")
    print(f"   - {len(data['notifications'])} Notifications")
    print(f"   - {len(data['services'])} Services")
    
    print("\n📁 Files created:")
    print("   - test_user_data.json")
    print("   - test_repairs_data.json") 
    print("   - test_notifications_data.json")
    print("   - test_services_data.json")
    print("   - complete_test_data.json")
    
    print("\n🔥 Next steps:")
    print("1. Import these JSON files to Firestore using Firebase Console")
    print("2. Or use the Firebase Admin SDK to upload programmatically")
    print("3. Test the app with this realistic data!")
    
    return data

if __name__ == "__main__":
    save_test_data()