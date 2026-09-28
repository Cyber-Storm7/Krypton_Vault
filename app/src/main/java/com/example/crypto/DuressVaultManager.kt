package com.example.crypto

import android.content.Context
import android.content.SharedPreferences
import com.example.model.VaultItemDecrypted
import com.example.model.VaultItemSummary
import com.example.model.VaultItemType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

/**
 * Duress / Decoy Vault Manager (Plausible Deniability).
 * When duress mode is triggered via the Duress PIN, the app silently routes
 * to a convincing decoy vault with pre-seeded dummy accounts.
 *
 * Plausible Deniability Safeguards:
 * - isCurrentSessionDecoy flag is tracked for ambient UI suppression.
 * - In decoy mode, Duress settings are COMPLETELY suppressed so that a coercer
 *   cannot detect that a decoy vault feature exists or has been activated.
 * - All mutation attempts to real vault credentials or duress settings while in decoy mode are ignored.
 */
object DuressVaultManager {

    private const val PREFS_NAME = "krypton_duress_vault_prefs"
    private const val KEY_DURESS_ENABLED = "duress_vault_enabled"
    private const val KEY_DURESS_PIN_HASH = "duress_pin_sha256"

    @Volatile
    private var isDecoyActive: Boolean = false

    /**
     * Ambient session state flag indicating whether the current vault session is a decoy session.
     */
    val isCurrentSessionDecoy: Boolean
        get() = isDecoyActive

    // In-memory decoy items storage for decoy session
    private val decoyItems = mutableListOf<VaultItemDecrypted>()
    private val _decoySummariesFlow = MutableStateFlow<List<VaultItemSummary>>(emptyList())
    val decoySummariesFlow: StateFlow<List<VaultItemSummary>> = _decoySummariesFlow.asStateFlow()

    private fun notifyDecoyChanged() {
        _decoySummariesFlow.value = getDecoySummaries()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if duress vault protection is enabled in the real vault.
     * Always returns false if currently in a decoy session to guarantee stealth plausible deniability.
     */
    fun isDuressEnabled(context: Context): Boolean {
        if (isCurrentSessionDecoy) return false
        return getPrefs(context).getBoolean(KEY_DURESS_ENABLED, false) &&
                getPrefs(context).getString(KEY_DURESS_PIN_HASH, null) != null
    }

    /**
     * Enables or disables duress vault protection.
     * No-op if currently inside a decoy session to prevent state tampering under duress.
     */
    fun setDuressEnabled(context: Context, enabled: Boolean) {
        if (isCurrentSessionDecoy) return
        getPrefs(context).edit().putBoolean(KEY_DURESS_ENABLED, enabled).apply()
    }

    /**
     * Sets the secondary Duress PIN that unlocks the decoy vault.
     * No-op if currently inside a decoy session.
     */
    fun setDuressPin(context: Context, duressPin: CharArray) {
        if (isCurrentSessionDecoy) {
            duressPin.wipe()
            return
        }
        val hash = hashPin(duressPin)
        duressPin.wipe()
        getPrefs(context).edit()
            .putString(KEY_DURESS_PIN_HASH, hash)
            .putBoolean(KEY_DURESS_ENABLED, true)
            .apply()

        // Ensure decoy items are seeded
        seedDefaultDecoyItems()
    }

    /**
     * Validates whether candidate PIN matches the Duress PIN.
     */
    fun verifyDuressPin(context: Context, candidatePin: CharArray): Boolean {
        val storedHash = getPrefs(context).getString(KEY_DURESS_PIN_HASH, null) ?: return false
        val candidateHash = hashPin(candidatePin)
        return storedHash == candidateHash
    }

    fun activateDecoyVault() {
        isDecoyActive = true
        if (decoyItems.isEmpty()) {
            seedDefaultDecoyItems()
        } else {
            notifyDecoyChanged()
        }
    }

    fun isDecoyActive(): Boolean = isDecoyActive

    fun resetDecoyState() {
        isDecoyActive = false
        _decoySummariesFlow.value = emptyList()
    }

    private fun hashPin(pin: CharArray): String {
        val bytes = Charsets.UTF_8.encode(java.nio.CharBuffer.wrap(pin)).array()
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            hashBytes.joinToString("") { "%02x".format(it) }
        } finally {
            bytes.wipe()
        }
    }

    /**
     * Seeds realistic decoy credentials for plausible deniability.
     */
    fun populateDefaultDecoyItems() = seedDefaultDecoyItems()

    fun seedDefaultDecoyItems() {
        decoyItems.clear()
        decoyItems.addAll(
            listOf(
                VaultItemDecrypted(
                    id = "decoy_1",
                    type = VaultItemType.LOGIN,
                    title = "Netflix Family",
                    username = "sarah.jenkins88@gmail.com",
                    password = "RiverBend#2024!".toCharArray(),
                    websiteUrl = "https://netflix.com",
                    notes = "Family plan profile with Dave & kids",
                    category = "Entertainment",
                    isFavorite = true
                ),
                VaultItemDecrypted(
                    id = "decoy_2",
                    type = VaultItemType.LOGIN,
                    title = "Spotify Premium",
                    username = "sarah_music_vibes",
                    password = "Acoustic99*Guitar".toCharArray(),
                    websiteUrl = "https://spotify.com",
                    notes = "Family Duo subscription",
                    category = "Entertainment",
                    isFavorite = true
                ),
                VaultItemDecrypted(
                    id = "decoy_3",
                    type = VaultItemType.LOGIN,
                    title = "Amazon Prime",
                    username = "sarah.jenkins88@gmail.com",
                    password = "CottagePineapple$77".toCharArray(),
                    websiteUrl = "https://amazon.com",
                    notes = "Prime shipping address set to home",
                    category = "Shopping",
                    isFavorite = false
                ),
                VaultItemDecrypted(
                    id = "decoy_4",
                    type = VaultItemType.CREDIT_CARD,
                    title = "Chase Freedom Card",
                    username = "Sarah Jenkins",
                    password = "342".toCharArray(), // CVV
                    websiteUrl = "4111222233334589", // Card number
                    notes = "Exp: 09/28, Limit: $2,500",
                    category = "Finance",
                    isFavorite = false
                ),
                VaultItemDecrypted(
                    id = "decoy_5",
                    type = VaultItemType.SECURE_NOTE,
                    title = "Apartment Wi-Fi & Gate",
                    username = "",
                    password = "OakRidgeGuest!2024".toCharArray(),
                    websiteUrl = "",
                    notes = "Gate code: #4921, Package locker: 8812",
                    category = "Personal",
                    isFavorite = true
                )
            )
        )
        notifyDecoyChanged()
    }

    fun getDecoySummaries(): List<VaultItemSummary> {
        return decoyItems.map { item ->
            VaultItemSummary(
                id = item.id,
                type = item.type,
                title = item.title,
                username = item.username,
                category = item.category,
                isFavorite = item.isFavorite,
                hasTotp = item.totpSecret.isNotBlank(),
                totpSecret = item.totpSecret,
                updatedAt = item.updatedAt
            )
        }
    }

    fun getDecoyItems(): List<VaultItemDecrypted> {
        return decoyItems.map { it.copy(password = it.password.clone()) }
    }

    fun getDecoyItemById(id: String): VaultItemDecrypted? {
        val found = decoyItems.firstOrNull { it.id == id } ?: return null
        return found.copy(password = found.password.clone())
    }

    fun saveDecoyItem(item: VaultItemDecrypted) {
        val index = decoyItems.indexOfFirst { it.id == item.id }
        val toSave = item.copy(
            id = if (item.id.isBlank() || item.id == "new") UUID.randomUUID().toString() else item.id,
            password = item.password.clone()
        )
        if (index >= 0) {
            decoyItems[index] = toSave
        } else {
            decoyItems.add(0, toSave)
        }
        notifyDecoyChanged()
    }

    fun deleteDecoyItem(id: String) {
        decoyItems.removeAll { it.id == id }
        notifyDecoyChanged()
    }

    fun toggleDecoyFavorite(id: String) {
        val index = decoyItems.indexOfFirst { it.id == id }
        if (index >= 0) {
            val old = decoyItems[index]
            decoyItems[index] = old.copy(isFavorite = !old.isFavorite)
            notifyDecoyChanged()
        }
    }
}
