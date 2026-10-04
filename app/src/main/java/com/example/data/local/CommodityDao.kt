package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FoodCommodity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommodityDao {
    @Query("SELECT * FROM food_commodities ORDER BY name ASC")
    fun getAllCommodities(): Flow<List<FoodCommodity>>

    @Query("SELECT * FROM food_commodities WHERE category = :category ORDER BY name ASC")
    fun getCommoditiesByCategory(category: String): Flow<List<FoodCommodity>>

    @Query("SELECT * FROM food_commodities WHERE id = :id")
    suspend fun getCommodityById(id: Long): FoodCommodity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommodities(commodities: List<FoodCommodity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommodity(commodity: FoodCommodity): Long

    @Update
    suspend fun updateCommodity(commodity: FoodCommodity)

    @Query("SELECT COUNT(*) FROM food_commodities")
    suspend fun getCommodityCount(): Int
}
