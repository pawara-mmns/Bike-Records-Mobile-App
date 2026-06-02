package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bike_details")
data class BikeDetails(
    @PrimaryKey val id: Int = 1,
    val brand: String = "",
    val model: String = "",
    val year: Int = 2024,
    val plateNumber: String = "",
    val currentOdometer: Double = 0.0,
    val targetServiceOdometer: Double = 5000.0, // Needs servicing at this odometer
    val insuranceExpiryDate: Long = 0L, // Epoch milliseconds
    val emissionExpiryDate: Long = 0L,  // Epoch milliseconds
    val insurancePolicyNum: String = "",
    val insuranceProvider: String = "",
    val revenueLicenseExpiryDate: Long = 0L, // Epoch milliseconds
    val revenueLicenseNo: String = "",
    val revenueLicenseProvince: String = ""
)

@Entity(tableName = "service_logs")
data class ServiceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,                  // Epoch milliseconds
    val odometer: Double,
    val cost: Double,
    val serviceDetails: String,      // Engine oil, tire rotation, etc.
    val nextDueOdometer: Double      // target mileage for next service
)

@Entity(tableName = "fuel_logs")
data class FuelLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,                  // Epoch milliseconds
    val odometer: Double,
    val cost: Double,                // Amount spent
    val liters: Double,              // Quantity of fuel filled
    val stationName: String = ""
)

@Entity(tableName = "expense_logs")
data class ExpenseLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,                  // Epoch milliseconds
    val category: String,            // Wash, Repair, Insurance, Accessory, Tax, Other
    val cost: Double,
    val notes: String
)

@Entity(tableName = "emission_logs")
data class EmissionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,                  // Test date
    val expiryDate: Long,            // Next test due date
    val slipNumber: String,          // Certificate reference
    val testCenter: String,
    val isPassed: Boolean,           // Pass/Fail
    val cost: Double,
    val notes: String = ""
)
