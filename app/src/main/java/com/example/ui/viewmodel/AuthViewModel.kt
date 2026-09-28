package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.CryptoManager
import com.example.crypto.DuressVaultManager
import com.example.crypto.wipe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.crypto.Cipher

enum class UnlockInputMode {
    QUICK_PIN,
    MASTER_PASSWORD
}

data class AuthUiState(
    val isInitialized: Boolean = false,
    val isQuickPinConfigured: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val unlockMode: UnlockInputMode = UnlockInputMode.QUICK_PIN,
    val isUnlocked: Boolean = false,
    val lockoutSecondsRemaining: Int = 0,
    val failedAttempts: Int = 0,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

/**
 * Authentication ViewModel managing zero-knowledge credential verification,
 * dual-tier setup (Root Master Password + Optional Quick PIN),
 * and dynamic unlock mode transitions.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow(
        AuthUiState(
            isInitialized = CryptoManager.isVaultInitialized(application),
            isQuickPinConfigured = CryptoManager.isQuickPinConfigured(application),
            isBiometricAvailable = CryptoManager.isBiometricEnabled(application),
            unlockMode = if (CryptoManager.isQuickPinConfigured(application)) {
                UnlockInputMode.QUICK_PIN
            } else {
                UnlockInputMode.MASTER_PASSWORD
            },
            isUnlocked = CryptoManager.isUnlocked()
        )
    )
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private var lockoutJob: Job? = null

    init {
        refreshState()
    }

    fun refreshState() {
        val app = getApplication<Application>()
        val initialized = CryptoManager.isVaultInitialized(app)
        val pinConfigured = CryptoManager.isQuickPinConfigured(app)
        val bioEnabled = CryptoManager.isBiometricEnabled(app)
        val unlocked = CryptoManager.isUnlocked()

        _authState.update {
            it.copy(
                isInitialized = initialized,
                isQuickPinConfigured = pinConfigured,
                isBiometricAvailable = bioEnabled,
                isUnlocked = unlocked,
                unlockMode = if (pinConfigured) it.unlockMode else UnlockInputMode.MASTER_PASSWORD
            )
        }
    }

    fun toggleUnlockMode() {
        _authState.update { current ->
            val nextMode = if (current.unlockMode == UnlockInputMode.QUICK_PIN) {
                UnlockInputMode.MASTER_PASSWORD
            } else {
                if (current.isQuickPinConfigured) UnlockInputMode.QUICK_PIN else UnlockInputMode.MASTER_PASSWORD
            }
            current.copy(unlockMode = nextMode, errorMessage = null)
        }
    }

    /**
     * Initializes the vault with a mandatory 8+ character root master password,
     * and an optional 6-digit Quick PIN.
     */
    fun setupVault(
        masterPassword: CharArray,
        quickPin: CharArray? = null,
        onResult: (Boolean) -> Unit
    ) {
        if (masterPassword.size < 8) {
            masterPassword.wipe()
            quickPin?.wipe()
            _authState.update { it.copy(errorMessage = "Master password must be at least 8 characters.") }
            onResult(false)
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMessage = null) }
            val app = getApplication<Application>()
            val success = CryptoManager.initializeVault(app, masterPassword)

            if (success) {
                if (quickPin != null && quickPin.isNotEmpty()) {
                    CryptoManager.setQuickPin(app, quickPin)
                }
                _authState.update {
                    it.copy(
                        isInitialized = true,
                        isUnlocked = true,
                        isQuickPinConfigured = CryptoManager.isQuickPinConfigured(app),
                        isLoading = false,
                        errorMessage = null
                    )
                }
                onResult(true)
            } else {
                quickPin?.wipe()
                _authState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to initialize cryptographic master vault."
                    )
                }
                onResult(false)
            }
        }
    }

    /**
     * Unlocks using the root alphanumeric master password.
     */
    fun unlockWithMasterPassword(masterPassword: CharArray, onResult: (Boolean) -> Unit) {
        if (_authState.value.lockoutSecondsRemaining > 0) {
            masterPassword.wipe()
            onResult(false)
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMessage = null) }
            val app = getApplication<Application>()
            val success = CryptoManager.unlockWithPassword(app, masterPassword)

            if (success) {
                _authState.update {
                    it.copy(
                        isUnlocked = true,
                        isLoading = false,
                        failedAttempts = 0,
                        errorMessage = null
                    )
                }
                onResult(true)
            } else {
                handleFailedAttempt()
                _authState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Invalid Master Password."
                    )
                }
                onResult(false)
            }
        }
    }

    /**
     * Unlocks using the 6-digit Quick PIN.
     * Silent decoy routing is performed if candidate PIN matches Duress PIN.
     */
    fun unlockWithQuickPin(quickPin: CharArray, onResult: (Boolean) -> Unit) {
        if (_authState.value.lockoutSecondsRemaining > 0) {
            quickPin.wipe()
            onResult(false)
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMessage = null) }
            val app = getApplication<Application>()
            val success = CryptoManager.unlockWithQuickPin(app, quickPin)

            if (success) {
                _authState.update {
                    it.copy(
                        isUnlocked = true,
                        isLoading = false,
                        failedAttempts = 0,
                        errorMessage = null
                    )
                }
                onResult(true)
            } else {
                handleFailedAttempt()
                _authState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Incorrect Quick PIN."
                    )
                }
                onResult(false)
            }
        }
    }

    /**
     * Unlocks via Android Keystore BiometricPrompt cipher.
     */
    fun unlockWithBiometrics(cipher: Cipher, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val success = CryptoManager.unlockWithBiometrics(app, cipher)
            if (success) {
                _authState.update {
                    it.copy(
                        isUnlocked = true,
                        failedAttempts = 0,
                        errorMessage = null
                    )
                }
                onResult(true)
            } else {
                _authState.update { it.copy(errorMessage = "Biometric authentication failed.") }
                onResult(false)
            }
        }
    }

    fun lockVault() {
        CryptoManager.lockVault()
        refreshState()
    }

    private fun handleFailedAttempt() {
        val attempts = _authState.value.failedAttempts + 1
        val lockoutTime = when {
            attempts >= 10 -> 300 // 5 minutes
            attempts >= 5 -> 30   // 30 seconds
            attempts >= 3 -> 10   // 10 seconds
            else -> 0
        }

        _authState.update {
            it.copy(
                failedAttempts = attempts,
                lockoutSecondsRemaining = lockoutTime
            )
        }

        if (lockoutTime > 0) {
            startLockoutTimer(lockoutTime)
        }
    }

    private fun startLockoutTimer(seconds: Int) {
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            for (remaining in seconds downTo 1) {
                _authState.update { it.copy(lockoutSecondsRemaining = remaining) }
                delay(1000)
            }
            _authState.update { it.copy(lockoutSecondsRemaining = 0) }
        }
    }
}
