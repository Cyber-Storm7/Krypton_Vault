package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.VaultItemType

/**
 * Encrypted persistent vault item stored in local database.
 * The payload is encrypted with AES-256 GCM using the user's derived master key.
 */
@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey
    val id: String,
    val type: VaultItemType,
    val title: String,
    val encryptedPayload: ByteArray,
    val iv: ByteArray,
    val category: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val passwordHashForDeduplication: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultItemEntity
        if (id != other.id) return false
        if (type != other.type) return false
        if (title != other.title) return false
        if (!encryptedPayload.contentEquals(other.encryptedPayload)) return false
        if (!iv.contentEquals(other.iv)) return false
        if (category != other.category) return false
        if (isFavorite != other.isFavorite) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (passwordHashForDeduplication != other.passwordHashForDeduplication) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + encryptedPayload.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + (passwordHashForDeduplication?.hashCode() ?: 0)
        return result
    }
}
