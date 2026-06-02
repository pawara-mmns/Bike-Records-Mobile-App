package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class BikeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BikeRepository

    val isTrackingActive: StateFlow<Boolean> = LocationService.isServiceRunning
    val sessionTrackedDistance: StateFlow<Double> = LocationService.sessionDistance
    val lastLocation: StateFlow<Location?> = LocationService.lastLocation

    init {
        val database = BikeDatabase.getDatabase(application)
        repository = BikeRepository(database.bikeDao())
    }

    // Bike details flow. If null, we'll expose a default entity
    val bikeDetails: StateFlow<BikeDetails> = repository.bikeDetails
        .map { it ?: BikeDetails(id = 1, brand = "Brand Name", model = "Model Name", year = 2024, currentOdometer = 0.0) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BikeDetails(id = 1, brand = "", model = "My Bike", year = 2024, currentOdometer = 0.0)
        )

    val allServiceLogs: StateFlow<List<ServiceLog>> = repository.allServiceLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFuelLogs: StateFlow<List<FuelLog>> = repository.allFuelLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenseLogs: StateFlow<List<ExpenseLog>> = repository.allExpenseLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEmissionLogs: StateFlow<List<EmissionLog>> = repository.allEmissionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Initialize with a standard starting bike if empty so there is zero empty-state friction
    fun initializeIfEmpty() {
        viewModelScope.launch {
            val current = repository.bikeDetails.first()
            if (current == null) {
                // Initialize default bike details
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 45) // Insurance expires in 45 days
                val insuranceExpiry = calendar.timeInMillis
                
                calendar.add(Calendar.DAY_OF_YEAR, 120) // Emission expires in 165 days total
                val emissionExpiry = calendar.timeInMillis

                val calendarRev = Calendar.getInstance()
                calendarRev.add(Calendar.DAY_OF_YEAR, 75) // Revenue license expires in 75 days
                val revenueLicenseExpiry = calendarRev.timeInMillis

                repository.saveBikeDetails(
                    BikeDetails(
                        id = 1,
                        brand = "Yamaha",
                        model = "MT-07",
                        year = 2024,
                        plateNumber = "BK-7788",
                        currentOdometer = 4250.0,
                        targetServiceOdometer = 5000.0,
                        insuranceExpiryDate = insuranceExpiry,
                        emissionExpiryDate = emissionExpiry,
                        insurancePolicyNum = "POL-9912",
                        insuranceProvider = "Standard Safe Care",
                        revenueLicenseExpiryDate = revenueLicenseExpiry,
                        revenueLicenseNo = "RL-88219A",
                        revenueLicenseProvince = "Western Province (WP)"
                    )
                )

                // Populate some sample logs to illustrate spending history & service records
                val now = System.currentTimeMillis()
                
                // Add two fuel records in the last 30 days
                repository.insertFuelLog(FuelLog(date = now - (2 * 24 * 3600 * 1000), odometer = 4210.0, cost = 5400.00, liters = 15.0, stationName = "EcoFuel Premium"))
                repository.insertFuelLog(FuelLog(date = now - (15 * 24 * 3600 * 1000), odometer = 3980.0, cost = 5112.00, liters = 14.2, stationName = "EcoFuel Premium"))
                repository.insertFuelLog(FuelLog(date = now - (35 * 24 * 3600 * 1000), odometer = 3650.0, cost = 5436.00, liters = 15.1, stationName = "EcoFuel Premium"))

                // Add past service log
                repository.insertServiceLog(
                    ServiceLog(
                        date = now - (60 * 24 * 3600 * 1000),
                        odometer = 1000.0,
                        cost = 14500.00,
                        serviceDetails = "First Breakthrough Oil Service, Chain adjustment, filter replacement.",
                        nextDueOdometer = 5000.0
                    )
                )

                // Add clean wash expense
                repository.insertExpenseLog(
                    ExpenseLog(
                        date = now - (10 * 24 * 3600 * 1000),
                        category = "Wash & Detail",
                        cost = 1500.00,
                        notes = "Deep clean mud wash"
                    )
                )

                // Add past emission record
                repository.insertEmissionLog(
                    EmissionLog(
                        date = now - (180 * 24 * 3600 * 1000),
                        expiryDate = emissionExpiry,
                        slipNumber = "EM-TEST-44211",
                        testCenter = "State Inspection #4",
                        isPassed = true,
                        cost = 1850.00,
                        notes = "Passed with 95% efficiency"
                    )
                )
            }
        }
    }

    // Update Bike info
    fun updateBikeDetails(
        brand: String,
        model: String,
        year: Int,
        plateNumber: String,
        currentOdometer: Double,
        targetServiceOdometer: Double,
        insuranceExpiryDate: Long,
        emissionExpiryDate: Long,
        insurancePolicyNum: String,
        insuranceProvider: String,
        revenueLicenseExpiryDate: Long,
        revenueLicenseNo: String,
        revenueLicenseProvince: String
    ) {
        viewModelScope.launch {
            repository.saveBikeDetails(
                BikeDetails(
                    id = 1,
                    brand = brand,
                    model = model,
                    year = year,
                    plateNumber = plateNumber,
                    currentOdometer = currentOdometer,
                    targetServiceOdometer = targetServiceOdometer,
                    insuranceExpiryDate = insuranceExpiryDate,
                    emissionExpiryDate = emissionExpiryDate,
                    insurancePolicyNum = insurancePolicyNum,
                    insuranceProvider = insuranceProvider,
                    revenueLicenseExpiryDate = revenueLicenseExpiryDate,
                    revenueLicenseNo = revenueLicenseNo,
                    revenueLicenseProvince = revenueLicenseProvince
                )
            )
        }
    }

    // Add logs
    fun addFuelLog(odometer: Double, cost: Double, liters: Double, station: String, date: Long) {
        viewModelScope.launch {
            repository.insertFuelLog(FuelLog(date = date, odometer = odometer, cost = cost, liters = liters, stationName = station))
            // Auto update bike odometer if the log odometer is larger!
            val currentBike = bikeDetails.value
            if (odometer > currentBike.currentOdometer) {
                repository.saveBikeDetails(currentBike.copy(currentOdometer = odometer))
            }
        }
    }

    fun addServiceLog(odometer: Double, cost: Double, details: String, nextDueOdometer: Double, date: Long) {
        viewModelScope.launch {
            repository.insertServiceLog(
                ServiceLog(
                    date = date,
                    odometer = odometer,
                    cost = cost,
                    serviceDetails = details,
                    nextDueOdometer = nextDueOdometer
                )
            )
            // Auto update current odometer and service due odometer
            val currentBike = bikeDetails.value
            val updatedCurrentOdo = if (odometer > currentBike.currentOdometer) odometer else currentBike.currentOdometer
            repository.saveBikeDetails(
                currentBike.copy(
                    currentOdometer = updatedCurrentOdo,
                    targetServiceOdometer = nextDueOdometer
                )
            )
        }
    }

    fun addExpenseLog(category: String, cost: Double, notes: String, date: Long) {
        viewModelScope.launch {
            repository.insertExpenseLog(
                ExpenseLog(
                    date = date,
                    category = category,
                    cost = cost,
                    notes = notes
                )
            )
        }
    }

    fun addEmissionLog(slipNumber: String, testCenter: String, flagPassed: Boolean, cost: Double, expiryDate: Long, date: Long) {
        viewModelScope.launch {
            repository.insertEmissionLog(
                EmissionLog(
                    date = date,
                    expiryDate = expiryDate,
                    slipNumber = slipNumber,
                    testCenter = testCenter,
                    isPassed = flagPassed,
                    cost = cost
                )
            )
            // Update emission expiry in bike details
            val currentBike = bikeDetails.value
            repository.saveBikeDetails(
                currentBike.copy(
                    emissionExpiryDate = expiryDate
                )
            )
        }
    }

    // Delete logs
    fun deleteFuel(log: FuelLog) {
        viewModelScope.launch { repository.deleteFuelLog(log) }
    }

    fun deleteService(log: ServiceLog) {
        viewModelScope.launch { repository.deleteServiceLog(log) }
    }

    fun deleteExpense(log: ExpenseLog) {
        viewModelScope.launch { repository.deleteExpenseLog(log) }
    }

    fun deleteEmission(log: EmissionLog) {
        viewModelScope.launch { repository.deleteEmissionLog(log) }
    }

    fun startLiveTracking() {
        if (isTrackingActive.value) return
        LocationService.reset()
        val intent = Intent(getApplication(), LocationService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplication<Application>().startForegroundService(intent)
            } else {
                getApplication<Application>().startService(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("BikeViewModel", "Error starting LocationService: ${e.message}")
        }
    }

    fun stopLiveTracking() {
        if (!isTrackingActive.value) return
        try {
            val intent = Intent(getApplication(), LocationService::class.java)
            getApplication<Application>().stopService(intent)
        } catch (e: Exception) {
            android.util.Log.e("BikeViewModel", "Error stopping LocationService: ${e.message}")
        }
    }

    fun simulateTravelDistance(km: Double) {
        LocationService.simulateTravelDistance(getApplication(), repository, km)
    }

    // -- Complete Alerts and Notification System states & methods --
    private val _isAlertsEnabledState = MutableStateFlow(AlertNotificationManager.isAlertsEnabled(getApplication()))
    val isAlertsEnabled: StateFlow<Boolean> = _isAlertsEnabledState.asStateFlow()

    private val _alertThresholdDaysState = MutableStateFlow(AlertNotificationManager.getThresholdDays(getApplication()))
    val alertThresholdDays: StateFlow<Int> = _alertThresholdDaysState.asStateFlow()

    fun setAlertsEnabled(enabled: Boolean) {
        AlertNotificationManager.setAlertsEnabled(getApplication(), enabled)
        _isAlertsEnabledState.value = enabled
    }

    fun setAlertThresholdDays(days: Int) {
        AlertNotificationManager.setThresholdDays(getApplication(), days)
        _alertThresholdDaysState.value = days
    }

    fun triggerManualCheckNow() {
        viewModelScope.launch {
            AlertNotificationManager.runImmediateAlertCheck(getApplication())
        }
    }

    fun triggerDemoNotification(type: String) {
        AlertNotificationManager.triggerTestNotification(getApplication(), type)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
