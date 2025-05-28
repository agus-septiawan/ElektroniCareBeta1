# Troubleshooting Firestore Permission Issues

## Status: Rules sudah diupdate tapi masih error PERMISSION_DENIED

## Solusi yang harus dicoba (berurutan):

### 1. FORCE REFRESH RULES (PALING PENTING)
Rules mungkin belum ter-propagate. Lakukan:
- Di Firebase Console, klik tombol "Publish" lagi
- Tunggu 2-3 menit untuk propagation
- Restart aplikasi Android sepenuhnya

### 2. CLEAR APP DATA & CACHE
```bash
# Di Android device/emulator:
adb shell pm clear com.example.elektronicarebeta1
```
Atau melalui Settings > Apps > ElektroniCareBeta1 > Storage > Clear Data

### 3. VERIFIKASI AUTHENTICATION
Pastikan user benar-benar authenticated:
- Cek di Firebase Console > Authentication > Users
- User ID: SNTZpfi0QcczNxWIJ8UHklYsYwy1 harus ada

### 4. TEST RULES DI RULES PLAYGROUND
Di Firebase Console > Firestore > Rules:
- Klik "Rules Playground"
- Test dengan:
  - Operation: get
  - Path: /users/SNTZpfi0QcczNxWIJ8UHklYsYwy1
  - Auth: Authenticated user dengan UID: SNTZpfi0QcczNxWIJ8UHklYsYwy1

### 5. TEMPORARY SUPER PERMISSIVE RULES
Jika masih error, coba rules ini sementara:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;  // ALLOW ALL - DEVELOPMENT ONLY
    }
  }
}
```

### 6. CHECK PROJECT CONFIGURATION
Pastikan:
- google-services.json sesuai dengan project yang benar
- Project ID di Firebase Console sama dengan di aplikasi

### 7. NETWORK & CONNECTIVITY
- Pastikan internet stabil
- Coba dengan WiFi berbeda
- Disable VPN jika ada

### 8. FIREBASE SDK VERSION
Update Firebase SDK ke versi terbaru jika perlu.