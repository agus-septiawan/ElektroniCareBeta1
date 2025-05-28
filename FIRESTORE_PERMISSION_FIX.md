# Firestore Permission Fix Guide

## Masalah
Aplikasi mengalami error "PERMISSION_DENIED" saat mencoba menulis data ke Firestore karena security rules yang terlalu ketat.

## Error Log
```
[Firestore]: Write failed at users/SNTZpfi0QcczNxWIJ8UHklYsYwy1: Status{code=PERMISSION_DENIED, description=Missing or insufficient permissions., cause=null}
```

## Solusi

### Opsi 1: Menggunakan Firebase Console (Tercepat)

1. Buka [Firebase Console](https://console.firebase.google.com/)
2. Pilih project Anda
3. Klik "Firestore Database" di menu kiri
4. Klik tab "Rules"
5. Ganti rules yang ada dengan:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // DEVELOPMENT RULES - Allow all authenticated users to read and write
    // TODO: Restrict these rules in production environment
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

6. Klik "Publish"

### Opsi 2: Menggunakan Firebase CLI

1. Install Firebase CLI:
```bash
npm install -g firebase-tools
```

2. Login ke Firebase:
```bash
firebase login
```

3. Initialize Firestore (jika belum):
```bash
firebase init firestore
```

4. Deploy rules:
```bash
firebase deploy --only firestore:rules
```

### Opsi 3: Menggunakan Script Python

Jalankan script yang sudah disediakan:
```bash
python3 deploy_firestore_rules.py
```

## Penjelasan Rules

Rules yang baru memungkinkan:
- Semua user yang sudah login (authenticated) dapat membaca dan menulis data
- Ini adalah rules untuk development, bukan production
- Untuk production, rules harus lebih ketat

## Rules Production (Untuk Nanti)

Untuk production, gunakan rules yang lebih ketat:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own user document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // All authenticated users can read services and technicians
    match /services/{serviceId} {
      allow read: if request.auth != null;
    }
    
    match /technicians/{technicianId} {
      allow read: if request.auth != null;
    }
    
    // Users can only access their own repairs
    match /repairs/{repairId} {
      allow read, write: if request.auth != null && 
        (resource.data.userId == request.auth.uid || 
         request.resource.data.userId == request.auth.uid);
    }
  }
}
```

## Verifikasi

Setelah mengupdate rules:
1. Restart aplikasi Android
2. Coba login lagi
3. Error "PERMISSION_DENIED" seharusnya hilang
4. User data akan tersimpan di Firestore

## Troubleshooting

Jika masih ada masalah:
1. Pastikan user sudah login dengan benar (cek Firebase Auth di console)
2. Pastikan rules sudah ter-deploy (cek timestamp di Firebase Console)
3. Cek network connectivity
4. Clear app data dan coba lagi