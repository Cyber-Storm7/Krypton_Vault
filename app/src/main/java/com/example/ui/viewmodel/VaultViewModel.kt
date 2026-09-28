package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.KryptonApplication
import com.example.crypto.CryptoManager
import com.example.crypto.DuressVaultManager
import com.example.crypto.wipe
import com.example.data.repository.SecurityAuditReport
import com.example.data.repository.VaultRepository
import com.example.model.VaultItemDecrypted
import com.example.model.VaultItemSummary
import com.example.model.VaultItemType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.crypto.Cipher

data class VaultUiState(
    val isInitialized: Boolean = false,
    val isUnlocked: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val lockoutSecondsRemaining: Int = 0,
    val failedAttempts: Int = 0,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val securityAudit: SecurityAuditReport? = null,
    val isAuditing: Boolean = false
)

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VaultRepository = (application as KryptonApplication).repository

    private val _uiState = MutableStateFlow(
        VaultUiState(
            isInitialized = CryptoManager.isVaultInitialized(application),
            isUnlocked = CryptoManager.isUnlocked(),
            isBiometricAvailable = CryptoManager.isBiometricEnabled(application)
        )
    )
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private var lockoutJob: Job? = null

    // Combined filtered items flow - strictly decoupled from unrelated uiState changes (lockout, status, etc)
    private val filterCriteria = _uiState
        .map { it.searchQuery to it.selectedCategory }
        .distinctUntilChanged()

    val filteredItems: StateFlow<List<VaultItemSummary>> = combine(
        repository.allSummaries,
        filterCriteria
    ) { summaries, (searchQuery, selectedCategory) ->
        summaries.filter { item ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Logins" -> item.type == VaultItemType.LOGIN
                "Notes" -> item.type == VaultItemType.SECURE_NOTE
                "Cards" -> item.type == VaultItemType.CREDIT_CARD
                "2FA" -> item.hasTotp
                else -> item.category.equals(selectedCategory, ignoreCase = true)
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                item.title.contains(searchQuery, ignoreCase = true) ||
                        item.username.contains(searchQuery, ignoreCase = true) ||
                        item.category.contains(searchQuery, ignoreCase = true)
            }

            matchesCategory && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun refreshState() {
        val app = getApplication<Application>()
        _uiState.update {
            it.copy(
                isInitialized = CryptoManager.isVaultInitialized(app),
                isUnlocked = CryptoManager.isUnlocked(),
                isBiometricAvailable = CryptoManager.isBiometricEnabled(app)
            )
        }
    }

    fun initializeVault(masterPassword: CharArray, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = CryptoManager.initializeVault(getApplication(), masterPassword)
            if (success) {
                _uiState.update {
                    it.copy(
                        isInitialized = true,
                        isUnlocked = true,
                        errorMessage = null,
                        statusMessage = "Vault initialized securely"
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to initialize vault") }
            }
            onDone(success)
        }
    }

    fun unlockWithPassword(masterPassword: CharArray, onDone: (Boolean) -> Unit = {}) {
        if (_uiState.value.lockoutSecondsRemaining > 0) {
            masterPassword.wipe()
            _uiState.update { it.copy(errorMessage = "Throttled: please wait ${it.lockoutSecondsRemaining}s") }
            onDone(false)
            return
        }

        viewModelScope.launch {
            // Plausible Deniability Duress Check:
            if (DuressVaultManager.verifyDuressPin(getApplication(), masterPassword)) {
                DuressVaultManager.activateDecoyVault()
                masterPassword.wipe()
                _uiState.update {
                    it.copy(
                        isUnlocked = true,
                        failedAttempts = 0,
                        errorMessage = null,
                        statusMessage = null
                    )
                }
                onDone(true)
                return@launch
            }

            val success = CryptoManager.unlockWithPassword(getApplication(), masterPassword)

            if (success) {
                _uiState.update {
                    it.copy(
                        isUnlocked = true,
                        failedAttempts = 0,
                        errorMessage = null,
                        statusMessage = null
                    )
                }
            } else {
                val newFails = _uiState.value.failedAttempts + 1
                var lockout = 0
                if (newFails >= 5) lockout = 30
                else if (newFails >= 4) lockout = 15
                else if (newFails >= 3) lockout = 5

                _uiState.update {
                    it.copy(
                        failedAttempts = newFails,
                        errorMessage = if (lockout > 0) "Incorrect password. Throttled for ${lockout}s" else "Incorrect master password",
                        lockoutSecondsRemaining = lockout
                    )
                }

                if (lockout > 0) {
                    startLockoutCountdown(lockout)
                }
            }
            onDone(success)
        }
    }

    private fun startLockoutCountdown(seconds: Int) {
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(lockoutSecondsRemaining = remaining) }
            }
        }
    }

    fun unlockWithBiometrics(cipher: Cipher, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = CryptoManager.unlockWithBiometrics(getApplication(), cipher)
            if (success) {
                _uiState.update {
                    it.copy(
                        isUnlocked = true,
                        failedAttempts = 0,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Biometric authentication failed") }
            }
            onDone(success)
        }
    }

    fun lockVault() {
        CryptoManager.lockVault()
        DuressVaultManager.resetDecoyState()
        repository.clearDecryptedCache()
        _uiState.update { it.copy(isUnlocked = false) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            repository.deleteItem(id)
            _uiState.update { it.copy(statusMessage = "Item deleted") }
        }
    }

    suspend fun getItemForEdit(id: String): VaultItemDecrypted? {
        return repository.getItemById(id)
    }

    suspend fun getAllItemsDecrypted(): List<VaultItemDecrypted> {
        return repository.getAllItemsDecrypted()
    }

    suspend fun parseBackupItems(bytes: ByteArray, passphrase: CharArray): List<VaultItemDecrypted> {
        return repository.parseBackupToItems(bytes, passphrase)
    }

    fun saveItem(item: VaultItemDecrypted, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.saveItem(item)
                item.wipeSensitiveFields()
                _uiState.update { it.copy(statusMessage = "Saved successfully") }
                onDone()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to save: ${e.message}") }
            }
        }
    }

    suspend fun getPasswordForCopy(id: String): CharArray? {
        return repository.getDecryptedPassword(id)
    }

    suspend fun getTotpSecret(id: String): String? {
        return repository.getDecryptedTotpSecret(id)
    }

    fun runSecurityAudit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuditing = true) }
            val report = repository.runSecurityAudit()
            _uiState.update { it.copy(securityAudit = report, isAuditing = false) }
        }
    }

    fun createSyncPayload(passphrase: CharArray, onResult: (ByteArray?) -> Unit) {
        viewModelScope.launch {
            try {
                val bytes = repository.createSyncPayload(passphrase)
                passphrase.wipe()
                onResult(bytes)
            } catch (e: Exception) {
                passphrase.wipe()
                _uiState.update { it.copy(errorMessage = "Sync packaging failed: ${e.message}") }
                onResult(null)
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
