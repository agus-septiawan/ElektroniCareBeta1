#!/usr/bin/env python3
"""
ElektroniCare Interactive Migration Tool
=======================================

Interactive script untuk menjalankan migration dengan UI yang user-friendly.
"""

import os
import sys
import json
from typing import Optional
from history_migration import ElektroniCareMigration

def print_banner():
    """Print application banner"""
    print("=" * 60)
    print("🔧 ELEKTRONICARE MIGRATION TOOL")
    print("=" * 60)
    print("Tool untuk mengisi database dengan sample repair history")
    print("Version: 1.0")
    print("=" * 60)

def print_menu():
    """Print main menu"""
    print("\n📋 PILIHAN MIGRATION:")
    print("1. 🚀 Full Migration (Semua User)")
    print("2. 👤 Sample untuk User Tertentu")
    print("3. 📊 Lihat Statistik Database")
    print("4. 🗑️  Clean All Data (BAHAYA!)")
    print("5. ❌ Exit")
    print("-" * 40)

def get_user_choice() -> int:
    """Get user menu choice"""
    while True:
        try:
            choice = int(input("Pilih opsi (1-5): "))
            if 1 <= choice <= 5:
                return choice
            else:
                print("❌ Pilihan tidak valid. Masukkan angka 1-5.")
        except ValueError:
            print("❌ Masukkan angka yang valid.")

def confirm_action(message: str) -> bool:
    """Confirm dangerous actions"""
    print(f"\n⚠️  {message}")
    response = input("Ketik 'YES' untuk konfirmasi: ").strip()
    return response.upper() == 'YES'

def get_service_account_path() -> Optional[str]:
    """Get Firebase service account path"""
    print("\n🔑 FIREBASE AUTHENTICATION:")
    print("1. Gunakan default credentials (Google Cloud/Environment)")
    print("2. Gunakan service account JSON file")
    
    while True:
        try:
            auth_choice = int(input("Pilih metode autentikasi (1-2): "))
            if auth_choice == 1:
                return None
            elif auth_choice == 2:
                path = input("Masukkan path ke service account JSON: ").strip()
                if os.path.exists(path):
                    return path
                else:
                    print(f"❌ File tidak ditemukan: {path}")
            else:
                print("❌ Pilihan tidak valid. Masukkan 1 atau 2.")
        except ValueError:
            print("❌ Masukkan angka yang valid.")

def run_full_migration(migration: ElektroniCareMigration):
    """Run full migration with confirmation"""
    print("\n🚀 FULL MIGRATION")
    print("Akan membuat sample repair history untuk SEMUA user yang terdaftar.")
    print("Setiap user akan mendapat 8-12 repair records dengan data realistis.")
    
    if not confirm_action("Lanjutkan dengan full migration?"):
        print("❌ Migration dibatalkan.")
        return
    
    print("\n⏳ Menjalankan full migration...")
    print("Ini mungkin memakan waktu beberapa menit...")
    
    success = migration.run_full_migration()
    
    if success:
        print("\n✅ FULL MIGRATION BERHASIL!")
        print("Semua user sekarang memiliki sample repair history.")
        print("Silakan cek aplikasi untuk melihat hasilnya.")
    else:
        print("\n❌ FULL MIGRATION GAGAL!")
        print("Cek log file untuk detail error.")

def run_user_migration(migration: ElektroniCareMigration):
    """Run migration for specific user"""
    print("\n👤 USER MIGRATION")
    print("Membuat sample repair history untuk user tertentu.")
    
    user_id = input("Masukkan User ID: ").strip()
    if not user_id:
        print("❌ User ID tidak boleh kosong.")
        return
    
    print(f"\n⏳ Membuat sample data untuk user: {user_id}")
    
    success = migration.create_sample_for_user(user_id)
    
    if success:
        print(f"\n✅ SAMPLE DATA BERHASIL DIBUAT!")
        print(f"User {user_id} sekarang memiliki sample repair history.")
    else:
        print(f"\n❌ GAGAL MEMBUAT SAMPLE DATA!")
        print("User mungkin tidak ditemukan atau sudah memiliki data.")

def show_statistics(migration: ElektroniCareMigration):
    """Show database statistics"""
    print("\n📊 STATISTIK DATABASE")
    print("⏳ Mengambil data...")
    
    stats = migration.get_migration_stats()
    
    if not stats:
        print("❌ Gagal mengambil statistik.")
        return
    
    print("\n" + "=" * 50)
    print("📊 STATISTIK MIGRATION")
    print("=" * 50)
    print(f"👥 Total Users: {stats['total_users']}")
    print(f"🔧 Total Repairs: {stats['total_repairs']}")
    print(f"📈 Rata-rata Repairs per User: {stats['average_repairs_per_user']}")
    
    print("\n📋 Repairs berdasarkan Status:")
    print("-" * 30)
    for status, count in stats['repairs_by_status'].items():
        status_emoji = {
            'pending': '⏳',
            'pending_confirmation': '❓',
            'in_progress': '🔄',
            'completed': '✅',
            'cancelled': '❌'
        }.get(status, '📝')
        
        print(f"{status_emoji} {status}: {count}")
    
    print("=" * 50)

def clean_all_data(migration: ElektroniCareMigration):
    """Clean all repair data with multiple confirmations"""
    print("\n🗑️  CLEAN ALL DATA")
    print("⚠️  PERINGATAN: Ini akan menghapus SEMUA data repair!")
    print("⚠️  Operasi ini TIDAK BISA DI-UNDO!")
    print("⚠️  Gunakan hanya jika Anda yakin!")
    
    if not confirm_action("Anda yakin ingin menghapus SEMUA data repair?"):
        print("❌ Operasi dibatalkan.")
        return
    
    print("\n⚠️  KONFIRMASI TERAKHIR!")
    if not confirm_action("Ini adalah konfirmasi terakhir. Semua data akan hilang!"):
        print("❌ Operasi dibatalkan.")
        return
    
    print("\n⏳ Menghapus semua data repair...")
    print("Ini mungkin memakan waktu...")
    
    success = migration.clean_all_repair_data()
    
    if success:
        print("\n✅ SEMUA DATA REPAIR BERHASIL DIHAPUS!")
        print("Database sekarang kosong dari data repair.")
    else:
        print("\n❌ GAGAL MENGHAPUS DATA!")
        print("Cek log file untuk detail error.")

def main():
    """Main interactive function"""
    print_banner()
    
    # Get Firebase credentials
    service_account_path = get_service_account_path()
    
    # Initialize migration
    try:
        print("\n⏳ Menginisialisasi koneksi Firebase...")
        migration = ElektroniCareMigration(service_account_path)
        print("✅ Koneksi Firebase berhasil!")
    except Exception as e:
        print(f"❌ Gagal menginisialisasi Firebase: {e}")
        print("Pastikan credentials dan koneksi internet Anda benar.")
        sys.exit(1)
    
    # Main loop
    while True:
        print_menu()
        choice = get_user_choice()
        
        if choice == 1:
            run_full_migration(migration)
        elif choice == 2:
            run_user_migration(migration)
        elif choice == 3:
            show_statistics(migration)
        elif choice == 4:
            clean_all_data(migration)
        elif choice == 5:
            print("\n👋 Terima kasih telah menggunakan ElektroniCare Migration Tool!")
            break
        
        # Ask if user wants to continue
        if choice != 5:
            input("\nTekan Enter untuk kembali ke menu utama...")

if __name__ == "__main__":
    main()