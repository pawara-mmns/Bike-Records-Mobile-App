package com.example.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.data.BikeDatabase
import com.example.data.BikeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class LocationService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var locationManager: LocationManager
    private lateinit var repository: BikeRepository

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val prev = _lastLocation.value
            if (prev != null) {
                val distanceInMeters = prev.distanceTo(location)
                if (distanceInMeters > 1.0) { // skip noise
                    val distanceInKm = distanceInMeters / 1000.0
                    addTrackedDistance(distanceInKm)
                }
            }
            _lastLocation.value = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val database = BikeDatabase.getDatabase(applicationContext)
        repository = BikeRepository(database.bikeDao())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForegroundServiceWithNotification()

        val action = intent?.action
        if (action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startTracking()
        return START_STICKY
    }

    private fun startTracking() {
        _isServiceRunning.value = true
        try {
            val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (hasGps) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    2f,
                    locationListener
                )
                val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastGps != null && _lastLocation.value == null) {
                    _lastLocation.value = lastGps
                }
            } else if (hasNetwork) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    3000L,
                    3f,
                    locationListener
                )
                val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastNet != null && _lastLocation.value == null) {
                    _lastLocation.value = lastNet
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("LocationService", "SecurityException requesting location updates: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("LocationService", "Error starting location tracking: ${e.message}")
        }
    }

    private fun addTrackedDistance(km: Double) {
        _sessionDistance.value += km
        updateNotification()
        
        serviceScope.launch {
            try {
                val bike = repository.bikeDetails.first()
                if (bike != null) {
                    val newOdo = bike.currentOdometer + km
                    val roundedOdo = Math.round(newOdo * 100.0) / 100.0
                    repository.saveBikeDetails(bike.copy(currentOdometer = roundedOdo))
                }
            } catch (e: Exception) {
                android.util.Log.e("LocationService", "Error saving odometer update: ${e.message}")
            }
        }
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, LocationService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, Class.forName("com.example.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distanceStr = String.format(Locale.getDefault(), "%,.3f km", _sessionDistance.value)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Live Trip Tracking Active")
            .setContentText("Distance traveled: $distanceStr")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Tracking",
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Live Telemetry Tracker Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live trip tracking distance statistics."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try {
            locationManager.removeUpdates(locationListener)
        } catch (e: Exception) {
            android.util.Log.e("LocationService", "Error stopping location updates: ${e.message}")
        }
        serviceJob.cancel()
        _isServiceRunning.value = false
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 21005
        const val ACTION_STOP = "com.example.ACTION_STOP_TRACKING"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _sessionDistance = MutableStateFlow(0.0)
        val sessionDistance: StateFlow<Double> = _sessionDistance.asStateFlow()

        private val _lastLocation = MutableStateFlow<Location?>(null)
        val lastLocation: StateFlow<Location?> = _lastLocation.asStateFlow()

        fun simulateTravelDistance(context: Context, repository: BikeRepository, km: Double) {
            _sessionDistance.value += km
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (_isServiceRunning.value && manager != null) {
                val stopIntent = Intent(context, LocationService::class.java).apply {
                    action = ACTION_STOP
                }
                val stopPendingIntent = PendingIntent.getService(
                    context,
                    1,
                    stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val openAppIntent = Intent(context, Class.forName("com.example.MainActivity")).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val distanceStr = String.format(Locale.getDefault(), "%,.3f km", _sessionDistance.value)
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Live Trip Tracking Active")
                    .setContentText("Distance traveled: $distanceStr")
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(openAppPendingIntent)
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Stop Tracking",
                        stopPendingIntent
                    )
                    .build()
                manager.notify(NOTIFICATION_ID, notification)
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bike = repository.bikeDetails.first()
                    if (bike != null) {
                        val newOdo = bike.currentOdometer + km
                        val roundedOdo = Math.round(newOdo * 100.0) / 100.0
                        repository.saveBikeDetails(bike.copy(currentOdometer = roundedOdo))
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LocationService", "Error simulating odometer update: ${e.message}")
                }
            }
        }

        fun reset() {
            _sessionDistance.value = 0.0
            _lastLocation.value = null
        }
    }
}
