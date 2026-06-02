package com.example.ui

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BikeDetails
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: BikeViewModel,
    modifier: Modifier = Modifier
) {
    val bike by viewModel.bikeDetails.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    val daysUntilInsurance = DateUtils.getDaysRemaining(bike.insuranceExpiryDate)
    val daysUntilEmission = DateUtils.getDaysRemaining(bike.emissionExpiryDate)
    val daysUntilRevenueLicense = DateUtils.getDaysRemaining(bike.revenueLicenseExpiryDate)
    val kmUntilService = bike.targetServiceOdometer - bike.currentOdometer

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vehicle Header Card (Cockpit style)
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBike,
                                    contentDescription = "Bike Icon",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (bike.brand.isEmpty() && bike.model == "My Bike") "Your Bike Profile" else "${bike.brand} ${bike.model}".trim(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (bike.plateNumber.isNotEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = bike.plateNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = { showEditDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Vehicle details",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Odometer Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CURRENT MILEAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.getDefault(), "%,.0f", bike.currentOdometer),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " km",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MODEL YEAR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (bike.year > 0) bike.year.toString() else "N/A",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Live Telemetry & GPS Odometer Tracker Card
        val isTrackingActive by viewModel.isTrackingActive.collectAsState()
        val sessionDistance by viewModel.sessionTrackedDistance.collectAsState()
        val lastLocation by viewModel.lastLocation.collectAsState()
        val trackingContext = LocalContext.current

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineGranted || coarseGranted) {
                viewModel.startLiveTracking()
            }
        }

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (isTrackingActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.03f) else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, if (isTrackingActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (isTrackingActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isTrackingActive) Icons.Default.Navigation else Icons.Default.LocationOn,
                                contentDescription = "GPS Icon",
                                tint = if (isTrackingActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "LIVE TELEMETRY TRACKER",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isTrackingActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isTrackingActive) "Real-time location mileage sync active" else "GPS standby • toggle to start tracking",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Switch(
                        checked = isTrackingActive,
                        onCheckedChange = { active ->
                            if (active) {
                                val fineGranted = ContextCompat.checkSelfPermission(trackingContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val coarseGranted = ContextCompat.checkSelfPermission(trackingContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (fineGranted || coarseGranted) {
                                    viewModel.startLiveTracking()
                                } else {
                                    val req = mutableListOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    ).apply {
                                        if (android.os.Build.VERSION.SDK_INT >= 33) {
                                            add("android.permission.POST_NOTIFICATIONS")
                                        }
                                    }.toTypedArray()
                                    permissionLauncher.launch(req)
                                }
                            } else {
                                viewModel.stopLiveTracking()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TRACKED SESSION DISTANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%,.3f km", sessionDistance),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isTrackingActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = if (isTrackingActive) Color(0xFF4CAF50).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isTrackingActive) Color(0xFF4CAF50) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTrackingActive) "ACTIVE" else "STANDBY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isTrackingActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "My location icon",
                            tint = if (isTrackingActive && lastLocation != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTrackingActive && lastLocation != null) {
                                "Lat: ${String.format(Locale.getDefault(), "%.6f", lastLocation!!.latitude)}, Long: ${String.format(Locale.getDefault(), "%.6f", lastLocation!!.longitude)}"
                            } else if (isTrackingActive) {
                                "Acquiring GPS Satellite Signal..."
                            } else {
                                "Standby (Awaiting tracking activation)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isTrackingActive && lastLocation != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SANDBOX ROAD TRIP SIMULATOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1.0, 5.0, 10.0).forEach { km ->
                        Button(
                            onClick = {
                                viewModel.simulateTravelDistance(km)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+$km km",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Active Alerts Section
        val alerts = remember(bike, daysUntilInsurance, daysUntilEmission, daysUntilRevenueLicense, kmUntilService) {
            mutableListOf<Pair<String, Color>>().apply {
                if (bike.insuranceExpiryDate > 0L && daysUntilInsurance <= 30) {
                    if (daysUntilInsurance < 0) {
                        add("Insurance expired ${Math.abs(daysUntilInsurance)} days ago!" to Color(0xFFFF5252))
                    } else {
                        add("Insurance expires in $daysUntilInsurance days." to Color(0xFFFFB300))
                    }
                }
                if (bike.emissionExpiryDate > 0L && daysUntilEmission <= 15) {
                    if (daysUntilEmission < 0) {
                        add("Emission clearance expired ${Math.abs(daysUntilEmission)} days ago!" to Color(0xFFFF5252))
                    } else {
                        add("Emission test expired/due in $daysUntilEmission days." to Color(0xFFFFB300))
                    }
                }
                if (bike.revenueLicenseExpiryDate > 0L && daysUntilRevenueLicense <= 30) {
                    if (daysUntilRevenueLicense < 0) {
                        add("Revenue License expired ${Math.abs(daysUntilRevenueLicense)} days ago!" to Color(0xFFFF5252))
                    } else {
                        add("Revenue License expires in $daysUntilRevenueLicense days." to Color(0xFFFFB300))
                    }
                }
                if (kmUntilService <= 500) {
                    if (kmUntilService < 0) {
                        add("Scheduled service overdue by ${Math.abs(kmUntilService.toInt())} km!" to Color(0xFFFF5252))
                    } else {
                        add("Scheduled service due in ${kmUntilService.toInt()} km." to Color(0xFFFFB300))
                    }
                }
            }
        }

        if (alerts.isNotEmpty()) {
            Text(
                text = "CRITICAL ALERT REMINDERS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp).padding(horizontal = 4.dp)
            )

            alerts.forEach { (msg, color) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (color == Color(0xFFFF5252)) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = "Alert icon",
                            tint = color
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Text(
            text = "VEHICLE HEALTH & DATES STATUS",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp).padding(horizontal = 4.dp)
        )

        // 1. Scheduled service mileage countdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Service logs Icon",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Maintenance Service Due",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Next service target: ${String.format(Locale.getDefault(), "%,.0f", bike.targetServiceOdometer)} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Indicator Circle (Urgent tag styled directly matching Tailwind HTML specification)
                    val indicatorBk = MaterialTheme.colorScheme.onPrimaryContainer
                    val indicatorText = MaterialTheme.colorScheme.primaryContainer
                    val indicatorLabel = when {
                        kmUntilService <= 0 -> "Overdue"
                        kmUntilService <= 500 -> "Urgent"
                        else -> "Healthy"
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = indicatorBk,
                    ) {
                        Text(
                            text = indicatorLabel,
                            color = indicatorText,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val progress = remember(bike) {
                    if (bike.targetServiceOdometer > 0) {
                        // Ratio of current mileage vs next target service mileage
                        val relativeRatio = bike.currentOdometer / bike.targetServiceOdometer
                        relativeRatio.coerceIn(0.0, 1.0).toFloat()
                    } else {
                        0f
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Current: ${String.format(Locale.getDefault(), "%,.0f", bike.currentOdometer)} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (kmUntilService >= 0) {
                            "${String.format(Locale.getDefault(), "%,.0f", kmUntilService)} km remaining"
                        } else {
                            "Overdue by ${String.format(Locale.getDefault(), "%,.0f", Math.abs(kmUntilService))} km!"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (kmUntilService <= 0) Color(0xFFFF5252) else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // 2. Insurance validity
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Insurance",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Insurance Coverage",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (bike.insuranceExpiryDate > 0L) {
                        Text(
                            text = "Expires: ${DateUtils.formatDate(bike.insuranceExpiryDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (bike.insuranceProvider.isNotEmpty() || bike.insurancePolicyNum.isNotEmpty()) {
                            Text(
                                text = "${bike.insuranceProvider} (${bike.insurancePolicyNum})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Text(
                            text = "No active policy details set",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                // Days Counter badge
                if (bike.insuranceExpiryDate > 0L) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (daysUntilInsurance >= 0) daysUntilInsurance.toString() else "EXPIRED",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (daysUntilInsurance <= 15) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (daysUntilInsurance >= 0) "days left" else "renew now",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Vehicle emission test validity
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Co2,
                            contentDescription = "Emission",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Emission Certificate",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (bike.emissionExpiryDate > 0) {
                        Text(
                            text = "Expires: ${DateUtils.formatDate(bike.emissionExpiryDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "No emission details logged yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                // Days Indicator badge
                if (bike.emissionExpiryDate > 0L) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (daysUntilEmission >= 0) daysUntilEmission.toString() else "EXPIRED",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (daysUntilEmission <= 10) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (daysUntilEmission >= 0) "days left" else "inspect due",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4. Vehicle revenue license validity
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CardMembership,
                            contentDescription = "Revenue License",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Revenue License",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (bike.revenueLicenseExpiryDate > 0L) {
                        Text(
                            text = "Expires: ${DateUtils.formatDate(bike.revenueLicenseExpiryDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (bike.revenueLicenseProvince.isNotEmpty() || bike.revenueLicenseNo.isNotEmpty()) {
                            Text(
                                text = "${bike.revenueLicenseProvince} (No: ${bike.revenueLicenseNo})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Text(
                            text = "No active license details logged",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                // Days Counter badge
                if (bike.revenueLicenseExpiryDate > 0L) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (daysUntilRevenueLicense >= 0) daysUntilRevenueLicense.toString() else "EXPIRED",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (daysUntilRevenueLicense <= 15) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (daysUntilRevenueLicense >= 0) "days left" else "renew now",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp)) // padding at bottom
    }

    if (showEditDialog) {
        EditBikeDetailsDialog(
            bike = bike,
            onDismiss = { showEditDialog = false },
            onSave = { brand, model, year, plate, currentOdo, targetOdo, insExpDate, emExpDate, insPolicy, insProv, revExpDate, revNo, revProvince ->
                viewModel.updateBikeDetails(
                    brand = brand,
                    model = model,
                    year = year,
                    plateNumber = plate,
                    currentOdometer = currentOdo,
                    targetServiceOdometer = targetOdo,
                    insuranceExpiryDate = insExpDate,
                    emissionExpiryDate = emExpDate,
                    insurancePolicyNum = insPolicy,
                    insuranceProvider = insProv,
                    revenueLicenseExpiryDate = revExpDate,
                    revenueLicenseNo = revNo,
                    revenueLicenseProvince = revProvince
                )
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBikeDetailsDialog(
    bike: BikeDetails,
    onDismiss: () -> Unit,
    onSave: (
        brand: String, model: String, year: Int, plateNumber: String,
        currentOdometer: Double, targetServiceOdometer: Double,
        insuranceExpiryDate: Long, emissionExpiryDate: Long,
        insurancePolicyNum: String, insuranceProvider: String,
        revenueLicenseExpiryDate: Long, revenueLicenseNo: String,
        revenueLicenseProvince: String
    ) -> Unit
) {
    val context = LocalContext.current
    var brand by remember { mutableStateOf(bike.brand) }
    var model by remember { mutableStateOf(bike.model) }
    var yearStr by remember { mutableStateOf(if (bike.year > 0) bike.year.toString() else "") }
    var plate by remember { mutableStateOf(bike.plateNumber) }
    var currentOdoStr by remember { mutableStateOf(bike.currentOdometer.toString()) }
    var targetOdoStr by remember { mutableStateOf(bike.targetServiceOdometer.toString()) }
    var insExpDate by remember { mutableStateOf(bike.insuranceExpiryDate) }
    var emExpDate by remember { mutableStateOf(bike.emissionExpiryDate) }
    var insPolicy by remember { mutableStateOf(bike.insurancePolicyNum) }
    var insProv by remember { mutableStateOf(bike.insuranceProvider) }
    var revExpDate by remember { mutableStateOf(bike.revenueLicenseExpiryDate) }
    var revNo by remember { mutableStateOf(bike.revenueLicenseNo) }
    var revProvince by remember { mutableStateOf(bike.revenueLicenseProvince) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Edit Bike Profile Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Brand / Model
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it },
                            label = { Text("Model Spec") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = yearStr,
                            onValueChange = { yearStr = it },
                            label = { Text("Model Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = plate,
                            onValueChange = { plate = it },
                            label = { Text("Plate Number") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Odometers
                    OutlinedTextField(
                        value = currentOdoStr,
                        onValueChange = { currentOdoStr = it },
                        label = { Text("Current Odometer Mileage (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetOdoStr,
                        onValueChange = { targetOdoStr = it },
                        label = { Text("Next Service Target Odometer (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Insurance Providers Group
                    Text(
                        text = "Insurance Information",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = insProv,
                            onValueChange = { insProv = it },
                            label = { Text("Policy Provider") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = insPolicy,
                            onValueChange = { insPolicy = it },
                            label = { Text("Policy Number") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Expiry dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val calendar = Calendar.getInstance().apply {
                                        if (insExpDate > 0) timeInMillis = insExpDate
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val c = Calendar.getInstance()
                                            c.set(y, m, d)
                                            insExpDate = c.timeInMillis
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Insurance Expiry",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (insExpDate > 0) DateUtils.formatDate(insExpDate) else "Tap to Set",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val calendar = Calendar.getInstance().apply {
                                        if (emExpDate > 0) timeInMillis = emExpDate
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val c = Calendar.getInstance()
                                            c.set(y, m, d)
                                            emExpDate = c.timeInMillis
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Emission Clearance Expiry",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (emExpDate > 0) DateUtils.formatDate(emExpDate) else "Tap to Set",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Revenue License Information Section
                    Text(
                        text = "Revenue License Information",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = revNo,
                            onValueChange = { revNo = it },
                            label = { Text("License / Receipt No") },
                            modifier = Modifier.weight(1.5f)
                        )
                        OutlinedTextField(
                            value = revProvince,
                            onValueChange = { revProvince = it },
                            label = { Text("Province") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val calendar = Calendar.getInstance().apply {
                                    if (revExpDate > 0) timeInMillis = revExpDate
                                }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val c = Calendar.getInstance()
                                        c.set(y, m, d)
                                        revExpDate = c.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Revenue License Expiry Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (revExpDate > 0) DateUtils.formatDate(revExpDate) else "Tap to Set",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val year = yearStr.toIntOrNull() ?: bike.year
                            val currentOdo = currentOdoStr.toDoubleOrNull() ?: bike.currentOdometer
                            val targetOdo = targetOdoStr.toDoubleOrNull() ?: bike.targetServiceOdometer
                            onSave(brand, model, year, plate, currentOdo, targetOdo, insExpDate, emExpDate, insPolicy, insProv, revExpDate, revNo, revProvince)
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save details")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}
