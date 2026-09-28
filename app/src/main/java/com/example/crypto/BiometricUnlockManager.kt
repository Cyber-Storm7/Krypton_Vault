package com.example.crypto

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

/**
 * Instant Fingerprint / Biometric Auto-Unlock Manager.
 * Orchestrates AndroidX BiometricPrompt for zero-friction unlocking and
 * hardware-backed Keystore master key unwrap.
 */
object BiometricUnlockManager {

    /**
     * Checks if the device has biometric hardware enrolled and ready.
     */
    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val status = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return status == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Checks if biometric unlock has been configured and enabled by the user for KryptonVault.
     */
    fun isBiometricUnlockReady(context: Context): Boolean {
        return canAuthenticate(context) && CryptoManager.isBiometricEnabled(context)
    }

    /**
     * Launches the biometric prompt immediately with cipher-backed hardware validation.
     */
    fun launchImmediateBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: (Cipher) -> Unit,
        onFallbackToPin: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cipher = CryptoManager.getBiometricCipherForDecryption(activity)
        if (cipher == null) {
            onFallbackToPin()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("KryptonVault Biometric Unlock")
            .setSubtitle("Confirm your fingerprint to access encrypted secrets")
            .setNegativeButtonText("Use PIN / Master Password")
            .setConfirmationRequired(false)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val authenticatedCipher = result.cryptoObject?.cipher ?: cipher
                    onSuccess(authenticatedCipher)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    ) {
                        onFallbackToPin()
                    } else {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Prompt remains active for retry, but notify error handler
                }
            }
        )

        try {
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } catch (e: Exception) {
            // In case keystore key is invalidated or requires lockscreen
            onError(e.localizedMessage ?: "Biometric prompt error")
            onFallbackToPin()
        }
    }
}
