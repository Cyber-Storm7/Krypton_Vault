package com.example.model

/**
 * Lightweight representation of an item for list display without keeping full
 * decrypted secret credentials in memory.
 */
data class VaultItemSummary(
    val id: String,
    val type: VaultItemType,
    val title: String,
    val username: String = "",
    val category: String = "General",
    val isFavorite: Boolean = false,
    val hasTotp: Boolean = false,
    val totpSecret: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
