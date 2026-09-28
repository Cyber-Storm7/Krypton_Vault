package com.example.data.repository

import com.example.crypto.CryptoManager
import com.example.crypto.DuressVaultManager
import com.example.crypto.EntropyCalculator
import com.example.crypto.HealthAuditEngine
import com.example.crypto.wipe
import com.example.data.local.VaultDao
import com.example.data.local.VaultItemEntity
import com.example.model.VaultItemDecrypted
import com.example.model.VaultItemSummary
import com.example.model.VaultItemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.UUID
data class SecurityAuditReport(
    val totalCount: Int,
    val weakCount: Int,
    val reusedCount: Int,
    val oldCount: Int,
    val flaggedItems: List<FlaggedVaultItem>
)

data class FlaggedVaultItem(
    val id: String,
    val title: String,
    val username: String,
    val isWeak: Boolean,
    val isReused: Boolean,
    val isOld: Boolean,
    val entropyBits: Double
)

class VaultRepository(private val vaultDao: VaultDao) {

    private val summaryCache = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, VaultItemSummary>>()
    private val totpCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    fun clearDecryptedCache() {
        summaryCache.clear()
        totpCache.clear()
    }

    val allSummaries: Flow<List<VaultItemSummary>> = kotlinx.coroutines.flow.combine(
        vaultDao.getAllItems().flowOn(Dispatchers.IO),
        DuressVaultManager.decoySummariesFlow
    ) { list, decoySummaries ->
        withContext(Dispatchers.Default) {
            if (DuressVaultManager.isDecoyActive()) {
                return@withContext decoySummaries
            }
            if (!CryptoManager.isUnlocked()) {
                summaryCache.clear()
                totpCache.clear()
                return@withContext list.map { entity ->
                    VaultItemSummary(
                        id = entity.id,
                        type = entity.type,
                        title = entity.title,
                        category = entity.category,
                        isFavorite = entity.isFavorite,
                        updatedAt = entity.updatedAt
                    )
                }
            }
            list.map { entity ->
                val cached = summaryCache[entity.id]
                if (cached != null && cached.first == entity.updatedAt) {
                    cached.second
                } else {
                    val summary = try {
                        val decryptedBytes = CryptoManager.decryptPayloadSync(entity.encryptedPayload, entity.iv)
                        val json = JSONObject(String(decryptedBytes, StandardCharsets.UTF_8))
                        decryptedBytes.wipe()
                        val username = json.optString("username", "")
                        val totp = json.optString("totpSecret", "")
                        if (totp.isNotBlank()) {
                            totpCache[entity.id] = totp
                        } else {
                            totpCache.remove(entity.id)
                        }
                        VaultItemSummary(
                            id = entity.id,
                            type = entity.type,
                            title = entity.title,
                            username = username,
                            category = entity.category,
                            isFavorite = entity.isFavorite,
                            hasTotp = totp.isNotBlank(),
                            totpSecret = totp,
                            updatedAt = entity.updatedAt
                        )
                    } catch (_: Exception) {
                        totpCache.remove(entity.id)
                        VaultItemSummary(
                            id = entity.id,
                            type = entity.type,
                            title = entity.title,
                            category = entity.category,
                            isFavorite = entity.isFavorite,
                            updatedAt = entity.updatedAt
                        )
                    }
                    summaryCache[entity.id] = entity.updatedAt to summary
                    summary
                }
            }
        }
    }

    suspend fun getItemById(id: String): VaultItemDecrypted? {
        if (DuressVaultManager.isDecoyActive()) {
            return DuressVaultManager.getDecoyItemById(id)
        }
        val entity = withContext(Dispatchers.IO) {
            vaultDao.getItemById(id)
        } ?: return null

        if (!CryptoManager.isUnlocked()) return null

        return withContext(Dispatchers.Default) {
            try {
                val decryptedBytes = CryptoManager.decryptPayload(entity.encryptedPayload, entity.iv)
                val decrypted = VaultItemDecrypted.fromPayloadBytes(
                    id = entity.id,
                    type = entity.type,
                    title = entity.title,
                    category = entity.category,
                    isFavorite = entity.isFavorite,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    payloadBytes = decryptedBytes
                )
                decryptedBytes.wipe()
                decrypted
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun saveItem(item: VaultItemDecrypted) {
        if (DuressVaultManager.isDecoyActive()) {
            DuressVaultManager.saveDecoyItem(item)
            return
        }
        if (!CryptoManager.isUnlocked()) throw IllegalStateException("Vault is locked")

        val (encResult, passwordHash) = withContext(Dispatchers.Default) {
            val payloadBytes = item.toPayloadBytes()
            val enc = CryptoManager.encryptPayload(payloadBytes)
            payloadBytes.wipe()

            val passHash = if (item.password.isNotEmpty()) {
                CryptoManager.hashPassword(item.password)
            } else {
                null
            }
            Pair(enc, passHash)
        }

        val entity = VaultItemEntity(
            id = if (item.id.isBlank()) UUID.randomUUID().toString() else item.id,
            type = item.type,
            title = item.title,
            encryptedPayload = encResult.ciphertext,
            iv = encResult.iv,
            category = item.category,
            isFavorite = item.isFavorite,
            createdAt = if (item.createdAt == 0L) System.currentTimeMillis() else item.createdAt,
            updatedAt = System.currentTimeMillis(),
            passwordHashForDeduplication = passwordHash
        )

        withContext(Dispatchers.IO) {
            vaultDao.insertItem(entity)
        }
    }

    suspend fun deleteItem(id: String) {
        summaryCache.remove(id)
        totpCache.remove(id)
        if (DuressVaultManager.isDecoyActive()) {
            DuressVaultManager.deleteDecoyItem(id)
            return
        }
        withContext(Dispatchers.IO) {
            vaultDao.deleteItemById(id)
        }
    }

    suspend fun toggleFavorite(id: String) {
        if (DuressVaultManager.isDecoyActive()) {
            DuressVaultManager.toggleDecoyFavorite(id)
            return
        }
        withContext(Dispatchers.IO) {
            val entity = vaultDao.getItemById(id) ?: return@withContext
            val updated = entity.copy(isFavorite = !entity.isFavorite, updatedAt = System.currentTimeMillis())
            vaultDao.updateItem(updated)
        }
    }

    suspend fun getDecryptedPassword(id: String): CharArray? {
        if (DuressVaultManager.isDecoyActive()) {
            return DuressVaultManager.getDecoyItemById(id)?.password
        }
        val item = getItemById(id) ?: return null
        val pass = item.password.clone()
        item.wipeSensitiveFields()
        return pass
    }

    suspend fun getDecryptedTotpSecret(id: String): String? {
        totpCache[id]?.let { return it }
        if (DuressVaultManager.isDecoyActive()) {
            return DuressVaultManager.getDecoyItemById(id)?.totpSecret
        }
        val item = getItemById(id) ?: return null
        val secret = item.totpSecret
        item.wipeSensitiveFields()
        if (secret.isNotBlank()) {
            totpCache[id] = secret
        }
        return secret.ifBlank { null }
    }

    /**
     * Retrieves all vault items decrypted into memory (strictly for Selective Sync Diff & in-memory merge).
     */
    suspend fun getAllItemsDecrypted(): List<VaultItemDecrypted> {
        if (DuressVaultManager.isDecoyActive()) {
            return DuressVaultManager.getDecoyItems()
        }
        if (!CryptoManager.isUnlocked()) return emptyList()

        val entities = withContext(Dispatchers.IO) {
            vaultDao.searchItems("").firstOrNull() ?: emptyList()
        }

        return withContext(Dispatchers.Default) {
            val result = mutableListOf<VaultItemDecrypted>()
            for (entity in entities) {
                val decrypted = getItemById(entity.id)
                if (decrypted != null) {
                    result.add(decrypted)
                }
            }
            result
        }
    }

    /**
     * Conducts a zero-knowledge security health audit across all vault credentials
     * utilizing HealthAuditEngine. Database fetches on IO, entropy/hash analysis on Default.
     */
    suspend fun runSecurityAudit(): SecurityAuditReport {
        if (!CryptoManager.isUnlocked()) {
            return SecurityAuditReport(0, 0, 0, 0, emptyList())
        }

        val (allHashes, flowList) = withContext(Dispatchers.IO) {
            val hashes = vaultDao.getAllPasswordHashes()
            val list = vaultDao.searchItems("").firstOrNull() ?: emptyList()
            Pair(hashes, list)
        }

        return withContext(Dispatchers.Default) {
            val reusedSet = HealthAuditEngine.findReusedHashes(allHashes)

            var weakCount = 0
            var reusedCount = 0
            var oldCount = 0
            val flagged = mutableListOf<FlaggedVaultItem>()

            for (entity in flowList) {
                val decryptedBytes = try {
                    CryptoManager.decryptPayload(entity.encryptedPayload, entity.iv)
                } catch (_: Exception) {
                    null
                } ?: continue

                val json = JSONObject(String(decryptedBytes, StandardCharsets.UTF_8))
                decryptedBytes.wipe()

                val username = json.optString("username", "")
                val passwordStr = json.optString("password", "")
                val passwordChars = passwordStr.toCharArray()

                val entropy = HealthAuditEngine.calculateShannonEntropy(passwordChars)
                val isWeak = HealthAuditEngine.isWeakPassword(passwordChars, entropy)
                passwordChars.wipe()

                val isReused = entity.passwordHashForDeduplication != null &&
                        reusedSet.contains(entity.passwordHashForDeduplication)

                val isOld = HealthAuditEngine.isOldPassword(entity.updatedAt)

                if (isWeak) weakCount++
                if (isReused) reusedCount++
                if (isOld) oldCount++

                if (isWeak || isReused || isOld) {
                    flagged.add(
                        FlaggedVaultItem(
                            id = entity.id,
                            title = entity.title,
                            username = username,
                            isWeak = isWeak,
                            isReused = isReused,
                            isOld = isOld,
                            entropyBits = (entropy * 10).toInt() / 10.0
                        )
                    )
                }
            }

            SecurityAuditReport(
                totalCount = flowList.size,
                weakCount = weakCount,
                reusedCount = reusedCount,
                oldCount = oldCount,
                flaggedItems = flagged
            )
        }
    }

    /**
     * Parses an encrypted .krypton backup into decrypted VaultItemDecrypted objects for Selective Sync Diff preview.
     */
    suspend fun parseBackupToItems(
        backupData: ByteArray,
        passphrase: CharArray
    ): List<VaultItemDecrypted> = withContext(Dispatchers.Default) {
        val processedData = try {
            val candidateText = String(backupData, StandardCharsets.UTF_8).trim()
            if (!candidateText.startsWith("KRP1")) {
                val decoded = android.util.Base64.decode(candidateText, android.util.Base64.DEFAULT)
                if (decoded.size >= 4 && String(decoded.copyOfRange(0, 4), StandardCharsets.US_ASCII) == "KRP1") {
                    decoded
                } else {
                    backupData
                }
            } else {
                backupData
            }
        } catch (_: Exception) {
            backupData
        }

        if (processedData.size < 48) throw IllegalArgumentException("Invalid backup file: corrupted size")

        var offset = 0
        val magic = ByteArray(4)
        System.arraycopy(processedData, offset, magic, 0, 4); offset += 4
        if (String(magic, StandardCharsets.US_ASCII) != "KRP1") {
            throw IllegalArgumentException("Unsupported or invalid backup format")
        }

        val salt = ByteArray(32)
        System.arraycopy(processedData, offset, salt, 0, 32); offset += 32

        val iv = ByteArray(12)
        System.arraycopy(processedData, offset, iv, 0, 12); offset += 12

        val ciphertext = ByteArray(processedData.size - offset)
        System.arraycopy(processedData, offset, ciphertext, 0, ciphertext.size)

        val backupKey = CryptoManager.deriveMasterKeySync(passphrase, salt)
        salt.wipe()

        val decryptedBytes = try {
            CryptoManager.decryptWithKeySync(ciphertext, iv, backupKey)
        } finally {
            backupKey.wipe()
        }

        val jsonString = String(decryptedBytes, StandardCharsets.UTF_8)
        decryptedBytes.wipe()

        val rootObj = JSONObject(jsonString)
        val itemsArray = rootObj.getJSONArray("items")

        val resultList = mutableListOf<VaultItemDecrypted>()
        for (i in 0 until itemsArray.length()) {
            val itemObj = itemsArray.getJSONObject(i)
            val id = itemObj.optString("id", UUID.randomUUID().toString())
            val typeStr = itemObj.optString("type", VaultItemType.LOGIN.name)
            val type = try { VaultItemType.valueOf(typeStr) } catch (_: Exception) { VaultItemType.LOGIN }
            val title = itemObj.optString("title", "Untitled")
            val category = itemObj.optString("category", "General")
            val isFavorite = itemObj.optBoolean("isFavorite", false)
            val createdAt = itemObj.optLong("createdAt", System.currentTimeMillis())
            val updatedAt = itemObj.optLong("updatedAt", System.currentTimeMillis())
            val payloadObj = itemObj.getJSONObject("payload")

            val item = VaultItemDecrypted(
                id = id,
                type = type,
                title = title,
                username = payloadObj.optString("username", ""),
                password = payloadObj.optString("password", "").toCharArray(),
                websiteUrl = payloadObj.optString("websiteUrl", ""),
                totpSecret = payloadObj.optString("totpSecret", ""),
                notes = payloadObj.optString("notes", ""),
                cardHolder = payloadObj.optString("cardHolder", ""),
                cardNumber = payloadObj.optString("cardNumber", ""),
                cardExpiry = payloadObj.optString("cardExpiry", ""),
                cardCvv = payloadObj.optString("cardCvv", "").toCharArray(),
                category = category,
                isFavorite = isFavorite,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
            resultList.add(item)
        }
        resultList
    }

    /**
     * Serializes decrypted vault items into an encrypted payload for peer-to-peer Bluetooth sync.
     */
    suspend fun createSyncPayload(passphrase: CharArray): ByteArray {
        if (!CryptoManager.isUnlocked()) throw IllegalStateException("Vault must be unlocked to sync")

        val flowList = withContext(Dispatchers.IO) {
            vaultDao.searchItems("").firstOrNull() ?: emptyList()
        }

        return withContext(Dispatchers.Default) {
            val jsonArray = JSONArray()
            for (entity in flowList) {
                val decryptedBytes = CryptoManager.decryptPayload(entity.encryptedPayload, entity.iv)
                val payloadString = String(decryptedBytes, StandardCharsets.UTF_8)
                decryptedBytes.wipe()

                val itemObj = JSONObject().apply {
                    put("id", entity.id)
                    put("type", entity.type.name)
                    put("title", entity.title)
                    put("category", entity.category)
                    put("isFavorite", entity.isFavorite)
                    put("createdAt", entity.createdAt)
                    put("updatedAt", entity.updatedAt)
                    put("payload", JSONObject(payloadString))
                }
                jsonArray.put(itemObj)
            }

            val exportRoot = JSONObject().apply {
                put("version", 1)
                put("format", "krypton-vault-sync")
                put("exportedAt", System.currentTimeMillis())
                put("items", jsonArray)
            }

            val plaintextBytes = exportRoot.toString().toByteArray(StandardCharsets.UTF_8)

            val salt = ByteArray(32)
            SecureRandom().nextBytes(salt)

            val syncKey = CryptoManager.deriveMasterKeySync(passphrase, salt)
            val encResult = CryptoManager.encryptWithKeySync(plaintextBytes, syncKey)
            plaintextBytes.wipe()
            syncKey.wipe()

            val magic = "KRP1".toByteArray(StandardCharsets.US_ASCII)
            val totalLength = magic.size + salt.size + encResult.iv.size + encResult.ciphertext.size
            val output = ByteArray(totalLength)

            var offset = 0
            System.arraycopy(magic, 0, output, offset, magic.size); offset += magic.size
            System.arraycopy(salt, 0, output, offset, salt.size); offset += salt.size
            System.arraycopy(encResult.iv, 0, output, offset, encResult.iv.size); offset += encResult.iv.size
            System.arraycopy(encResult.ciphertext, 0, output, offset, encResult.ciphertext.size)

            salt.wipe()
            output
        }
    }
}
