package com.example.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BikeDetails

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertRemindersHubDialog(
    viewModel: BikeViewModel,
    bike: BikeDetails,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isAlertsEnabled by viewModel.isAlertsEnabled.collectAsState()
    val alertThresholdDays by viewModel.alertThresholdDays.collectAsState()

    val daysUntilInsurance = DateUtils.getDaysRemaining(bike.insuranceExpiryDate)
    val daysUntilEmission = DateUtils.getDaysRemaining(bike.emissionExpiryDate)
    val daysUntilRevenueLicense = DateUtils.getDaysRemaining(bike.revenueLicenseExpiryDate)
    val kmUntilService = bike.targetServiceOdometer - bike.currentOdometer

    val scrollState = rememberScrollState()

    // Request NOTIFICATION permisson launcher (for Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission enabled! Alerts will show.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied. Alerts cannot be triggered.", Toast.LENGTH_LONG).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(16.dp)
                .testTag("alerts_hub_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alerts setup",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Alerts & Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("dismiss_alerts_hub_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Setup Dialog"
                        )
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Body content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // Permission Check Banner (Android 13+)
                    if (Build.VERSION.SDK_INT >= 33) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Permissions status check",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "System Permissions",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Ensure notification access is allowed.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Grant", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    // Main Toggles Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Daily Automatic Alerts",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Scan vehicle deadlines every morning and notify the device of upcoming expirations automatically.",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isAlertsEnabled,
                                    onCheckedChange = { viewModel.setAlertsEnabled(it) },
                                    modifier = Modifier.testTag("toggle_daily_alerts"),
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (isAlertsEnabled) Icons.Default.Check else Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                )
                            }

                            if (isAlertsEnabled) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Expiration Warning Threshold: $alertThresholdDays days",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Triggers a notification reminder when any document validity date falls below this threshold.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Slider(
                                    value = alertThresholdDays.toFloat(),
                                    onValueChange = { viewModel.setAlertThresholdDays(it.toInt()) },
                                    valueRange = 5f..45f,
                                    steps = 7,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("threshold_selector_slider")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("5 Days", style = MaterialTheme.typography.labelSmall)
                                    Text("15 Days", style = MaterialTheme.typography.labelSmall)
                                    Text("30 Days", style = MaterialTheme.typography.labelSmall)
                                    Text("45 Days", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    // Alerts Evaluator (Active Status) Section
                    Text(
                        text = "Current Alert Indicators",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    // Card 1: Insurance Status
                    AlertStatusCard(
                        title = "Insurance Expiry",
                        statusText = if (bike.insuranceExpiryDate <= 0L) "Not Configured"
                        else if (daysUntilInsurance < 0) "Policy Expired!"
                        else "Expires in $daysUntilInsurance days",
                        icon = Icons.Default.Shield,
                        stateColor = if (bike.insuranceExpiryDate <= 0L) Color.Gray
                        else if (daysUntilInsurance < 0) MaterialTheme.colorScheme.error
                        else if (daysUntilInsurance <= alertThresholdDays) Color(0xFFFFB300)
                        else Color(0xFF4CAF50),
                        subtitle = if (bike.insuranceExpiryDate > 0L) "Valid until ${DateUtils.formatDate(bike.insuranceExpiryDate)} (No: ${bike.insurancePolicyNum})" else "Setup insurance policy code under Edit Bike Profiles"
                    )

                    // Card 2: Emission Status
                    AlertStatusCard(
                        title = "Emission Test Expiry",
                        statusText = if (bike.emissionExpiryDate <= 0L) "Not Configured"
                        else if (daysUntilEmission < 0) "Expired/Critical!"
                        else "Expires in $daysUntilEmission days",
                        icon = Icons.Default.CloudQueue,
                        stateColor = if (bike.emissionExpiryDate <= 0L) Color.Gray
                        else if (daysUntilEmission < 0) MaterialTheme.colorScheme.error
                        else if (daysUntilEmission <= alertThresholdDays) Color(0xFFFFB300)
                        else Color(0xFF4CAF50),
                        subtitle = if (bike.emissionExpiryDate > 0L) "Valid until ${DateUtils.formatDate(bike.emissionExpiryDate)}" else "Setup carbon emission dates"
                    )

                    // Card 3: Revenue License Status
                    AlertStatusCard(
                        title = "Revenue License",
                        statusText = if (bike.revenueLicenseExpiryDate <= 0L) "Not Configured"
                        else if (daysUntilRevenueLicense < 0) "License Expired!"
                        else "Expires in $daysUntilRevenueLicense days",
                        icon = Icons.Default.CardMembership,
                        stateColor = if (bike.revenueLicenseExpiryDate <= 0L) Color.Gray
                        else if (daysUntilRevenueLicense < 0) MaterialTheme.colorScheme.error
                        else if (daysUntilRevenueLicense <= alertThresholdDays) Color(0xFFFFB300)
                        else Color(0xFF4CAF50),
                        subtitle = if (bike.revenueLicenseExpiryDate > 0L) "${bike.revenueLicenseProvince} (No: ${bike.revenueLicenseNo})" else "Configure provincial license details"
                    )

                    // Card 4: Service status
                    val isServiceOverdue = kmUntilService < 0
                    AlertStatusCard(
                        title = "Planned Tune-Up Service",
                        statusText = if (kmUntilService <= 0) "Service Overdue!"
                        else if (kmUntilService <= 500) "Due Very Soon!"
                        else "Service Healthy",
                        icon = Icons.Default.Construction,
                        stateColor = if (kmUntilService <= 0) MaterialTheme.colorScheme.error
                        else if (kmUntilService <= 500) Color(0xFFFFB300)
                        else Color(0xFF4CAF50),
                        subtitle = if (kmUntilService <= 0) "Overdue by ${Math.abs(kmUntilService.toInt())} km (Limit: ${bike.targetServiceOdometer.toInt()} km)"
                        else "${kmUntilService.toInt()} km left until target odo limit"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Immediate Actions Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Developer Testing Actions",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            // Process current state
                            Button(
                                onClick = {
                                    viewModel.triggerManualCheckNow()
                                    Toast.makeText(context, "Scanning current deadlines... Notifications will fire if due.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("run_manual_scan_alerts"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan & Notify Current Expirations", style = MaterialTheme.typography.bodyMedium)
                            }

                            Text(
                                text = "Instantly trigger a simulated push notification alert directly onto your device status bar down below:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SuggestionChip(
                                    onClick = { viewModel.triggerDemoNotification("insurance") },
                                    label = { Text("Demo: Insurance Expiry", style = MaterialTheme.typography.labelSmall) }
                                )
                                SuggestionChip(
                                    onClick = { viewModel.triggerDemoNotification("emission") },
                                    label = { Text("Demo: Emission Due", style = MaterialTheme.typography.labelSmall) }
                                )
                                SuggestionChip(
                                    onClick = { viewModel.triggerDemoNotification("revenue") },
                                    label = { Text("Demo: Revenue License", style = MaterialTheme.typography.labelSmall) }
                                )
                                SuggestionChip(
                                    onClick = { viewModel.triggerDemoNotification("service") },
                                    label = { Text("Demo: Service Limit", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Footer Actions
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close Hub", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AlertStatusCard(
    title: String,
    statusText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    stateColor: Color,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = stateColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = stateColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = stateColor,
                        textAlign = TextAlign.End
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
