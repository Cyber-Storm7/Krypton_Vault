package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_items ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllItems(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): VaultItemEntity?

    @Query("SELECT * FROM vault_items WHERE category = :category ORDER BY isFavorite DESC, updatedAt DESC")
    fun getItemsByCategory(category: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY isFavorite DESC, updatedAt DESC")
    fun searchItems(query: String): Flow<List<VaultItemEntity>>

    @Query("SELECT passwordHashForDeduplication FROM vault_items WHERE passwordHashForDeduplication IS NOT NULL")
    suspend fun getAllPasswordHashes(): List<String>

    @Query("SELECT * FROM vault_items WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteItems(): Flow<List<VaultItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VaultItemEntity>)

    @Update
    suspend fun updateItem(item: VaultItemEntity): Int

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteItemById(id: String): Int

    @Query("DELETE FROM vault_items")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM vault_items")
    suspend fun getItemCount(): Int
}
