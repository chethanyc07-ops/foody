package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SavedPackagingSpec
import kotlinx.coroutines.flow.Flow

@Dao
interface PackagingSpecDao {
    @Query("SELECT * FROM saved_packaging_specs ORDER BY timestamp DESC")
    fun getAllSpecs(): Flow<List<SavedPackagingSpec>>

    @Query("SELECT * FROM saved_packaging_specs WHERE status = :status ORDER BY timestamp DESC")
    fun getSpecsByStatus(status: String): Flow<List<SavedPackagingSpec>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpec(spec: SavedPackagingSpec): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecs(specs: List<SavedPackagingSpec>)

    @Update
    suspend fun updateSpec(spec: SavedPackagingSpec)

    @Delete
    suspend fun deleteSpec(spec: SavedPackagingSpec)

    @Query("DELETE FROM saved_packaging_specs WHERE id = :id")
    suspend fun deleteSpecById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_packaging_specs")
    suspend fun getSpecCount(): Int
}
