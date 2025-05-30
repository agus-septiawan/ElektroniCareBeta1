#!/usr/bin/env python3
"""
ElektroniCare History Migration Script
=====================================

Script untuk mengisi database Firestore dengan sample repair history data
untuk semua user atau user tertentu.

Author: ElektroniCare Team
Version: 1.0
"""

import os
import sys
import json
import random
import logging
from datetime import datetime, timedelta
from typing import List, Dict, Any, Optional
import firebase_admin
from firebase_admin import credentials, firestore
from dataclasses import dataclass

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('migration.log'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

@dataclass
class MigrationConfig:
    """Configuration for migration"""
    repairs_per_user_min: int = 8
    repairs_per_user_max: int = 12
    months_back: int = 6
    min_cost: float = 50000.0
    max_cost: float = 2000000.0

class ElektroniCareMigration:
    """Main migration class for ElektroniCare repair history"""
    
    def __init__(self, service_account_path: str = None):
        """
        Initialize migration with Firebase credentials
        
        Args:
            service_account_path: Path to Firebase service account JSON file
        """
        self.db = None
        self.config = MigrationConfig()
        self._init_firebase(service_account_path)
        self._init_sample_data()
    
    def _init_firebase(self, service_account_path: str = None):
        """Initialize Firebase connection"""
        try:
            if service_account_path and os.path.exists(service_account_path):
                cred = credentials.Certificate(service_account_path)
                firebase_admin.initialize_app(cred)
                logger.info(f"Firebase initialized with service account: {service_account_path}")
            else:
                # Try to use default credentials or environment
                firebase_admin.initialize_app()
                logger.info("Firebase initialized with default credentials")
            
            self.db = firestore.client()
            logger.info("Firestore client initialized successfully")
            
        except Exception as e:
            logger.error(f"Failed to initialize Firebase: {e}")
            sys.exit(1)
    
    def _init_sample_data(self):
        """Initialize sample data for migration"""
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
    
    def get_all_users(self) -> List[Dict[str, Any]]:
        """Get all users from Firestore"""
        try:
            users_ref = self.db.collection('users')
            docs = users_ref.stream()
            
            users = []
            for doc in docs:
                user_data = doc.to_dict()
                user_data['id'] = doc.id
                users.append(user_data)
            
            logger.info(f"Found {len(users)} users in database")
            return users
            
        except Exception as e:
            logger.error(f"Error getting users: {e}")
            return []
    
    def get_user_repairs_count(self, user_id: str) -> int:
        """Get current repair count for a user"""
        try:
            repairs_ref = self.db.collection('repairs')
            query = repairs_ref.where('userId', '==', user_id)
            docs = list(query.stream())
            count = len(docs)
            logger.debug(f"User {user_id} has {count} existing repairs")
            return count
            
        except Exception as e:
            logger.error(f"Error getting repair count for user {user_id}: {e}")
            return 0
    
    def generate_sample_repair(self, user_id: str) -> Dict[str, Any]:
        """Generate a single sample repair record"""
        device_type = random.choice(self.device_types)
        device_model = random.choice(self.device_models[device_type])
        issue_description = random.choice(self.issue_descriptions)
        status = random.choice(self.repair_statuses)
        location = random.choice(self.locations)
        technician_email = random.choice(self.technician_emails)
        
        # Generate dates (last N months)
        now = datetime.now()
        months_ago = now - timedelta(days=30 * self.config.months_back)
        
        # Random creation date
        time_diff = now - months_ago
        random_days = random.randint(0, time_diff.days)
        created_at = months_ago + timedelta(days=random_days)
        
        # Appointment date (1-30 days after creation)
        appointment_days = random.randint(1, 30)
        appointment_timestamp = created_at + timedelta(days=appointment_days)
        
        # Completed date (only for completed status)
        completed_date = None
        if status == "completed":
            completion_days = random.randint(1, 7)
            completed_date = appointment_timestamp + timedelta(days=completion_days)
        
        # Estimated cost
        estimated_cost = round(random.uniform(self.config.min_cost, self.config.max_cost), 2)
        
        # Generate sample image URL
        image_id = random.randint(1, 5)
        device_image_url = f"https://res.cloudinary.com/elektronicare/image/upload/v1/samples/{device_type.lower()}_{image_id}.jpg"
        
        repair_data = {
            'userId': user_id,
            'deviceType': device_type,
            'deviceModel': device_model,
            'issueDescription': issue_description,
            'status': status,
            'location': location,
            'technicianEmail': technician_email,
            'estimatedCost': estimated_cost,
            'appointmentTimestamp': appointment_timestamp,
            'createdAt': created_at,
            'updatedAt': created_at,
            'serviceId': f"service_{random.randint(1, 5)}",
            'deviceImageUrl': device_image_url
        }
        
        if completed_date:
            repair_data['completedDate'] = completed_date
        
        return repair_data
    
    def create_repairs_for_user(self, user_id: str, user_name: str = "Unknown") -> int:
        """Create sample repairs for a specific user"""
        try:
            # Check existing repairs
            existing_count = self.get_user_repairs_count(user_id)
            
            if existing_count >= 5:
                logger.info(f"User {user_name} ({user_id}) already has {existing_count} repairs, skipping")
                return 0
            
            # Generate random number of repairs
            num_repairs = random.randint(self.config.repairs_per_user_min, self.config.repairs_per_user_max)
            logger.info(f"Creating {num_repairs} repairs for user {user_name} ({user_id})")
            
            repairs_created = 0
            batch = self.db.batch()
            
            for i in range(num_repairs):
                repair_data = self.generate_sample_repair(user_id)
                
                # Add to batch
                repair_ref = self.db.collection('repairs').document()
                batch.set(repair_ref, repair_data)
                repairs_created += 1
                
                logger.debug(f"Generated repair {i+1}: {repair_data['deviceModel']} - {repair_data['status']}")
            
            # Commit batch
            batch.commit()
            logger.info(f"Successfully created {repairs_created} repairs for user {user_name}")
            
            return repairs_created
            
        except Exception as e:
            logger.error(f"Error creating repairs for user {user_id}: {e}")
            return 0
    
    def run_full_migration(self) -> bool:
        """Run migration for all users"""
        try:
            logger.info("Starting full migration for all users...")
            
            users = self.get_all_users()
            if not users:
                logger.warning("No users found in database")
                return False
            
            total_repairs_created = 0
            successful_users = 0
            
            for user in users:
                user_id = user['id']
                user_name = user.get('fullName', 'Unknown')
                
                repairs_created = self.create_repairs_for_user(user_id, user_name)
                if repairs_created > 0:
                    total_repairs_created += repairs_created
                    successful_users += 1
            
            logger.info(f"Migration completed!")
            logger.info(f"Users processed: {successful_users}/{len(users)}")
            logger.info(f"Total repairs created: {total_repairs_created}")
            
            return True
            
        except Exception as e:
            logger.error(f"Full migration failed: {e}")
            return False
    
    def create_sample_for_user(self, user_id: str) -> bool:
        """Create sample repairs for a specific user"""
        try:
            logger.info(f"Creating sample repairs for user: {user_id}")
            
            # Get user info
            user_doc = self.db.collection('users').document(user_id).get()
            if not user_doc.exists:
                logger.error(f"User {user_id} not found")
                return False
            
            user_data = user_doc.to_dict()
            user_name = user_data.get('fullName', 'Unknown')
            
            repairs_created = self.create_repairs_for_user(user_id, user_name)
            
            if repairs_created > 0:
                logger.info(f"Successfully created {repairs_created} sample repairs for {user_name}")
                return True
            else:
                logger.warning(f"No repairs created for {user_name}")
                return False
                
        except Exception as e:
            logger.error(f"Error creating sample for user {user_id}: {e}")
            return False
    
    def clean_all_repair_data(self) -> bool:
        """Clean all repair data from database (DANGEROUS!)"""
        try:
            logger.warning("⚠️  CLEANING ALL REPAIR DATA - THIS CANNOT BE UNDONE!")
            
            repairs_ref = self.db.collection('repairs')
            docs = repairs_ref.stream()
            
            deleted_count = 0
            batch = self.db.batch()
            batch_count = 0
            
            for doc in docs:
                batch.delete(doc.reference)
                batch_count += 1
                deleted_count += 1
                
                # Commit batch every 500 operations (Firestore limit)
                if batch_count >= 500:
                    batch.commit()
                    batch = self.db.batch()
                    batch_count = 0
                    logger.info(f"Deleted {deleted_count} repairs so far...")
            
            # Commit remaining operations
            if batch_count > 0:
                batch.commit()
            
            logger.info(f"Successfully deleted {deleted_count} repair records")
            return True
            
        except Exception as e:
            logger.error(f"Error cleaning repair data: {e}")
            return False
    
    def get_migration_stats(self) -> Dict[str, Any]:
        """Get current migration statistics"""
        try:
            # Count users
            users_ref = self.db.collection('users')
            users_count = len(list(users_ref.stream()))
            
            # Count repairs
            repairs_ref = self.db.collection('repairs')
            repairs_count = len(list(repairs_ref.stream()))
            
            # Count repairs by status
            status_counts = {}
            for status in self.repair_statuses:
                status_query = repairs_ref.where('status', '==', status)
                status_count = len(list(status_query.stream()))
                status_counts[status] = status_count
            
            stats = {
                'total_users': users_count,
                'total_repairs': repairs_count,
                'repairs_by_status': status_counts,
                'average_repairs_per_user': round(repairs_count / users_count, 2) if users_count > 0 else 0
            }
            
            return stats
            
        except Exception as e:
            logger.error(f"Error getting migration stats: {e}")
            return {}

def main():
    """Main function for command line usage"""
    import argparse
    
    parser = argparse.ArgumentParser(description='ElektroniCare History Migration Tool')
    parser.add_argument('--service-account', '-s', 
                       help='Path to Firebase service account JSON file')
    parser.add_argument('--action', '-a', 
                       choices=['full', 'user', 'clean', 'stats'],
                       required=True,
                       help='Migration action to perform')
    parser.add_argument('--user-id', '-u',
                       help='User ID for user-specific migration')
    parser.add_argument('--confirm-clean', action='store_true',
                       help='Confirm that you want to clean all data')
    
    args = parser.parse_args()
    
    # Initialize migration
    migration = ElektroniCareMigration(args.service_account)
    
    if args.action == 'full':
        logger.info("🚀 Running full migration for all users...")
        success = migration.run_full_migration()
        if success:
            print("✅ Full migration completed successfully!")
        else:
            print("❌ Full migration failed!")
            sys.exit(1)
    
    elif args.action == 'user':
        if not args.user_id:
            print("❌ User ID required for user migration")
            sys.exit(1)
        
        logger.info(f"👤 Creating sample data for user: {args.user_id}")
        success = migration.create_sample_for_user(args.user_id)
        if success:
            print("✅ User sample data created successfully!")
        else:
            print("❌ User sample data creation failed!")
            sys.exit(1)
    
    elif args.action == 'clean':
        if not args.confirm_clean:
            print("❌ Clean action requires --confirm-clean flag")
            print("⚠️  WARNING: This will delete ALL repair data!")
            sys.exit(1)
        
        logger.info("🗑️  Cleaning all repair data...")
        success = migration.clean_all_repair_data()
        if success:
            print("✅ All repair data cleaned successfully!")
        else:
            print("❌ Clean operation failed!")
            sys.exit(1)
    
    elif args.action == 'stats':
        logger.info("📊 Getting migration statistics...")
        stats = migration.get_migration_stats()
        if stats:
            print("\n📊 Migration Statistics:")
            print(f"Total Users: {stats['total_users']}")
            print(f"Total Repairs: {stats['total_repairs']}")
            print(f"Average Repairs per User: {stats['average_repairs_per_user']}")
            print("\nRepairs by Status:")
            for status, count in stats['repairs_by_status'].items():
                print(f"  {status}: {count}")
        else:
            print("❌ Failed to get statistics!")
            sys.exit(1)

if __name__ == "__main__":
    main()