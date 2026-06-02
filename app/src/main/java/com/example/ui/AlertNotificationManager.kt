package com.example.ui

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.data.BikeDatabase
import com.example.data.BikeDetails
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Locale

object AlertNotificationManager {

    const val CHANNEL_ID = "vehicle_alerts_channel"
    private const val PREFS_NAME = "alert_prefs"
    private const val KEY_ALERTS_ENABLED = "alerts_enabled"
    private const val KEY_THRESHOLD_DAYS = "threshold_days"

    // High-quality distinct notification IDs
    const val ID_INSURANCE = 10001
    const val ID_EMISSION = 10002
    const val ID_REVENUE_LICENSE = 10003
    const val ID_SERVICE = 10004

    fun createAlertChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vehicle Alerts & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you when your revenue license, insurance, emission cert, or service is due."
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun isAlertsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ALERTS_ENABLED, true)
    }

    fun setAlertsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ALERTS_ENABLED, enabled).apply()
        if (enabled) {
            scheduleDailyAlarm(context)
        } else {
            cancelDailyAlarm(context)
        }
    }

    fun getThresholdDays(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THRESHOLD_DAYS, 14) // default 14 days warning
    }

    fun setThresholdDays(context: Context, days: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THRESHOLD_DAYS, days).apply()
    }

    fun scheduleDailyAlarm(context: Context) {
        if (!isAlertsEnabled(context)) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertCheckReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set to fire daily starting at 9:00 AM
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            // If 9 AM already passed, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            android.util.Log.e("AlertNotification", "Error scheduling alarm: ${e.message}")
        }
    }

    fun cancelDailyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertCheckReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    suspend fun runImmediateAlertCheck(context: Context) {
        val db = BikeDatabase.getDatabase(context)
        val bike = db.bikeDao().getBikeDetails().first() ?: return
        evaluateBikeDetails(context, bike)
    }

    fun evaluateBikeDetails(context: Context, bike: BikeDetails) {
        createAlertChannel(context)
        val thresholdDays = getThresholdDays(context)

        // 1. Insurance Expiry
        if (bike.insuranceExpiryDate > 0L) {
            val days = DateUtils.getDaysRemaining(bike.insuranceExpiryDate)
            if (days <= thresholdDays) {
                if (days < 0) {
                    sendNotification(
                        context,
                        ID_INSURANCE,
                        "Insurance Expired! 🚨",
                        "Your policy (${bike.insurancePolicyNum}) expired ${Math.abs(days)} days ago! Please renew immediately."
                    )
                } else {
                    sendNotification(
                        context,
                        ID_INSURANCE,
                        "Insurance Renewal Due ⏳",
                        "Your policy (${bike.insurancePolicyNum}) expires in $days days (${DateUtils.formatDate(bike.insuranceExpiryDate)})."
                    )
                }
            }
        }

        // 2. Emission Expiry
        if (bike.emissionExpiryDate > 0L) {
            val days = DateUtils.getDaysRemaining(bike.emissionExpiryDate)
            if (days <= thresholdDays) {
                if (days < 0) {
                    sendNotification(
                        context,
                        ID_EMISSION,
                        "Emission Certificate Expired! 🚨",
                        "Your emission certificate expired ${Math.abs(days)} days ago! Please schedule a test."
                    )
                } else {
                    sendNotification(
                        context,
                        ID_EMISSION,
                        "Emission Certificate Renewal Due ⏳",
                        "Your certificate expires in $days days (${DateUtils.formatDate(bike.emissionExpiryDate)})."
                    )
                }
            }
        }

        // 3. Revenue License Expiry
        if (bike.revenueLicenseExpiryDate > 0L) {
            val days = DateUtils.getDaysRemaining(bike.revenueLicenseExpiryDate)
            if (days <= thresholdDays) {
                if (days < 0) {
                    sendNotification(
                        context,
                        ID_REVENUE_LICENSE,
                        "Revenue License Expired! 🚨",
                        "Your Western Province license is expired by ${Math.abs(days)} days! Renew to avoid traffic penalties."
                    )
                } else {
                    sendNotification(
                        context,
                        ID_REVENUE_LICENSE,
                        "Revenue License Renewal Due ⏳",
                        "Your license (${bike.revenueLicenseNo}) expires in $days days (${DateUtils.formatDate(bike.revenueLicenseExpiryDate)})."
                    )
                }
            }
        }

        // 4. Scheduled Service Reminder
        val kmRemaining = bike.targetServiceOdometer - bike.currentOdometer
        if (kmRemaining <= 500) {
            if (kmRemaining < 0) {
                sendNotification(
                    context,
                    ID_SERVICE,
                    "Service Overdue! 🛠️",
                    "Your vehicle has exceeded the scheduled service odometer by ${String.format(Locale.getDefault(), "%,.0fKey", Math.abs(kmRemaining))} km!"
                )
            } else {
                sendNotification(
                    context,
                    ID_SERVICE,
                    "Service Approaching! 🛠️",
                    "Service is recommended in ${String.format(Locale.getDefault(), "%,.0f", kmRemaining)} km of travel."
                )
            }
        }
    }

    private fun sendNotification(context: Context, id: Int, title: String, message: String) {
        val openAppIntent = Intent(context, Class.forName("com.example.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            id,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(openAppPendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }

    // Trigger instant mock notifications of different types for showcase/sandbox testing
    fun triggerTestNotification(context: Context, type: String) {
        createAlertChannel(context)
        val title: String
        val msg: String
        val id: Int

        when (type) {
            "insurance" -> {
                title = "Demo: Insurance Expiring 🛡️"
                msg = "[TEST ALERT] Your Alliance Star insurance coverage is expiring in 5 days (Policy #POL-9912)."
                id = ID_INSURANCE
            }
            "emission" -> {
                title = "Demo: Emission Test Due 💨"
                msg = "[TEST ALERT] EcoCheck Test Center status update: Emission testing certificate is due for renewal."
                id = ID_EMISSION
            }
            "revenue" -> {
                title = "Demo: Revenue License Renewal 🎫"
                msg = "[TEST ALERT] Department of Motor Traffic reminder: Western Province license renewal window is open (No. RL-88219A)."
                id = ID_REVENUE_LICENSE
            }
            else -> {
                title = "Demo: General Tuning Reminder ⚙️"
                msg = "[TEST ALERT] Your vehicle is approaching its 12,000 km general engine checkup interval."
                id = ID_SERVICE
            }
        }

        sendNotification(context, id, title, msg)
    }
}
