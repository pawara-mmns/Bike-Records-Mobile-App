package com.example.data

import kotlinx.coroutines.flow.Flow

class BikeRepository(private val bikeDao: BikeDao) {

    val bikeDetails: Flow<BikeDetails?> = bikeDao.getBikeDetails()
    val allServiceLogs: Flow<List<ServiceLog>> = bikeDao.getAllServiceLogs()
    val allFuelLogs: Flow<List<FuelLog>> = bikeDao.getAllFuelLogs()
    val allExpenseLogs: Flow<List<ExpenseLog>> = bikeDao.getAllExpenseLogs()
    val allEmissionLogs: Flow<List<EmissionLog>> = bikeDao.getAllEmissionLogs()

    suspend fun saveBikeDetails(details: BikeDetails) {
        bikeDao.insertOrUpdateBikeDetails(details)
    }

    suspend fun insertServiceLog(log: ServiceLog) {
        bikeDao.insertServiceLog(log)
    }

    suspend fun deleteServiceLog(log: ServiceLog) {
        bikeDao.deleteServiceLog(log)
    }

    suspend fun insertFuelLog(log: FuelLog) {
        bikeDao.insertFuelLog(log)
    }

    suspend fun deleteFuelLog(log: FuelLog) {
        bikeDao.deleteFuelLog(log)
    }

    suspend fun insertExpenseLog(log: ExpenseLog) {
        bikeDao.insertExpenseLog(log)
    }

    suspend fun deleteExpenseLog(log: ExpenseLog) {
        bikeDao.deleteExpenseLog(log)
    }

    suspend fun insertEmissionLog(log: EmissionLog) {
        bikeDao.insertEmissionLog(log)
    }

    suspend fun deleteEmissionLog(log: EmissionLog) {
        bikeDao.deleteEmissionLog(log)
    }
}
