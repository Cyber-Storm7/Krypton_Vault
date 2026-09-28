package com.example.crypto

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Zero-Knowledge Cryptographic Manager.
 * Handles PBKDF2 master key derivation, AES-256 GCM encryption,
 * Android Keystore biometric key wrapping, Quick PIN wrapping, and memory sanitization.
 * All CPU-intensive cryptographic operations are strictly offloaded to Dispatchers.Default.
 */
object CryptoManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val BIOMETRIC_KEY_ALIAS = "krypton_biometric_master_wrapper"
    private const val PREFS_NAME = "krypton_vault_security_prefs"
    private const val KEY_SALT = "krypton_master_salt"
    private const val KEY_VERIFIER = "krypton_master_verifier"
    private const val KEY_VERIFIER_IV = "krypton_master_verifier_iv"
    private const val KEY_BIOMETRIC_WRAPPED_KEY = "krypton_bio_wrapped_key"
    private const val KEY_BIOMETRIC_IV = "krypton_bio_iv"
    private const val KEY_BIOMETRIC_ENABLED = "krypton_bio_enabled"
    private const val KEY_VAULT_INITIALIZED = "krypton_vault_initialized"

    // Quick PIN Preferences keys
    private const val KEY_QUICK_PIN_ENABLED = "krypton_quick_pin_enabled"
    private const val KEY_QUICK_PIN_SALT = "krypton_quick_pin_salt"
    private const val KEY_QUICK_PIN_WRAPPED_KEY = "krypton_quick_pin_wrapped_key"
    private const val KEY_QUICK_PIN_IV = "krypton_quick_pin_iv"

    private const val PBKDF2_ITERATIONS = 120_000
    private const val QUICK_PIN_ITERATIONS = 60_000
    private const val PBKDF2_KEY_LENGTH = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val SALT_LENGTH = 32

    private val secureRandom = SecureRandom()

    // Holds decrypted master key in volatile memory while vault is unlocked
    @Volatile
    private var activeMasterKey: ByteArray? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isVaultInitialized(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VAULT_INITIALIZED, false)
    }

    fun isBiometricEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false) &&
                getPrefs(context).getString(KEY_BIOMETRIC_WRAPPED_KEY, null) != null
    }

    fun isQuickPinConfigured(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_QUICK_PIN_ENABLED, false) &&
                getPrefs(context).getString(KEY_QUICK_PIN_WRAPPED_KEY, null) != null
    }

    fun isUnlocked(): Boolean {
        return activeMasterKey != null || DuressVaultManager.isDecoyActive()
    }

    fun lockVault() {
        activeMasterKey?.wipe()
        activeMasterKey = null
        DuressVaultManager.resetDecoyState()
    }

    /**
     * Initializes the vault for the first time with a root alphanumeric master password.
     * Derivation runs strictly on Dispatchers.Default.
     */
    suspend fun initializeVault(context: Context, masterPassword: CharArray): Boolean = withContext(Dispatchers.Default) {
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)

        val derivedKey = deriveKeyInternal(masterPassword, salt, PBKDF2_ITERATIONS)
        masterPassword.wipe()

        return@withContext try {
            val verifierPlaintext = "KRYPTON_VAULT_VERIFIER_OK".toByteArray(StandardCharsets.UTF_8)
            val encryptionResult = encryptWithKeyInternal(verifierPlaintext, derivedKey)

            withContext(Dispatchers.IO) {
                val prefs = getPrefs(context)
                prefs.edit()
                    .putString(KEY_SALT, android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
                    .putString(KEY_VERIFIER, android.util.Base64.encodeToString(encryptionResult.ciphertext, android.util.Base64.NO_WRAP))
                    .putString(KEY_VERIFIER_IV, android.util.Base64.encodeToString(encryptionResult.iv, android.util.Base64.NO_WRAP))
                    .putBoolean(KEY_VAULT_INITIALIZED, true)
                    .apply()
            }

            activeMasterKey?.wipe()
            activeMasterKey = derivedKey.clone()
            true
        } finally {
            salt.wipe()
            derivedKey.wipe()
        }
    }

    /**
     * Configures a 6-digit Quick PIN that encrypts the active master key.
     * Derivation runs strictly on Dispatchers.Default.
     */
    suspend fun setQuickPin(context: Context, quickPin: CharArray): Boolean = withContext(Dispatchers.Default) {
        val masterKey = activeMasterKey ?: return@withContext false
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)

        val pinKey = deriveKeyInternal(quickPin, salt, QUICK_PIN_ITERATIONS)
        quickPin.wipe()

        return@withContext try {
            val encryptionResult = encryptWithKeyInternal(masterKey, pinKey)
            withContext(Dispatchers.IO) {
                getPrefs(context).edit()
                    .putString(KEY_QUICK_PIN_SALT, android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
                    .putString(KEY_QUICK_PIN_WRAPPED_KEY, android.util.Base64.encodeToString(encryptionResult.ciphertext, android.util.Base64.NO_WRAP))
                    .putString(KEY_QUICK_PIN_IV, android.util.Base64.encodeToString(encryptionResult.iv, android.util.Base64.NO_WRAP))
                    .putBoolean(KEY_QUICK_PIN_ENABLED, true)
                    .apply()
            }
            true
        } catch (_: Exception) {
            false
        } finally {
            salt.wipe()
            pinKey.wipe()
        }
    }

    /**
     * Clears Quick PIN configuration.
     */
    suspend fun clearQuickPin(context: Context) = withContext(Dispatchers.IO) {
        getPrefs(context).edit()
            .remove(KEY_QUICK_PIN_SALT)
            .remove(KEY_QUICK_PIN_WRAPPED_KEY)
            .remove(KEY_QUICK_PIN_IV)
            .putBoolean(KEY_QUICK_PIN_ENABLED, false)
            .apply()
    }

    /**
     * Unlocks the vault using the Quick PIN.
     * Silent decoy routing occurs if the candidate PIN matches the Duress PIN.
     */
    suspend fun unlockWithQuickPin(context: Context, quickPin: CharArray): Boolean = withContext(Dispatchers.Default) {
        // First check Duress PIN for plausible deniability
        if (DuressVaultManager.verifyDuressPin(context, quickPin)) {
            quickPin.wipe()
            DuressVaultManager.activateDecoyVault()
            return@withContext true
        }

        if (!isQuickPinConfigured(context)) {
            quickPin.wipe()
            return@withContext false
        }

        val prefs = withContext(Dispatchers.IO) { getPrefs(context) }
        val saltBase64 = prefs.getString(KEY_QUICK_PIN_SALT, null)
        val wrappedBase64 = prefs.getString(KEY_QUICK_PIN_WRAPPED_KEY, null)
        val ivBase64 = prefs.getString(KEY_QUICK_PIN_IV, null)

        if (saltBase64 == null || wrappedBase64 == null || ivBase64 == null) {
            quickPin.wipe()
            return@withContext false
        }

        val salt = android.util.Base64.decode(saltBase64, android.util.Base64.NO_WRAP)
        val wrapped = android.util.Base64.decode(wrappedBase64, android.util.Base64.NO_WRAP)
        val iv = android.util.Base64.decode(ivBase64, android.util.Base64.NO_WRAP)

        val pinKey = deriveKeyInternal(quickPin, salt, QUICK_PIN_ITERATIONS)
        quickPin.wipe()
        salt.wipe()

        return@withContext try {
            val unwrappedMasterKey = decryptWithKeyInternal(wrapped, iv, pinKey)
            activeMasterKey?.wipe()
            activeMasterKey = unwrappedMasterKey
            true
        } catch (_: Exception) {
            false
        } finally {
            pinKey.wipe()
        }
    }

    /**
     * Unlocks the vault using the root alphanumeric master password.
     * Silent decoy routing occurs if candidate password matches Duress PIN.
     */
    suspend fun unlockWithPassword(context: Context, masterPassword: CharArray): Boolean = withContext(Dispatchers.Default) {
        // Plausible deniability duress verification
        if (DuressVaultManager.verifyDuressPin(context, masterPassword)) {
            masterPassword.wipe()
            DuressVaultManager.activateDecoyVault()
            return@withContext true
        }

        val prefs = withContext(Dispatchers.IO) { getPrefs(context) }
        val saltBase64 = prefs.getString(KEY_SALT, null)
        val verifierBase64 = prefs.getString(KEY_VERIFIER, null)
        val verifierIvBase64 = prefs.getString(KEY_VERIFIER_IV, null)

        if (saltBase64 == null || verifierBase64 == null || verifierIvBase64 == null) {
            masterPassword.wipe()
            return@withContext false
        }

        val salt = android.util.Base64.decode(saltBase64, android.util.Base64.NO_WRAP)
        val verifier = android.util.Base64.decode(verifierBase64, android.util.Base64.NO_WRAP)
        val verifierIv = android.util.Base64.decode(verifierIvBase64, android.util.Base64.NO_WRAP)

        val candidateKey = deriveKeyInternal(masterPassword, salt, PBKDF2_ITERATIONS)
        masterPassword.wipe()
        salt.wipe()

        return@withContext try {
            val decryptedBytes = decryptWithKeyInternal(verifier, verifierIv, candidateKey)
            val verifierString = String(decryptedBytes, StandardCharsets.UTF_8)
            decryptedBytes.wipe()

            if (verifierString == "KRYPTON_VAULT_VERIFIER_OK") {
                activeMasterKey?.wipe()
                activeMasterKey = candidateKey.clone()
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        } finally {
            candidateKey.wipe()
        }
    }

    /**
     * Thread-safe AES payload encryption strictly executed on Dispatchers.Default.
     */
    suspend fun encryptPayload(data: ByteArray): EncryptionResult = withContext(Dispatchers.Default) {
        val key = activeMasterKey ?: throw IllegalStateException("Vault is locked")
        return@withContext encryptWithKeyInternal(data, key)
    }

    /**
     * Thread-safe AES payload decryption strictly executed on Dispatchers.Default.
     */
    suspend fun decryptPayload(ciphertext: ByteArray, iv: ByteArray): ByteArray = withContext(Dispatchers.Default) {
        val key = activeMasterKey ?: throw IllegalStateException("Vault is locked")
        return@withContext decryptWithKeyInternal(ciphertext, iv, key)
    }

    /**
     * Synchronous payload decryption helper when called inside an existing coroutine context.
     */
    fun decryptPayloadSync(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        val key = activeMasterKey ?: throw IllegalStateException("Vault is locked")
        return decryptWithKeyInternal(ciphertext, iv, key)
    }

    /**
     * Synchronous payload encryption helper when called inside an existing coroutine context.
     */
    fun encryptPayloadSync(data: ByteArray): EncryptionResult {
        val key = activeMasterKey ?: throw IllegalStateException("Vault is locked")
        return encryptWithKeyInternal(data, key)
    }

    fun deriveMasterKeySync(password: CharArray, salt: ByteArray): ByteArray {
        return deriveKeyInternal(password, salt, PBKDF2_ITERATIONS)
    }

    fun encryptWithKeySync(plaintext: ByteArray, keyBytes: ByteArray): EncryptionResult {
        return encryptWithKeyInternal(plaintext, keyBytes)
    }

    fun decryptWithKeySync(ciphertext: ByteArray, iv: ByteArray, keyBytes: ByteArray): ByteArray {
        return decryptWithKeyInternal(ciphertext, iv, keyBytes)
    }

    /**
     * PBKDF2WithHmacSHA256 key derivation.
     */
    private fun deriveKeyInternal(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, PBKDF2_KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val encoded = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return encoded
    }

    /**
     * AES-256 GCM encryption.
     */
    private fun encryptWithKeyInternal(plaintext: ByteArray, keyBytes: ByteArray): EncryptionResult {
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)

        val keySpec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

        val ciphertext = cipher.doFinal(plaintext)
        return EncryptionResult(ciphertext = ciphertext, iv = iv)
    }

    /**
     * AES-256 GCM decryption.
     */
    private fun decryptWithKeyInternal(ciphertext: ByteArray, iv: ByteArray, keyBytes: ByteArray): ByteArray {
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)
        return cipher.doFinal(ciphertext)
    }

    /**
     * Computes a SHA-256 hash string for password deduplication without storing plaintext.
     */
    suspend fun hashPassword(password: CharArray): String = withContext(Dispatchers.Default) {
        val bytes = Charsets.UTF_8.encode(java.nio.CharBuffer.wrap(password)).array()
        return@withContext try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            hashBytes.joinToString("") { "%02x".format(it) }
        } finally {
            bytes.wipe()
        }
    }

    fun hashPasswordSync(password: CharArray): String {
        val bytes = Charsets.UTF_8.encode(java.nio.CharBuffer.wrap(password)).array()
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            hashBytes.joinToString("") { "%02x".format(it) }
        } finally {
            bytes.wipe()
        }
    }

    // ==========================================
    // Biometric Keystore Wrapping Implementation
    // ==========================================

    private fun getOrCreateBiometricKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (keyStore.containsAlias(BIOMETRIC_KEY_ALIAS)) {
            val entry = keyStore.getEntry(BIOMETRIC_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) {
                return entry.secretKey
            }
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenSpec = KeyGenParameterSpec.Builder(
            BIOMETRIC_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    fun getBiometricCipherForEncryption(): Cipher {
        val key = getOrCreateBiometricKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    fun getBiometricCipherForDecryption(context: Context): Cipher? {
        val prefs = getPrefs(context)
        val ivBase64 = prefs.getString(KEY_BIOMETRIC_IV, null) ?: return null
        val iv = android.util.Base64.decode(ivBase64, android.util.Base64.NO_WRAP)
        val key = getOrCreateBiometricKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher
    }

    suspend fun enableBiometricUnlock(context: Context, cipher: Cipher): Boolean = withContext(Dispatchers.Default) {
        val masterKey = activeMasterKey ?: return@withContext false
        return@withContext try {
            val wrapped = cipher.doFinal(masterKey)
            val iv = cipher.iv

            withContext(Dispatchers.IO) {
                getPrefs(context).edit()
                    .putString(KEY_BIOMETRIC_WRAPPED_KEY, android.util.Base64.encodeToString(wrapped, android.util.Base64.NO_WRAP))
                    .putString(KEY_BIOMETRIC_IV, android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP))
                    .putBoolean(KEY_BIOMETRIC_ENABLED, true)
                    .apply()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun unlockWithBiometrics(context: Context, cipher: Cipher): Boolean = withContext(Dispatchers.Default) {
        val prefs = withContext(Dispatchers.IO) { getPrefs(context) }
        val wrappedBase64 = prefs.getString(KEY_BIOMETRIC_WRAPPED_KEY, null) ?: return@withContext false
        val wrapped = android.util.Base64.decode(wrappedBase64, android.util.Base64.NO_WRAP)

        return@withContext try {
            val unwrappedMasterKey = cipher.doFinal(wrapped)
            activeMasterKey?.wipe()
            activeMasterKey = unwrappedMasterKey
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun disableBiometrics(context: Context) = withContext(Dispatchers.IO) {
        getPrefs(context).edit()
            .remove(KEY_BIOMETRIC_WRAPPED_KEY)
            .remove(KEY_BIOMETRIC_IV)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }
}

data class EncryptionResult(
    val ciphertext: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionResult
        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!iv.contentEquals(other.iv)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}
