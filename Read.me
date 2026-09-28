# <p align="center"><img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" alt="Krypton Vault Logo" width="128" /><br>Krypton Vault</p>

<p align="center">
  <strong>Zero-Knowledge, 100% Air-Gapped Personal Credentials Manager & Peer-to-Peer Security Suite</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android_8.0+_(API_26--36)-34D399?style=for-the-badge&logo=android&logoColor=white" alt="Platform: Android">
  <img src="https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Cryptography-AES--256--GCM_%7C_PBKDF2-10B981?style=for-the-badge" alt="AES-256-GCM">
  <img src="https://img.shields.io/badge/Network_Permission-NONE_(Air--Gapped)-06B6D4?style=for-the-badge" alt="100% Offline">
  <img src="https://img.shields.io/badge/License-Apache_2.0-F59E0B?style=for-the-badge" alt="License">
</p>

---

## ⚡ Download the APK

The pre-compiled, cryptographically signed release APK is available directly on our GitHub Releases page:

👉 **[Download Latest Krypton Vault Release (.apk)](../../releases/latest)**

---

## 🛡️ Executive Summary & Security Philosophy

**Krypton Vault** is engineered from the ground up for individuals who refuse to trust cloud servers with their digital identity. Unlike mainstream password managers that synchronize encrypted vaults across remote third-party infrastructure, Krypton Vault operates under a strict **Zero-Internet, Zero-Knowledge** paradigm:

- **100% Air-Gapped**: The `android.permission.INTERNET` permission is **completely omitted** from the application manifest. It is physically impossible for the app to open network sockets, connect to cloud APIs, transmit telemetry, or leak metadata over IP networks.
- **Peer-to-Peer Synchronization**: Synchronize credentials between your own offline devices through direct encrypted peer-to-peer Bluetooth RFCOMM channels, eliminating intermediate cloud servers or public relays.
- **Plausible Deniability**: Under physical coercion or hostile inspection, entering an alternate Duress PIN opens a realistic decoy vault with innocuous dummy data stored strictly in volatile memory.

---

## 🎨 UI & Design Credits

A special acknowledgment and credit to:

### **[Rifatullah Ahmad Zubaer](https://github.com/rifatullah-ahmad-zubaer)**

For designing and architecting the tactile Neumorphic (Soft-UI) aesthetic, the custom pitch-black OLED zero-lag rendering engine, the interactive floating navigation dock, and the 12-theme color palette system that give Krypton Vault its signature look, feel, and fluid haptic responsiveness.

---

## 🌟 Key Features

### 1. 🔐 Military-Grade Zero-Knowledge Cryptography
- **PBKDF2 Key Derivation**: Master keys derived using PBKDF2 with HMAC-SHA256 at **120,000 iterations** run asynchronously off the main thread to ensure rock-solid security without UI stutter.
- **Quick PIN Wrapping**: Secondary PBKDF2 (60,000 iterations) wraps the in-memory master key for rapid everyday unlocking.
- **AES-256-GCM Encryption**: All sensitive payloads are encrypted with authenticated AES-256-GCM using unique, cryptographically secure 12-byte initialization vectors (`SecureRandom`) and 128-bit authentication tags.
- **Encrypted Local Database**: Stored in a local SQLite database encrypted with **SQLCipher** at rest.
- **RAM Hygiene & Zeroization**: Passwords, PINs, and keys are held in `CharArray` and `ByteArray` rather than immutable strings, wiped with zeroes (`.wipe()`) immediately upon use.

### 2. 🎭 Plausible Deniability (Duress Decoy Vault)
- Configure an alternate **Duress PIN** in security settings.
- If coerced into unlocking the device, typing the Duress PIN loads a **decoy vault** populated with realistic dummy accounts.
- Any actions in decoy mode are stored purely in volatile RAM and **never touch** the encrypted SQLCipher database.
- Settings menus for the duress feature automatically disappear during a decoy session to leave zero forensic evidence.

### 3. 📡 Air-Gapped Peer-to-Peer Bluetooth Sync
- Synchronize credentials between two offline devices over encrypted RFCOMM Bluetooth (`UUID: 9f2b1d34-58e1-4c92-b431-ec7b6f3a8d10`).
- **Mutual 4-Digit PIN Pairing**: Dynamically derives a single-use session key via PBKDF2 + AES-256-GCM.
- **Visual Diff & Conflict Resolution**: Displays imported items categorized into **NEW**, **UPDATED**, and **UNCHANGED** with an interactive side-by-side resolver (`[ Keep Mine ]` vs `[ Use Theirs ]`).
- **Batch Merge**: Commits chosen items directly into the encrypted local database with complete integrity verification.

### 4. 📊 Shannon Entropy Health & Security Audit
- On-device audit engine computing mathematical Shannon entropy:
  $$H = -\sum_{i=1}^{n} p_i \log_2(p_i)$$
- Detects weak passwords (<12 characters or <45 bits entropy), aged credentials (>180 days), and cross-account password reuse using SHA-256 hash deduplication without plaintext exposure.
- Real-time GPU brute-force crack time estimates with one-tap "Fix" buttons.

### 5. 🎲 Password & Diceware Passphrase Generator
- **Random Password Mode**: Configurable length (8–64 characters), character sets (uppercase, lowercase, numbers, symbols), and visual ambiguity filter (`1`, `l`, `I`, `0`, `O`).
- **Diceware Passphrase Mode**: Generates cryptographically secure multi-word passphrases (3–8 words) with customizable delimiters and capitalization.
- Accessible both as a dedicated full screen and as an instant contextual bottom sheet.

### 6. 🛡️ System & Operating System Hardening
- **`FLAG_SECURE`**: Strictly active across all windows to prevent screen recording, screenshots, and task switcher snapshots.
- **Biometric Integration**: Seamless Android Keystore fingerprint and facial authentication.
- **Scrambled PIN Keypad**: Randomized digit layout defeats shoulder surfing and thermal/smudge screen analysis.
- **Auto-Clearing Clipboard**: Automatically purges credentials from the Android clipboard after 30 seconds.
- **ProcessLifecycle-Aware Auto-Lock**: Automatically locks when switching apps or locking the device, with zero disruption during active in-app navigation.

### 7. 🎨 Neumorphic Engine & 12 OLED Themes
- Authentic tactile Neumorphic (Soft-UI) styling: extruded convex cards, concave sockets, and physical spring depress animations.
- **True OLED Pitch-Black (`#000000`)**: Elevation shadows are completely bypassed in dark mode to guarantee zero GPU overhead and buttery 60/120 FPS scrolling.
- **12 Dynamic Accent Themes**:
  1. *Monochrome (Black & White)*
  2. *Emerald (Zero-Knowledge Green)*
  3. *Cyber Cyan (Electric Neon)*
  4. *Neon Purple (Deep Tech)*
  5. *Crimson Red (Intense Ruby)*
  6. *Amber Gold (Warm Bronze)*
  7. *Sapphire Blue (Cobalt Tech)*
  8. *Green & White*
  9. *Yellow & Brown*
  10. *Red & White*
  11. *Cyan & Orange*
  12. *Purple & Gold*
- **Floating Pill Dock**: Animated stadium pill navigation dock with smooth spring transitions and haptic feedback.

---

## 🏗️ Technical Architecture

| Layer | Technologies & Implementations |
| :--- | :--- |
| **Language & Tooling** | Kotlin 2.0, JDK 17, Android Gradle Plugin 8.9+, Jetpack Compose |
| **Minimum / Target SDK** | Android 8.0 (API 26) → Android 15 (API 36) |
| **Database & ORM** | Room 2.6.1 + SQLCipher 4.5.4 |
| **Cryptographic Core** | `javax.crypto`, PBKDF2WithHmacSHA256, AES-256-GCM, `SecureRandom` |
| **Hardware Peripherals** | Android Bluetooth RFCOMM Sockets (Peer-to-Peer Sync) |
| **State & Concurrency** | Kotlin Coroutines, StateFlow, Android Jetpack ViewModel |
| **Lifecycle & Security** | `androidx.lifecycle.ProcessLifecycleOwner`, `androidx.biometric.BiometricPrompt` |
| **Autofill Provider** | Android System Autofill Framework (`AutofillService`) |

---

## 📂 Project Structure

```
krypton-vault/
├── app/
│   ├── build.gradle.kts                     # App module configuration & dependencies
│   ├── proguard-rules.pro                   # R8 optimization and obfuscation rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml          # Zero-internet permissions manifest
│           ├── java/com/example/
│           │   ├── KryptonApplication.kt    # App initialization & preferences setup
│           │   ├── MainActivity.kt          # Edge-to-edge Compose host & routing
│           │   ├── autofill/                # Android Autofill framework service
│           │   ├── bluetooth/               # P2P encrypted Bluetooth RFCOMM sync
│           │   ├── crypto/                  # PBKDF2, AES-GCM, Duress, Entropy
│           │   ├── data/                    # Room DB (SQLCipher) & repositories
│           │   ├── model/                   # Decrypted credential data models
│           │   ├── ui/
│           │   │   ├── components/          # Neumorphic engine & floating nav dock
│           │   │   ├── navigation/          # Compose screen destinations
│           │   │   ├── screens/             # Vault, Generator, Health, Settings, etc.
│           │   │   ├── theme/               # 12-theme palette & OLED pitch-black tokens
│           │   │   └── viewmodel/           # VaultViewModel & AuthViewModel
│           │   └── util/                    # LifecycleManager, ClipboardHelper, SecurityPrefs
│           └── res/                         # Vector icons, themes, and XML configurations
├── .github/workflows/
│   └── build-apk.yml                        # Automated CI APK build & release pipeline
├── build.gradle.kts                         # Root Gradle script
├── settings.gradle.kts                      # Gradle settings & dependency repos
├── memory.md                                # Comprehensive engineering log & bug catalog
└── Read.me                                  # Project documentation
```

---

## 🛠️ Building & Compiling from Source

### Prerequisites
- [Android Studio Ladybug (or newer)](https://developer.android.com/studio)
- JDK 17 (Temurin / OpenJDK)
- Android SDK with Platform 36 installed

### Build Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/krypton-vault.git
   cd krypton-vault
   ```

2. **Assemble Debug APK:**
   ```bash
   ./gradlew assembleDebug
   # APK location: app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Assemble Signed Release APK:**
   ```bash
   ./gradlew assembleRelease
   # APK location: app/build/outputs/apk/release/app-release.apk
   ```

---

## 📄 License

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for full details.

```
Copyright 2026 Krypton Vault Authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

---

<p align="center">
  <sub>Built with 🔒 Zero-Knowledge Cryptography & High-Fidelity Neumorphic Design.</sub>
</p>
