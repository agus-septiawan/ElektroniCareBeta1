# 🐍 ElektroniCare Python Migration Tool

Tool migration Python untuk mengisi database Firestore dengan sample repair history data yang realistis.

## 🚀 QUICK START

### 1. Setup (Pertama kali)
```bash
# Clone repository dan masuk ke folder migration
cd migration_python

# Jalankan setup
python setup.py

# Atau install manual
pip install -r requirements.txt
```

### 2. Jalankan Migration

#### **Interactive Mode** (Recommended) 🖥️
```bash
# Windows
run_migration.bat

# Linux/Mac
./run_migration.sh

# Manual
python interactive_migration.py
```

#### **Command Line Mode** 🔧
```bash
# Full migration untuk semua user
python history_migration.py --action full

# Sample untuk user tertentu
python history_migration.py --action user --user-id "USER_ID_HERE"

# Lihat statistik database
python history_migration.py --action stats

# Clean semua data (HATI-HATI!)
python history_migration.py --action clean --confirm-clean

# Dengan service account
python history_migration.py --service-account service-account.json --action full
```

## 📋 FITUR

### 🎯 **Migration Types**
- **Full Migration**: Sample data untuk semua user (8-12 repairs per user)
- **User Migration**: Sample data untuk user tertentu
- **Clean Data**: Hapus semua data repair (BAHAYA!)
- **Statistics**: Lihat statistik database

### 📊 **Sample Data**
- **10 Device Types**: Smartphone, Laptop, Tablet, Smart TV, Gaming Console, dll
- **50+ Device Models**: iPhone 14, MacBook Pro, PlayStation 5, dll
- **15 Issue Types**: Screen cracked, battery drain, water damage, dll
- **5 Repair Status**: pending, in_progress, completed, cancelled, dll
- **5 Service Locations**: Jakarta, Bandung, Surabaya, Medan, Semarang
- **Realistic Dates**: Random dalam 6 bulan terakhir
- **Cost Range**: 50k - 2M IDR

## 🔧 SETUP FIREBASE

### Option 1: Service Account (Recommended)
1. Pergi ke [Firebase Console](https://console.firebase.google.com/)
2. Pilih project ElektroniCare
3. Settings → Service Accounts
4. Generate new private key
5. Download JSON file
6. Simpan sebagai `service-account.json` di folder migration

### Option 2: Environment Variable
```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
```

### Option 3: Google Cloud SDK
```bash
gcloud auth application-default login
```

## 📁 FILE STRUCTURE

```
migration_python/
├── history_migration.py      # Main migration script
├── interactive_migration.py  # Interactive UI
├── setup.py                 # Setup script
├── requirements.txt         # Python dependencies
├── config.json             # Configuration
├── run_migration.bat       # Windows batch script
├── run_migration.sh        # Linux/Mac shell script
├── README.md              # Documentation
└── migration.log          # Log file (created after run)
```

## 🎮 INTERACTIVE MODE

Menu interaktif dengan pilihan:

```
📋 PILIHAN MIGRATION:
1. 🚀 Full Migration (Semua User)
2. 👤 Sample untuk User Tertentu  
3. 📊 Lihat Statistik Database
4. 🗑️  Clean All Data (BAHAYA!)
5. ❌ Exit
```

### Features:
- ✅ User-friendly interface
- ✅ Konfirmasi untuk operasi berbahaya
- ✅ Progress tracking
- ✅ Error handling
- ✅ Statistik real-time

## 🔍 COMMAND LINE OPTIONS

### Basic Commands
```bash
# Help
python history_migration.py --help

# Full migration
python history_migration.py --action full

# User migration
python history_migration.py --action user --user-id "abc123"

# Statistics
python history_migration.py --action stats

# Clean (DANGEROUS!)
python history_migration.py --action clean --confirm-clean
```

### Advanced Options
```bash
# With service account
python history_migration.py \
  --service-account service-account.json \
  --action full

# Custom user ID
python history_migration.py \
  --action user \
  --user-id "custom-user-id-here"
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
- History page terisi dengan data
- Filter status berfungsi
- Sorting berdasarkan tanggal
- Detail repair lengkap
- Gambar device sample

## ⚠️ TROUBLESHOOTING

### Firebase Connection Issues
```bash
# Check credentials
python -c "import firebase_admin; print('Firebase SDK OK')"

# Test connection
python history_migration.py --action stats
```

### Permission Errors
```bash
# Install with user permissions
pip install --user -r requirements.txt

# Or use virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
venv\Scripts\activate     # Windows
pip install -r requirements.txt
```

### Python Version Issues
```bash
# Check Python version
python --version

# Use Python 3 explicitly
python3 history_migration.py --action stats
```

## 🔒 SAFETY FEATURES

- ✅ **Duplicate Prevention**: Cek existing data sebelum create
- ✅ **Batch Operations**: Efficient Firestore operations
- ✅ **Error Handling**: Robust error handling dan logging
- ✅ **Confirmation**: Multiple confirmation untuk operasi berbahaya
- ✅ **Logging**: Detailed logging ke file dan console
- ✅ **Rollback**: Clean function untuk reset database

## 📝 CONFIGURATION

Edit `config.json` untuk customize:

```json
{
  "migration": {
    "repairs_per_user_min": 8,
    "repairs_per_user_max": 12,
    "months_back": 6,
    "min_cost": 50000.0,
    "max_cost": 2000000.0
  }
}
```

## 🚨 IMPORTANT NOTES

### Before Running:
1. **Backup Data** - Backup data penting jika ada
2. **Test Connection** - Jalankan `--action stats` dulu
3. **Check Permissions** - Pastikan Firebase rules mengizinkan write
4. **Stable Internet** - Pastikan koneksi internet stabil

### After Migration:
1. **Restart App** - Restart aplikasi Android
2. **Check History** - Pergi ke halaman History
3. **Test Filters** - Coba semua filter status
4. **Verify Data** - Pastikan data muncul dengan benar

## 🎯 USE CASES

### 1. Demo/Presentation
```bash
python history_migration.py --action full
# Semua user punya data untuk demo
```

### 2. Development/Testing
```bash
python history_migration.py --action user --user-id "current-user-id"
# Test dengan data user tertentu
```

### 3. Database Reset
```bash
python history_migration.py --action clean --confirm-clean
python history_migration.py --action full
# Reset dan isi ulang database
```

## 🔗 INTEGRATION

### Dengan Android App:
1. Migration mengisi collection `repairs` di Firestore
2. HistoryActivity akan otomatis menampilkan data
3. Filter dan sorting berfungsi dengan data baru
4. Profile user tetap utuh (hanya repair data yang ditambah)

### Dengan Firebase:
1. Menggunakan Firebase Admin SDK
2. Batch operations untuk performance
3. Proper error handling
4. Respect Firestore limits dan quotas

## 📞 SUPPORT

Jika ada masalah:
1. Cek `migration.log` untuk detail error
2. Pastikan Firebase credentials benar
3. Test koneksi dengan `--action stats`
4. Cek Firestore rules dan permissions

---

**Happy Migration! 🎉**