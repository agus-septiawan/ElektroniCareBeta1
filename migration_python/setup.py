#!/usr/bin/env python3
"""
Setup script untuk ElektroniCare Migration Tool
"""

import os
import sys
import subprocess
import platform

def check_python_version():
    """Check if Python version is compatible"""
    if sys.version_info < (3, 7):
        print("❌ Python 3.7 atau lebih baru diperlukan!")
        print(f"Versi Python Anda: {platform.python_version()}")
        sys.exit(1)
    else:
        print(f"✅ Python {platform.python_version()} - Compatible")

def install_requirements():
    """Install required packages"""
    print("\n📦 Installing dependencies...")
    
    try:
        subprocess.check_call([
            sys.executable, "-m", "pip", "install", "-r", "requirements.txt"
        ])
        print("✅ Dependencies installed successfully!")
    except subprocess.CalledProcessError as e:
        print(f"❌ Failed to install dependencies: {e}")
        sys.exit(1)

def check_firebase_credentials():
    """Check Firebase credentials setup"""
    print("\n🔑 Checking Firebase credentials...")
    
    # Check for service account file
    service_account_files = [
        "service-account.json",
        "firebase-service-account.json",
        "../app/google-services.json"
    ]
    
    found_credentials = False
    for file_path in service_account_files:
        if os.path.exists(file_path):
            print(f"✅ Found service account file: {file_path}")
            found_credentials = True
            break
    
    # Check environment variables
    if os.getenv('GOOGLE_APPLICATION_CREDENTIALS'):
        print(f"✅ Found GOOGLE_APPLICATION_CREDENTIALS environment variable")
        found_credentials = True
    
    if not found_credentials:
        print("⚠️  No Firebase credentials found!")
        print("Anda perlu:")
        print("1. Download service account JSON dari Firebase Console")
        print("2. Simpan sebagai 'service-account.json' di folder ini")
        print("3. Atau set environment variable GOOGLE_APPLICATION_CREDENTIALS")
        print("\nMigration masih bisa dijalankan jika Anda menggunakan Google Cloud credentials.")

def create_sample_config():
    """Create sample configuration if not exists"""
    config_file = "config.json"
    if not os.path.exists(config_file):
        print(f"\n📝 Creating sample {config_file}...")
        # Config already created in previous step
        print(f"✅ Sample {config_file} created!")
    else:
        print(f"✅ {config_file} already exists")

def make_scripts_executable():
    """Make Python scripts executable on Unix systems"""
    if platform.system() != "Windows":
        scripts = ["history_migration.py", "interactive_migration.py"]
        for script in scripts:
            if os.path.exists(script):
                os.chmod(script, 0o755)
                print(f"✅ Made {script} executable")

def print_usage_instructions():
    """Print usage instructions"""
    print("\n" + "=" * 60)
    print("🎉 SETUP COMPLETED!")
    print("=" * 60)
    print("\n📋 CARA PENGGUNAAN:")
    print("\n1. 🖥️  Interactive Mode (Recommended):")
    print("   python interactive_migration.py")
    print("\n2. 🔧 Command Line Mode:")
    print("   python history_migration.py --action full")
    print("   python history_migration.py --action user --user-id USER_ID")
    print("   python history_migration.py --action stats")
    print("   python history_migration.py --action clean --confirm-clean")
    print("\n3. 📖 Dengan Service Account:")
    print("   python history_migration.py --service-account service-account.json --action full")
    print("\n📁 FILES:")
    print("   - history_migration.py: Main migration script")
    print("   - interactive_migration.py: Interactive UI")
    print("   - config.json: Configuration file")
    print("   - requirements.txt: Python dependencies")
    print("   - migration.log: Log file (created after first run)")
    print("\n⚠️  PENTING:")
    print("   - Pastikan Firebase credentials sudah dikonfigurasi")
    print("   - Test dengan 'stats' action terlebih dahulu")
    print("   - Backup data penting sebelum menjalankan migration")
    print("=" * 60)

def main():
    """Main setup function"""
    print("🔧 ElektroniCare Migration Tool Setup")
    print("=" * 40)
    
    # Check Python version
    check_python_version()
    
    # Install requirements
    install_requirements()
    
    # Check Firebase credentials
    check_firebase_credentials()
    
    # Create sample config
    create_sample_config()
    
    # Make scripts executable
    make_scripts_executable()
    
    # Print usage instructions
    print_usage_instructions()

if __name__ == "__main__":
    main()