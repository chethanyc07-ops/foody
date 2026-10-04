package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PackagingMaterial
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM packaging_materials ORDER BY circularityScore DESC")
    fun getAllMaterials(): Flow<List<PackagingMaterial>>

    @Query("SELECT * FROM packaging_materials WHERE category = :category ORDER BY circularityScore DESC")
    fun getMaterialsByCategory(category: String): Flow<List<PackagingMaterial>>

    @Query("SELECT * FROM packaging_materials WHERE id = :id")
    suspend fun getMaterialById(id: Long): PackagingMaterial?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<PackagingMaterial>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: PackagingMaterial): Long

    @Update
    suspend fun updateMaterial(material: PackagingMaterial)

    @Query("SELECT COUNT(*) FROM packaging_materials")
    suspend fun getMaterialCount(): Int
}
