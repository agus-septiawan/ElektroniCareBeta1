# 🚀 Setup Cloudinary untuk Upload Foto Profile - Langkah demi Langkah

## 📋 **LANGKAH 1: Buat Cloudinary Account**

1. **Buka browser dan pergi ke**: https://cloudinary.com
2. **Klik "Sign Up for Free"**
3. **Isi form registrasi**:
   - Email: [email Anda]
   - Password: [password yang kuat]
   - Company name: "ElektroniCare" (atau nama lain)
4. **Verifikasi email** jika diminta
5. **Login ke dashboard**

## 📋 **LANGKAH 2: Dapatkan Credentials**

1. **Di Cloudinary Dashboard**, Anda akan melihat:
   ```
   Cloud name: [catat ini]
   API Key: [catat ini] 
   API Secret: [catat ini - klik "reveal" jika tersembunyi]
   ```

2. **Contoh credentials**:
   ```
   Cloud name: elektronicare-demo
   API Key: 123456789012345
   API Secret: abcdefghijklmnopqrstuvwxyz123
   ```

## 📋 **LANGKAH 3: Update CloudinaryConfig.kt**

1. **Buka file**: `app/src/main/java/com/example/elektronicarebeta1/cloudinary/CloudinaryConfig.kt`

2. **Ganti baris 12-14**:
   ```kotlin
   // SEBELUM:
   const val CLOUD_NAME = "your_cloud_name"
   const val API_KEY = "your_api_key"
   const val API_SECRET = "your_api_secret"
   
   // SESUDAH (ganti dengan credentials Anda):
   const val CLOUD_NAME = "elektronicare-demo"
   const val API_KEY = "123456789012345"
   const val API_SECRET = "abcdefghijklmnopqrstuvwxyz123"
   ```

## 📋 **LANGKAH 4: Setup Upload Presets**

1. **Di Cloudinary Dashboard**, pergi ke **Settings** (ikon gear) > **Upload**

2. **Scroll ke bawah ke "Upload presets"**

3. **Klik "Add upload preset"**

4. **Buat preset pertama**:
   - **Preset name**: `profile_images`
   - **Signing mode**: `Unsigned`
   - **Folder**: `profile_images`
   - **Klik "Save"**

5. **Buat preset kedua**:
   - **Preset name**: `repair_images`
   - **Signing mode**: `Unsigned`
   - **Folder**: `repair_images`
   - **Klik "Save"**

## 📋 **LANGKAH 5: Test Setup**

1. **Buka terminal di project folder**

2. **Jalankan verification script**:
   ```bash
   python3 verify_cloudinary_setup.py
   ```

3. **Hasil yang diharapkan**:
   ```
   🚀 Cloudinary Setup Verification for ElektroniCare
   ==================================================
   ✅ Cloud Name configured: elektronicare-demo
   ✅ CloudinaryConfig.kt properly configured
   ✅ API connection successful
   ✅ Upload presets found: profile_images, repair_images
   ✅ Test upload successful
   ==================================================
   🎉 All checks passed! Cloudinary is ready to use.
   ```

## 📋 **LANGKAH 6: Build dan Test Aplikasi**

1. **Build aplikasi**:
   ```bash
   ./gradlew assembleDebug
   ```

2. **Install di device/emulator**

3. **Test upload foto**:
   - Login ke aplikasi
   - Pergi ke Profile
   - Tap icon edit foto (pensil)
   - Grant permission jika diminta
   - Pilih foto dari gallery
   - Tap "Save Profile"
   - Foto harus ter-upload dan muncul di profile

## 🔧 **Troubleshooting**

### **Masalah: "Invalid credentials"**
**Solusi**: 
- Cek kembali Cloud Name, API Key, dan API Secret
- Pastikan tidak ada spasi atau karakter tambahan
- Copy-paste langsung dari dashboard

### **Masalah: "Upload preset not found"**
**Solusi**:
- Pastikan upload presets sudah dibuat di dashboard
- Cek nama preset harus persis sama: `profile_images` dan `repair_images`
- Pastikan signing mode adalah "Unsigned"

### **Masalah: "Permission denied"**
**Solusi**:
- Pastikan permission sudah di-grant di Settings > Apps > ElektroniCare > Permissions
- Restart aplikasi setelah grant permission

### **Masalah: Upload berhasil tapi foto tidak muncul**
**Solusi**:
- Cek internet connection
- Cek logcat untuk error messages
- Pastikan Glide loading dengan benar

## 📞 **Support**

Jika masih ada masalah:
1. **Cek logcat** untuk error messages detail
2. **Cek Cloudinary Dashboard** > Media Library untuk melihat uploaded files
3. **Run verification script** lagi untuk memastikan setup benar
4. **Restart aplikasi** setelah perubahan konfigurasi

## 🎯 **Expected Result**

Setelah setup lengkap:
- ✅ User bisa tap edit photo icon
- ✅ Permission dialog muncul (jika belum granted)
- ✅ Gallery picker terbuka
- ✅ User pilih foto
- ✅ Foto preview muncul di profile
- ✅ User save profile
- ✅ Foto ter-upload ke Cloudinary
- ✅ Profile image URL tersimpan di Firestore
- ✅ Foto profile ter-update di aplikasi

**Estimasi waktu setup**: 10-15 menit