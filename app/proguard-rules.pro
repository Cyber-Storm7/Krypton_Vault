# Krypton Vault ProGuard / R8 Rules

# Keep line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# AndroidX Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-keep class * extends androidx.room.migration.Migration

# AndroidX SQLite & Security Crypto
-keep class androidx.sqlite.db.** { *; }
-keep class androidx.security.crypto.** { *; }

# AndroidX Biometrics & Fragments
-keep class androidx.biometric.** { *; }
-keep class androidx.fragment.app.** { *; }

# CameraX & ZXing QR Scanning
-keep class androidx.camera.** { *; }
-keep class com.google.zxing.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# App Models, Entities, DAOs, and Cryptographic components
-keep class com.example.model.** { *; }
-keep class com.example.data.entity.** { *; }
-keep class com.example.data.dao.** { *; }
-keep class com.example.data.database.** { *; }
-keep class com.example.crypto.** { *; }

# Suppress compile-time annotations from Tink and static analysis tools
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
