# 🚨 MANUAL EMERGENCY FIX STEPS

## MASALAH YANG DITEMUKAN:
- Aplikasi langsung masuk dashboard tanpa login yang benar
- Firebase Auth state tersimpan dari session sebelumnya
- PERMISSION_DENIED error karena Firestore tidak ter-setup dengan benar

## 🔧 LANGKAH PERBAIKAN MANUAL:

### STEP 1: CLEAR APP DATA DI EMULATOR
```bash
# Di terminal/command prompt:
adb shell pm clear com.example.elektronicarebeta1
```

**ATAU** di Android Emulator:
1. Settings → Apps → ElektroniCareBeta1
2. Storage → Clear Data
3. Clear Cache

### STEP 2: REBUILD & INSTALL FRESH
```bash
# Di Android Studio atau terminal:
./gradlew clean
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**ATAU** di Android Studio:
1. Build → Clean Project
2. Build → Rebuild Project
3. Run → Run 'app'

### STEP 3: VERIFIKASI FIRESTORE DATABASE
1. Buka: https://console.firebase.google.com/project/elektronicare-4a4dc/firestore
2. **PASTIKAN DATABASE SUDAH DIBUAT!**
3. Jika belum ada, klik **"Create database"**
4. Pilih **"Test mode"** untuk sementara
5. Pilih region (us-central1 recommended)

### STEP 4: APPLY SUPER PERMISSIVE RULES
Di Firebase Console → Firestore → Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // EMERGENCY RULES - ALLOW ALL ACCESS
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

**PENTING:** Klik **"Publish"** dan tunggu 2-3 menit!

### STEP 5: TEST APLIKASI
1. Buka aplikasi di emulator
2. **SEHARUSNYA** masuk ke Welcome/Onboarding screen (BUKAN dashboard)
3. Lakukan proses login dengan email/password
4. Setelah login berhasil, masuk dashboard tanpa error

## 🎯 EXPECTED BEHAVIOR:

### ✅ YANG BENAR:
- App start → Splash → Welcome/Login screen
- User login dengan email/password
- Dashboard muncul tanpa PERMISSION_DENIED error

### ❌ JIKA MASIH SALAH:
- App langsung ke dashboard
- Masih ada PERMISSION_DENIED error

## 🔍 TROUBLESHOOTING LANJUTAN:

### Jika masih langsung ke dashboard:
1. Pastikan kode SplashActivity sudah ter-update (ada `auth.signOut()`)
2. Clear app data lagi
3. Restart emulator sepenuhnya

### Jika masih PERMISSION_DENIED:
1. Cek Firestore Database benar-benar sudah dibuat
2. Cek rules sudah ter-publish dan tunggu propagasi
3. Cek Authentication → Sign-in method → Email/Password enabled

### Jika login gagal:
1. Cek Authentication → Users → pastikan ada user test
2. Atau buat user baru di register screen

## 🚨 NUCLEAR OPTION:
Jika semua gagal, buat Firebase project baru:
1. Firebase Console → Create new project
2. Download google-services.json baru
3. Replace file di app/google-services.json
4. Setup Firestore + Auth dari awal

## 📞 SUPPORT:
Jika masih bermasalah, kirim screenshot:
1. Firebase Console → Firestore (apakah database ada?)
2. Firebase Console → Rules (apakah rules sudah benar?)
3. App behavior (masuk dashboard atau login screen?)
4. Logcat output saat app start