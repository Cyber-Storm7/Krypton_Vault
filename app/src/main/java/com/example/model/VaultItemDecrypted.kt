package com.example.model

import com.example.crypto.wipe
import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * In-memory decrypted representation of a vault record.
 * Sensitive fields (passwords, card CVVs) are stored as CharArray and must be wiped after use.
 */
data class VaultItemDecrypted(
    val id: String,
    val type: VaultItemType,
    val title: String,
    val username: String = "",
    val password: CharArray = CharArray(0),
    val websiteUrl: String = "",
    val totpSecret: String = "",
    val notes: String = "",
    val cardHolder: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvv: CharArray = CharArray(0),
    val category: String = "General",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Serializes decrypted fields to a UTF-8 JSON ByteArray for AES-GCM encryption.
     */
    fun toPayloadBytes(): ByteArray {
        val json = JSONObject().apply {
            put("username", username)
            put("password", String(password))
            put("websiteUrl", websiteUrl)
            put("totpSecret", totpSecret)
            put("notes", notes)
            put("cardHolder", cardHolder)
            put("cardNumber", cardNumber)
            put("cardExpiry", cardExpiry)
            put("cardCvv", String(cardCvv))
        }
        return json.toString().toByteArray(StandardCharsets.UTF_8)
    }

    /**
     * Securely clears all sensitive cryptographic arrays from memory.
     */
    fun wipeSensitiveFields() {
        password.wipe()
        cardCvv.wipe()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultItemDecrypted
        if (id != other.id) return false
        if (type != other.type) return false
        if (title != other.title) return false
        if (username != other.username) return false
        if (!password.contentEquals(other.password)) return false
        if (websiteUrl != other.websiteUrl) return false
        if (totpSecret != other.totpSecret) return false
        if (notes != other.notes) return false
        if (cardHolder != other.cardHolder) return false
        if (cardNumber != other.cardNumber) return false
        if (cardExpiry != other.cardExpiry) return false
        if (!cardCvv.contentEquals(other.cardCvv)) return false
        if (category != other.category) return false
        if (isFavorite != other.isFavorite) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + password.contentHashCode()
        result = 31 * result + websiteUrl.hashCode()
        result = 31 * result + totpSecret.hashCode()
        result = 31 * result + notes.hashCode()
        result = 31 * result + cardHolder.hashCode()
        result = 31 * result + cardNumber.hashCode()
        result = 31 * result + cardExpiry.hashCode()
        result = 31 * result + cardCvv.contentHashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }

    companion object {
        fun fromPayloadBytes(
            id: String,
            type: VaultItemType,
            title: String,
            category: String,
            isFavorite: Boolean,
            createdAt: Long,
            updatedAt: Long,
            payloadBytes: ByteArray
        ): VaultItemDecrypted {
            val jsonString = String(payloadBytes, StandardCharsets.UTF_8)
            val json = JSONObject(jsonString)

            val rawPassword = json.optString("password", "")
            val rawCvv = json.optString("cardCvv", "")

            return VaultItemDecrypted(
                id = id,
                type = type,
                title = title,
                username = json.optString("username", ""),
                password = rawPassword.toCharArray(),
                websiteUrl = json.optString("websiteUrl", ""),
                totpSecret = json.optString("totpSecret", ""),
                notes = json.optString("notes", ""),
                cardHolder = json.optString("cardHolder", ""),
                cardNumber = json.optString("cardNumber", ""),
                cardExpiry = json.optString("cardExpiry", ""),
                cardCvv = rawCvv.toCharArray(),
                category = category,
                isFavorite = isFavorite,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}
