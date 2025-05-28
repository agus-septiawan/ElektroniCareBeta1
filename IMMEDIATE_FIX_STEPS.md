# IMMEDIATE FIX STEPS - Firestore Permission

## 🚨 LANGKAH DARURAT (Lakukan sekarang juga):

### STEP 1: Ganti Rules dengan Super Permissive
Di Firebase Console > Firestore Database > Rules, ganti dengan:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

**KLIK PUBLISH dan tunggu 2-3 menit!**

### STEP 2: Clear App Data Sepenuhnya
```bash
# Via ADB:
adb shell pm clear com.example.elektronicarebeta1

# Atau via Settings:
Settings > Apps > ElektroniCareBeta1 > Storage > Clear Data
```

### STEP 3: Restart Emulator/Device
Restart Android emulator atau device sepenuhnya.

### STEP 4: Test Aplikasi
- Buka aplikasi
- Coba login
- Cek logcat untuk error

## 🔍 JIKA MASIH ERROR:

### STEP 5: Verifikasi Project ID
Pastikan di Firebase Console Anda berada di project: **elektronicare-4a4dc**

### STEP 6: Check Authentication
Di Firebase Console > Authentication > Users, pastikan user dengan UID `SNTZpfi0QcczNxWIJ8UHklYsYwy1` ada.

### STEP 7: Network Test
- Coba dengan WiFi berbeda
- Disable VPN jika ada
- Pastikan internet stabil

## 🆘 JIKA SEMUA GAGAL:

### STEP 8: Recreate Firebase Project
1. Buat project Firebase baru
2. Download google-services.json baru
3. Replace file di app/
4. Setup Authentication dan Firestore dari awal

### STEP 9: Check Firebase SDK Version
Update ke versi terbaru jika perlu.

## ⚠️ CATATAN PENTING:
Rules `allow read, write: if true` sangat berbahaya untuk production!
Ini hanya untuk debugging. Setelah berhasil, ganti dengan rules yang aman.