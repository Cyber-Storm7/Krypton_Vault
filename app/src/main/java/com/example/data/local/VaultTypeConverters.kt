package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.VaultItemType

class VaultTypeConverters {
    @TypeConverter
    fun fromVaultItemType(value: VaultItemType): String {
        return value.name
    }

    @TypeConverter
    fun toVaultItemType(value: String): VaultItemType {
        return try {
            VaultItemType.valueOf(value)
        } catch (_: Exception) {
            VaultItemType.LOGIN
        }
    }
}
