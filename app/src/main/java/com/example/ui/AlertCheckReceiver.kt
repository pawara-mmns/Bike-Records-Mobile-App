package com.example.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.BikeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlertCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        
        scope.launch {
            try {
                val db = BikeDatabase.getDatabase(context.applicationContext)
                val bike = db.bikeDao().getBikeDetails().first()
                if (bike != null) {
                    AlertNotificationManager.evaluateBikeDetails(context.applicationContext, bike)
                }
            } catch (e: Exception) {
                android.util.Log.e("AlertCheckReceiver", "Error running schedule bike check: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
