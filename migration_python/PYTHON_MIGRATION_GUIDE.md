# 🐍 PYTHON MIGRATION SCRIPT - ELEKTRONICARE

## 🎯 OVERVIEW

Python migration script yang powerful dan fleksibel untuk mengisi database Firestore dengan sample repair history data. Script ini dibuat sebagai alternatif dari Kotlin migration yang bisa dijalankan secara terpisah dari aplikasi Android.

## ✨ KEUNGGULAN PYTHON SCRIPT

### 🚀 **Standalone Operation**
- Tidak perlu build aplikasi Android
- Bisa dijalankan di server, laptop, atau CI/CD
- Independent dari Android development environment

### 🖥️ **Interactive Mode**
- User-friendly interface dengan menu
- Konfirmasi untuk operasi berbahaya
- Progress tracking real-time
- Error handling yang robust

### 🔧 **Command Line Interface**
- Automation-friendly
- Scriptable untuk CI/CD
- Batch operations
- Logging ekstensif

### 📊 **Advanced Features**
- Statistics dan analytics
- Batch operations untuk performance
- Configurable parameters
- Multiple authentication methods

## 📋 FITUR LENGKAP

### 🎯 **Migration Types**
1. **Full Migration** - Sample data untuk semua user (8-12 repairs per user)
2. **User Migration** - Sample data untuk user tertentu
3. **Clean Data** - Hapus semua data repair (BAHAYA!)
4. **Statistics** - Lihat statistik database lengkap

### 📊 **Sample Data Realistis**
- **10 Device Types**: Smartphone, Laptop, Tablet, Smart TV, Gaming Console, Smartwatch, Headphones, Speaker, Camera, Printer
- **50+ Device Models**: iPhone 14, MacBook Pro, PlayStation 5, Samsung Galaxy S23, dll
- **15 Issue Types**: Screen cracked, battery drain, water damage, charging issues, dll
- **5 Repair Status**: pending, pending_confirmation, in_progress, completed, cancelled
- **5 Service Locations**: Jakarta, Bandung, Surabaya, Medan, Semarang
- **Realistic Dates**: Random dalam 6 bulan terakhir
- **Cost Range**: 50k - 2M IDR
- **Sample Images**: Cloudinary URLs untuk device images

## 🚀 QUICK START

### 1. **Setup Environment**
```bash
# Clone dan masuk ke folder
cd migration_python

# Install dependencies
pip install -r requirements.txt

# Atau jalankan setup
python setup.py
```

### 2. **Setup Firebase Credentials**

#### Option A: Service Account (Recommended)
```bash
# Download service account JSON dari Firebase Console
# Simpan sebagai service-account.json
```

#### Option B: Environment Variable
```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
```

#### Option C: Google Cloud SDK
```bash
gcloud auth application-default login
```

### 3. **Jalankan Migration**

#### **Interactive Mode** (Recommended)
```bash
# Windows
run_migration.bat

# Linux/Mac
./run_migration.sh

# Manual
python interactive_migration.py
```

#### **Command Line Mode**
```bash
# Full migration
python history_migration.py --action full

# User specific
python history_migration.py --action user --user-id "USER_ID"

# Statistics
python history_migration.py --action stats

# Clean data (HATI-HATI!)
python history_migration.py --action clean --confirm-clean
```

## 🖥️ INTERACTIVE MODE

Menu yang user-friendly:

```
🔧 ELEKTRONICARE MIGRATION TOOL
========================================
Tool untuk mengisi database dengan sample repair history
Version: 1.0
========================================

🔑 FIREBASE AUTHENTICATION:
1. Gunakan default credentials (Google Cloud/Environment)
2. Gunakan service account JSON file

📋 PILIHAN MIGRATION:
1. 🚀 Full Migration (Semua User)
2. 👤 Sample untuk User Tertentu
3. 📊 Lihat Statistik Database
4. 🗑️  Clean All Data (BAHAYA!)
5. ❌ Exit
```

### Features Interactive Mode:
- ✅ **Authentication Setup** - Pilih metode autentikasi
- ✅ **User Confirmation** - Konfirmasi untuk operasi berbahaya
- ✅ **Progress Tracking** - Monitor progress real-time
- ✅ **Error Handling** - Handle error dengan graceful
- ✅ **Statistics Display** - Tampilkan statistik dengan emoji
- ✅ **Logging** - Log ke file dan console

## 🔧 COMMAND LINE INTERFACE

### Basic Commands
```bash
# Help
python history_migration.py --help

# Full migration untuk semua user
python history_migration.py --action full

# Sample untuk user tertentu
python history_migration.py --action user --user-id "abc123"

# Lihat statistik database
python history_migration.py --action stats

# Clean semua data (DANGEROUS!)
python history_migration.py --action clean --confirm-clean
```

### Advanced Usage
```bash
# Dengan service account
python history_migration.py \
  --service-account service-account.json \
  --action full

# Custom user ID
python history_migration.py \
  --action user \
  --user-id "custom-user-id-here"

# Automation script
#!/bin/bash
python history_migration.py --action clean --confirm-clean
python history_migration.py --action full
python history_migration.py --action stats
```

## 📊 EXPECTED RESULTS

### Setelah Full Migration:
```
📊 Migration Statistics:
👥 Total Users: 5
🔧 Total Repairs: 47
📈 Rata-rata Repairs per User: 9.4

📋 Repairs berdasarkan Status:
⏳ pending: 8
❓ pending_confirmation: 9
🔄 in_progress: 12
✅ completed: 15
❌ cancelled: 3
```

### Di Aplikasi Android:
- ✅ History page terisi dengan data lengkap
- ✅ Filter status berfungsi dengan baik
- ✅ Sorting berdasarkan tanggal
- ✅ Detail repair dapat dilihat
- ✅ Gambar device sample muncul
- ✅ UI responsive dengan data banyak

## 🔒 SAFETY & SECURITY

### Safety Features:
- ✅ **Duplicate Prevention** - Cek existing data sebelum create
- ✅ **Batch Operations** - Efficient Firestore operations
- ✅ **Error Handling** - Robust error handling dan logging
- ✅ **Multiple Confirmation** - Konfirmasi ganda untuk operasi berbahaya
- ✅ **Detailed Logging** - Log ke file dan console
- ✅ **Rollback Support** - Clean function untuk reset

### Security Features:
- ✅ **Firebase Admin SDK** - Secure server-side access
- ✅ **Service Account** - Proper authentication
- ✅ **Environment Variables** - Secure credential storage
- ✅ **Input Validation** - Validate semua input
- ✅ **Permission Check** - Verify Firestore permissions

## ⚙️ CONFIGURATION

### config.json
```json
{
  "migration": {
    "repairs_per_user_min": 8,
    "repairs_per_user_max": 12,
    "months_back": 6,
    "min_cost": 50000.0,
    "max_cost": 2000000.0
  },
  "firebase": {
    "project_id": "elektronicare-4a4dc",
    "collections": {
      "users": "users",
      "repairs": "repairs"
    }
  }
}
```

### Customizable Parameters:
- **repairs_per_user_min/max** - Jumlah repair per user
- **months_back** - Rentang tanggal (bulan ke belakang)
- **min_cost/max_cost** - Range estimasi biaya
- **project_id** - Firebase project ID
- **collections** - Nama collection Firestore

## 📁 FILE STRUCTURE

```
migration_python/
├── 📄 history_migration.py          # Main migration script
├── 🖥️ interactive_migration.py      # Interactive UI
├── ⚙️ setup.py                     # Setup script
├── 📋 requirements.txt             # Python dependencies
├── 🔧 config.json                 # Configuration
├── 🪟 run_migration.bat           # Windows batch script
├── 🐧 run_migration.sh            # Linux/Mac shell script
├── 📖 README.md                   # Documentation
├── 🔑 service-account-template.json # Service account template
└── 📝 migration.log               # Log file (created after run)
```

## 🎯 USE CASES

### 1. **Demo/Presentation** 🎪
```bash
# Setup data untuk demo
python history_migration.py --action full

# Hasil: Semua user punya repair history
# Perfect untuk demo aplikasi
```

### 2. **Development/Testing** 🧪
```bash
# Test dengan user tertentu
python history_migration.py --action user --user-id "test-user"

# Hasil: Hanya test user yang punya data
# Ideal untuk development
```

### 3. **Database Reset** 🔄
```bash
# Reset dan isi ulang
python history_migration.py --action clean --confirm-clean
python history_migration.py --action full

# Hasil: Database fresh dengan data baru
```

### 4. **CI/CD Integration** 🤖
```bash
#!/bin/bash
# CI/CD script
export GOOGLE_APPLICATION_CREDENTIALS="$SERVICE_ACCOUNT_PATH"
python history_migration.py --action stats
if [ $? -eq 0 ]; then
    python history_migration.py --action full
fi
```

### 5. **Production Seeding** 🌱
```bash
# Production environment
python history_migration.py \
  --service-account prod-service-account.json \
  --action full
```

## ⚠️ TROUBLESHOOTING

### Firebase Connection Issues
```bash
# Test connection
python history_migration.py --action stats

# Check credentials
python -c "import firebase_admin; print('Firebase SDK OK')"

# Debug mode
export GOOGLE_CLOUD_PROJECT=elektronicare-4a4dc
python history_migration.py --action stats
```

### Permission Errors
```bash
# User install
pip install --user -r requirements.txt

# Virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
venv\Scripts\activate     # Windows
pip install -r requirements.txt
```

### Python Version Issues
```bash
# Check version
python --version

# Use Python 3 explicitly
python3 history_migration.py --action stats

# Install Python 3.7+
# Ubuntu: sudo apt install python3.8
# macOS: brew install python@3.8
# Windows: Download from python.org
```

### Firestore Rules Issues
```javascript
// Firestore Rules - pastikan write access
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /repairs/{document} {
      allow read, write: if request.auth != null;
    }
    match /users/{document} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 📈 PERFORMANCE

### Optimization Features:
- ✅ **Batch Operations** - Firestore batch writes (500 ops/batch)
- ✅ **Async Operations** - Non-blocking operations
- ✅ **Memory Efficient** - Stream processing untuk large datasets
- ✅ **Rate Limiting** - Respect Firestore quotas
- ✅ **Error Retry** - Automatic retry untuk transient errors

### Performance Metrics:
- **Full Migration**: ~2-5 menit untuk 100 users
- **User Migration**: ~5-10 detik per user
- **Statistics**: ~10-30 detik tergantung data size
- **Clean Operation**: ~1-3 menit tergantung data size

## 🔗 INTEGRATION

### Dengan Android App:
1. **Data Compatibility** - Format data sama dengan Kotlin version
2. **Collection Structure** - Menggunakan collection yang sama
3. **Field Mapping** - Field names dan types konsisten
4. **Timestamp Handling** - Proper Firestore timestamp

### Dengan Firebase:
1. **Admin SDK** - Server-side access dengan full permissions
2. **Batch Operations** - Efficient bulk operations
3. **Error Handling** - Proper Firebase error handling
4. **Quota Management** - Respect Firestore limits

### Dengan CI/CD:
1. **Environment Variables** - Secure credential management
2. **Exit Codes** - Proper exit codes untuk automation
3. **Logging** - Structured logging untuk monitoring
4. **Health Checks** - Built-in health check commands

## 🎉 CONCLUSION

Python migration script ini memberikan solusi yang:

### ✅ **Powerful**
- Full-featured migration tool
- Advanced error handling
- Comprehensive logging
- Performance optimized

### ✅ **Flexible**
- Multiple authentication methods
- Configurable parameters
- Interactive dan command line modes
- Cross-platform support

### ✅ **Safe**
- Multiple confirmations
- Duplicate prevention
- Rollback support
- Extensive validation

### ✅ **Production Ready**
- CI/CD integration
- Environment variable support
- Proper error codes
- Monitoring friendly

**Perfect untuk development, testing, demo, dan production use! 🚀**

---

## 📞 SUPPORT

Jika ada masalah:
1. 📝 Cek `migration.log` untuk detail error
2. 🔑 Pastikan Firebase credentials benar
3. 🌐 Test koneksi dengan `--action stats`
4. 🔒 Cek Firestore rules dan permissions
5. 📊 Verify data dengan interactive mode

**Happy Migration! 🎉**