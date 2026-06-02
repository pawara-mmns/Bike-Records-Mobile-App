package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeDao {
    // Bike Details
    @Query("SELECT * FROM bike_details WHERE id = 1 LIMIT 1")
    fun getBikeDetails(): Flow<BikeDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBikeDetails(bikeDetails: BikeDetails)

    // Service Logs
    @Query("SELECT * FROM service_logs ORDER BY date DESC")
    fun getAllServiceLogs(): Flow<List<ServiceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceLog(log: ServiceLog)

    @Delete
    suspend fun deleteServiceLog(log: ServiceLog)

    // Fuel Logs
    @Query("SELECT * FROM fuel_logs ORDER BY date DESC")
    fun getAllFuelLogs(): Flow<List<FuelLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelLog(log: FuelLog)

    @Delete
    suspend fun deleteFuelLog(log: FuelLog)

    // Expense Logs
    @Query("SELECT * FROM expense_logs ORDER BY date DESC")
    fun getAllExpenseLogs(): Flow<List<ExpenseLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseLog(log: ExpenseLog)

    @Delete
    suspend fun deleteExpenseLog(log: ExpenseLog)

    // Emission Logs
    @Query("SELECT * FROM emission_logs ORDER BY date DESC")
    fun getAllEmissionLogs(): Flow<List<EmissionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmissionLog(log: EmissionLog)

    @Delete
    suspend fun deleteEmissionLog(log: EmissionLog)
}
