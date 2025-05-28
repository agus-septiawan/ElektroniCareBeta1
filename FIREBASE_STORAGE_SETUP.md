# Firebase Storage Setup untuk Upload Foto Profile

## 🔍 Analisis Masalah

Fitur update foto profile belum berfungsi karena beberapa masalah:

1. **Missing Storage Permissions** - AndroidManifest.xml tidak memiliki permission untuk akses storage
2. **Firebase Storage Rules Tidak Dikonfigurasi** - Tidak ada rules untuk mengatur akses storage
3. **Runtime Permissions Tidak Dihandle** - Aplikasi tidak meminta permission saat runtime
4. **Firebase Storage Mungkin Belum Diaktifkan** - Perlu verifikasi di Firebase Console

## ✅ Solusi yang Telah Diimplementasi

### 1. **Android Permissions**
Ditambahkan ke `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

### 2. **Firebase Storage Rules**
Dibuat file `storage.rules`:
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Profile images - users can only upload/read their own profile images
    match /profile_images/{userId}-{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Repair images - users can only upload/read their own repair images
    match /repair_images/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read any image
    match /{allPaths=**} {
      allow read: if request.auth != null;
    }
  }
}
```

### 3. **Runtime Permissions**
Ditambahkan ke `ProfileActivity.kt`:
- Permission checking untuk Android 13+ (READ_MEDIA_IMAGES) dan Android 12- (READ_EXTERNAL_STORAGE)
- Request permission dialog
- Proper error handling

### 4. **Enhanced Error Handling**
Diperbaiki di `FirebaseManager.kt`:
- Detailed logging untuk upload process
- Specific error handling untuk StorageException
- Better filename generation dengan extension .jpg

### 5. **Configuration Update**
Updated `firebase.json`:
```json
{
  "storage": {
    "rules": "storage.rules"
  }
}
```

## 🚀 Langkah-langkah Setup

### 1. **Aktifkan Firebase Storage di Console**
1. Buka [Firebase Console](https://console.firebase.google.com/)
2. Pilih project `elektronicare-4a4dc`
3. Pergi ke **Storage** di sidebar
4. Klik **Get started**
5. Pilih **Start in production mode**
6. Pilih location (pilih yang terdekat dengan target user)
7. Klik **Done**

### 2. **Deploy Storage Rules**
```bash
# Install Firebase CLI jika belum ada
npm install -g firebase-tools

# Login ke Firebase
firebase login

# Deploy storage rules
python deploy_storage_rules.py
# atau manual:
firebase deploy --only storage
```

### 3. **Verifikasi Setup**
```bash
python verify_storage_setup.py
```

### 4. **Test di Aplikasi**
1. Build dan run aplikasi
2. Login dengan user
3. Pergi ke Profile
4. Tap icon edit foto
5. Pilih foto dari gallery
6. Save profile

## 🔧 Troubleshooting

### **Masalah: Permission Denied saat Upload**
**Solusi:**
1. Pastikan user sudah login (authenticated)
2. Cek storage rules sudah di-deploy
3. Cek permission di AndroidManifest.xml

### **Masalah: "Storage bucket not configured"**
**Solusi:**
1. Pastikan Firebase Storage sudah diaktifkan di console
2. Cek `google-services.json` memiliki `storage_bucket`
3. Re-download `google-services.json` dari console jika perlu

### **Masalah: "Permission denied" di Android**
**Solusi:**
1. Pastikan permission sudah ditambahkan di AndroidManifest.xml
2. Untuk Android 6.0+, pastikan runtime permission sudah di-request
3. Cek di Settings > Apps > ElektroniCare > Permissions

### **Masalah: Upload berhasil tapi gambar tidak muncul**
**Solusi:**
1. Cek network connection
2. Cek Glide loading dengan placeholder
3. Cek URL yang disimpan di Firestore

## 📱 Flow Upload Foto Profile

1. **User tap edit photo icon**
2. **Check storage permission**
   - Android 13+: READ_MEDIA_IMAGES
   - Android 12-: READ_EXTERNAL_STORAGE
3. **Request permission jika belum ada**
4. **Launch image picker**
5. **User pilih foto**
6. **Preview foto di ImageView**
7. **User tap save**
8. **Upload ke Firebase Storage**
   - Path: `profile_images/{userId}-{uuid}.jpg`
9. **Get download URL**
10. **Update Firestore user document**
11. **Refresh profile data**

## 🔒 Security Rules Explanation

```javascript
// Hanya user yang login bisa akses
if request.auth != null

// User hanya bisa upload/edit foto profile mereka sendiri
match /profile_images/{userId}-{allPaths=**} {
  allow read, write: if request.auth.uid == userId;
}

// Semua user yang login bisa read foto (untuk melihat profile user lain)
match /{allPaths=**} {
  allow read: if request.auth != null;
}
```

## 📊 File Structure

```
ElektroniCareBeta1/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # ✅ Updated permissions
│   │   └── java/.../ProfileActivity.kt  # ✅ Runtime permissions
│   ├── google-services.json             # ✅ Storage bucket configured
│   └── build.gradle.kts                 # ✅ Firebase Storage dependency
├── firebase.json                        # ✅ Storage rules config
├── storage.rules                        # ✅ New storage rules
├── deploy_storage_rules.py              # ✅ Deploy script
└── verify_storage_setup.py              # ✅ Verification script
```

## ✅ Checklist Setup

- [x] Firebase Storage dependency di build.gradle.kts
- [x] Storage permissions di AndroidManifest.xml
- [x] Runtime permission handling di ProfileActivity.kt
- [x] Firebase Storage rules dibuat
- [x] firebase.json dikonfigurasi untuk storage
- [x] Error handling diperbaiki di FirebaseManager.kt
- [x] Verification script dibuat
- [x] Deploy script dibuat
- [ ] **TODO: Aktifkan Firebase Storage di Console**
- [ ] **TODO: Deploy storage rules**
- [ ] **TODO: Test upload foto di aplikasi**

## 🎯 Expected Result

Setelah setup lengkap:
1. User bisa tap edit photo icon
2. Permission dialog muncul (jika belum granted)
3. Gallery picker terbuka
4. User pilih foto
5. Foto preview muncul di profile
6. User save profile
7. Foto ter-upload ke Firebase Storage
8. Profile image URL tersimpan di Firestore
9. Foto profile ter-update di aplikasi

## 📞 Support

Jika masih ada masalah:
1. Cek logcat untuk error messages
2. Cek Firebase Console > Storage untuk melihat uploaded files
3. Cek Firebase Console > Firestore untuk melihat updated profileImageUrl
4. Run verification script untuk memastikan setup benar