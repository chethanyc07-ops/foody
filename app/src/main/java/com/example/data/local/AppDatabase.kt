package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FoodCommodity
import com.example.data.model.PackagingMaterial
import com.example.data.model.SavedPackagingSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FoodCommodity::class,
        PackagingMaterial::class,
        SavedPackagingSpec::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commodityDao(): CommodityDao
    abstract fun materialDao(): MaterialDao
    abstract fun packagingSpecDao(): PackagingSpecDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_packaging_intelligence.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }

            suspend fun populateDatabase(db: AppDatabase) {
                db.commodityDao().insertCommodities(DefaultPackagingData.commodities)
                db.materialDao().insertMaterials(DefaultPackagingData.materials)
                db.packagingSpecDao().insertSpecs(DefaultPackagingData.sampleSpecs)
            }
        }
    }
}
