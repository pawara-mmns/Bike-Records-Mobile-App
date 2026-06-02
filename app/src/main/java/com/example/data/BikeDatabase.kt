package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BikeDetails::class,
        ServiceLog::class,
        FuelLog::class,
        ExpenseLog::class,
        EmissionLog::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BikeDatabase : RoomDatabase() {
    abstract fun bikeDao(): BikeDao

    companion object {
        @Volatile
        private var INSTANCE: BikeDatabase? = null

        fun getDatabase(context: Context): BikeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BikeDatabase::class.java,
                    "bike_records_database"
                )
                .fallbackToDestructiveMigration() // safe default for prototyping
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
