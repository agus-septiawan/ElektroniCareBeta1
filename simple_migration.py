#!/usr/bin/env python3
"""
ElektroniCare Simple Migration Script
====================================

Script sederhana untuk mengisi database Firestore dengan sample repair history data.
TIDAK PERLU service account - menggunakan web app credentials!

Usage:
    python simple_migration.py

Author: ElektroniCare Team
Version: 1.0 (Simple)
"""

import json
import random
import requests
from datetime import datetime, timedelta
from typing import List, Dict, Any
import time

class SimpleFirebaseMigration:
    """Simple migration using Firebase REST API"""

    def __init__(self):
        """Initialize with project config"""
        self.project_id = "elektronicare-4a4dc"
        self.base_url = f"https://firestore.googleapis.com/v1/projects/{self.project_id}/databases/(default)/documents"

        # Sample data - sama seperti Kotlin version
        self.device_types = [
            "Smartphone", "Laptop", "Tablet", "Smart TV", "Gaming Console",
            "Smartwatch", "Headphones", "Speaker", "Camera", "Printer"
        ]

        self.device_models = {
            "Smartphone": ["iPhone 14", "Samsung Galaxy S23", "Xiaomi 13", "OPPO Find X5", "Vivo V27"],
            "Laptop": ["MacBook Pro", "ASUS ROG", "Lenovo ThinkPad", "HP Pavilion", "Dell XPS"],
            "Tablet": ["iPad Pro", "Samsung Galaxy Tab", "Huawei MatePad", "Lenovo Tab", "Xiaomi Pad"],
            "Smart TV": ["Samsung QLED", "LG OLED", "Sony Bravia", "TCL Android TV", "Xiaomi TV"],
            "Gaming Console": ["PlayStation 5", "Xbox Series X", "Nintendo Switch", "Steam Deck", "ASUS ROG Ally"],
            "Smartwatch": ["Apple Watch", "Samsung Galaxy Watch", "Garmin Forerunner", "Fitbit Versa", "Amazfit GTR"],
            "Headphones": ["AirPods Pro", "Sony WH-1000XM4", "Bose QuietComfort", "Sennheiser HD", "Audio-Technica ATH"],
            "Speaker": ["JBL Charge", "Bose SoundLink", "Sony SRS", "Harman Kardon Onyx", "Ultimate Ears BOOM"],
            "Camera": ["Canon EOS R5", "Sony Alpha A7", "Nikon Z9", "Fujifilm X-T5", "Panasonic Lumix"],
            "Printer": ["HP LaserJet", "Canon PIXMA", "Epson EcoTank", "Brother DCP", "Xerox WorkCentre"]
        }

        self.issue_descriptions = [
            "Screen cracked and not responding to touch",
            "Battery drains very quickly, needs replacement",
            "Device won't turn on, power button not working",
            "Charging port damaged, cable won't stay connected",
            "Speaker making crackling sounds",
            "Camera not focusing properly, blurry images",
            "Overheating issues during normal usage",
            "Software keeps crashing and freezing",
            "Water damage, device got wet accidentally",
            "Display has dead pixels and color issues",
            "Keyboard keys not working properly",
            "WiFi connection keeps dropping",
            "Bluetooth pairing problems",
            "Memory/storage issues, running very slow",
            "Physical damage from drop, case cracked"
        ]

        self.repair_statuses = [
            "pending",
            "pending_confirmation",
            "in_progress",
            "completed",
            "cancelled"
        ]

        self.locations = [
            "ElektroniCare Service Center - Jakarta",
            "ElektroniCare Service Center - Bandung",
            "ElektroniCare Service Center - Surabaya",
            "ElektroniCare Service Center - Medan",
            "ElektroniCare Service Center - Semarang"
        ]

        self.technician_emails = [
            "satriawiangga200@gmail.com",
            "technician1@elektronicare.com",
            "technician2@elektronicare.com",
            "technician3@elektronicare.com",
            "technician4@elektronicare.com"
        ]

    def get_all_users(self) -> List[Dict]:
        """Get all users from Firestore using REST API"""
        try:
            print("🔍 Mengambil data users...")
            url = f"{self.base_url}/users"
            response = requests.get(url)

            if response.status_code == 200:
                data = response.json()
                users = []

                if 'documents' in data:
                    for doc in data['documents']:
                        user_id = doc['name'].split('/')[-1]
                        fields = doc.get('fields', {})

                        user_data = {
                            'id': user_id,
                            'fullName': fields.get('fullName', {}).get('stringValue', 'User'),
                            'email': fields.get('email', {}).get('stringValue', '')
                        }
                        users.append(user_data)

                print(f"✅ Ditemukan {len(users)} users")
                return users
            else:
                print(f"❌ Error getting users: {response.status_code}")
                return []

        except Exception as e:
            print(f"❌ Error: {e}")
            return []

    def check_existing_repairs(self, user_id: str) -> int:
        """Check how many repairs user already has"""
        try:
            url = f"{self.base_url}/repairs"
            params = {
                'structuredQuery': json.dumps({
                    'where': {
                        'fieldFilter': {
                            'field': {'fieldPath': 'userId'},
                            'op': 'EQUAL',
                            'value': {'stringValue': user_id}
                        }
                    }
                })
            }

            response = requests.post(f"{self.base_url}:runQuery", json={'structuredQuery': params['structuredQuery']})

            if response.status_code == 200:
                data = response.json()
                count = len([item for item in data if 'document' in item])
                return count
            return 0

        except Exception as e:
            print(f"❌ Error checking repairs for {user_id}: {e}")
            return 0

    def generate_sample_repair(self, user_id: str, index: int) -> Dict:
        """Generate a single sample repair record"""
        device_type = random.choice(self.device_types)
        device_model = random.choice(self.device_models.get(device_type, ["Unknown Model"]))
        issue_description = random.choice(self.issue_descriptions)
        status = random.choice(self.repair_statuses)
        location = random.choice(self.locations)
        technician_email = random.choice(self.technician_emails)

        # Generate dates (last 6 months)
        now = datetime.now()
        six_months_ago = now - timedelta(days=180)

        # Random creation date
        created_at = six_months_ago + timedelta(
            seconds=random.randint(0, int((now - six_months_ago).total_seconds()))
        )

        # Appointment date (1-30 days after creation)
        appointment_timestamp = created_at + timedelta(days=random.randint(1, 30))

        # Completed date (only for completed status)
        completed_date = None
        if status == "completed":
            completed_date = appointment_timestamp + timedelta(days=random.randint(1, 7))

        # Estimated cost (50k - 2M IDR)
        estimated_cost = random.uniform(50000.0, 2000000.0)

        # Generate sample image URL
        image_id = random.randint(1, 5)
        device_image_url = f"https://res.cloudinary.com/elektronicare/image/upload/v1/samples/{device_type.lower()}_{image_id}.jpg"

        repair_data = {
            "userId": {"stringValue": user_id},
            "deviceType": {"stringValue": device_type},
            "deviceModel": {"stringValue": device_model},
            "issueDescription": {"stringValue": issue_description},
            "status": {"stringValue": status},
            "location": {"stringValue": location},
            "technicianEmail": {"stringValue": technician_email},
            "estimatedCost": {"doubleValue": estimated_cost},
            "appointmentTimestamp": {"timestampValue": appointment_timestamp.isoformat() + "Z"},
            "createdAt": {"timestampValue": created_at.isoformat() + "Z"},
            "updatedAt": {"timestampValue": created_at.isoformat() + "Z"},
            "serviceId": {"stringValue": f"service_{random.randint(1, 5)}"},
            "deviceImageUrl": {"stringValue": device_image_url}
        }

        if completed_date:
            repair_data["completedDate"] = {"timestampValue": completed_date.isoformat() + "Z"}

        return repair_data

    def create_repair(self, repair_data: Dict) -> bool:
        """Create a single repair record"""
        try:
            url = f"{self.base_url}/repairs"
            response = requests.post(url, json={"fields": repair_data})

            if response.status_code == 200:
                return True
            else:
                print(f"❌ Error creating repair: {response.status_code} - {response.text}")
                return False

        except Exception as e:
            print(f"❌ Error creating repair: {e}")
            return False

    def create_sample_repairs_for_user(self, user_id: str, user_name: str) -> bool:
        """Create sample repair records for a specific user"""
        try:
            # Check existing repairs
            existing_count = self.check_existing_repairs(user_id)

            if existing_count >= 5:
                print(f"⏭️  User {user_name} sudah punya {existing_count} repairs, skip")
                return True

            # Create 8-12 sample repairs
            num_repairs = random.randint(8, 12)
            print(f"📝 Membuat {num_repairs} sample repairs untuk {user_name}...")

            success_count = 0
            for i in range(num_repairs):
                repair_data = self.generate_sample_repair(user_id, i)

                if self.create_repair(repair_data):
                    success_count += 1
                    device_model = repair_data["deviceModel"]["stringValue"]
                    status = repair_data["status"]["stringValue"]
                    print(f"  ✅ {i+1}/{num_repairs}: {device_model} - {status}")
                else:
                    print(f"  ❌ {i+1}/{num_repairs}: Failed")

                # Small delay to avoid rate limiting
                time.sleep(0.1)

            print(f"✅ Berhasil membuat {success_count}/{num_repairs} repairs untuk {user_name}")
            return success_count > 0

        except Exception as e:
            print(f"❌ Error creating repairs for {user_name}: {e}")
            return False

    def run_migration(self) -> bool:
        """Run migration for all users"""
        try:
            print("🚀 MEMULAI SIMPLE MIGRATION")
            print("=" * 50)

            # Get all users
            users = self.get_all_users()

            if not users:
                print("❌ Tidak ada users ditemukan!")
                return False

            print(f"👥 Akan membuat sample data untuk {len(users)} users")
            print()

            success_count = 0
            for i, user in enumerate(users, 1):
                print(f"[{i}/{len(users)}] Processing user: {user['fullName']} ({user['id']})")

                if self.create_sample_repairs_for_user(user['id'], user['fullName']):
                    success_count += 1

                print()

            print("=" * 50)
            print(f"🎉 MIGRATION SELESAI!")
            print(f"✅ Berhasil: {success_count}/{len(users)} users")
            print(f"❌ Gagal: {len(users) - success_count}/{len(users)} users")

            return success_count > 0

        except Exception as e:
            print(f"❌ Migration error: {e}")
            return False

def main():
    """Main function"""
    print("🔥 ElektroniCare Simple Migration Tool")
    print("=====================================")
    print()

    migration = SimpleFirebaseMigration()

    print("Pilih opsi:")
    print("1. 🚀 Full Migration (semua users)")
    print("2. 📊 Lihat statistik users")
    print("3. ❌ Exit")
    print()

    choice = input("Masukkan pilihan (1-3): ").strip()

    if choice == "1":
        print()
        confirm = input("⚠️  Lanjutkan dengan full migration? (y/N): ").strip().lower()
        if confirm in ['y', 'yes']:
            migration.run_migration()
        else:
            print("❌ Migration dibatalkan")

    elif choice == "2":
        users = migration.get_all_users()
        print(f"\n📊 Total users: {len(users)}")
        for user in users[:5]:  # Show first 5
            print(f"  - {user['fullName']} ({user['email']})")
        if len(users) > 5:
            print(f"  ... dan {len(users) - 5} users lainnya")

    elif choice == "3":
        print("👋 Goodbye!")

    else:
        print("❌ Pilihan tidak valid!")

if __name__ == "__main__":
    main()
