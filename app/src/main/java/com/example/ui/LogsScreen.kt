package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import java.util.*

enum class LogFilter {
    SERVICE, FUEL, EXPENSE, EMISSION
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogsScreen(
    viewModel: BikeViewModel,
    modifier: Modifier = Modifier
) {
    val serviceLogs by viewModel.allServiceLogs.collectAsState()
    val fuelLogs by viewModel.allFuelLogs.collectAsState()
    val expenseLogs by viewModel.allExpenseLogs.collectAsState()
    val emissionLogs by viewModel.allEmissionLogs.collectAsState()

    var activeFilter by remember { mutableStateOf(LogFilter.SERVICE) }
    var showTypePicker by remember { mutableStateOf(false) }

    // Dialog trigger variables
    var showAddFuelDialog by remember { mutableStateOf(false) }
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddEmissionDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header & Filter row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HISTORICAL RECORD LOGS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LogFilter.values().forEach { filter ->
                            val isSelected = activeFilter == filter
                            val label = when (filter) {
                                LogFilter.SERVICE -> "Services"
                                LogFilter.FUEL -> "Fuel Fills"
                                LogFilter.EXPENSE -> "Expenses"
                                LogFilter.EMISSION -> "Emission"
                            }
                            val icon = when (filter) {
                                LogFilter.SERVICE -> Icons.Default.Build
                                LogFilter.FUEL -> Icons.Default.LocalGasStation
                                LogFilter.EXPENSE -> Icons.Default.ShoppingBag
                                LogFilter.EMISSION -> Icons.Default.Co2
                            }

                            FilterChip(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("filter_chip_${filter.name.lowercase()}"),
                                onClick = { activeFilter = filter },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                selected = isSelected,
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Timeline List of logs
            when (activeFilter) {
                LogFilter.SERVICE -> {
                    if (serviceLogs.isEmpty()) {
                        EmptyLogsPlaceholder("No past maintenance services recorded yet. Update your bike records!")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(serviceLogs, key = { it.id }) { log ->
                                ServiceLogCard(log = log, onDelete = { viewModel.deleteService(log) })
                            }
                        }
                    }
                }
                LogFilter.FUEL -> {
                    if (fuelLogs.isEmpty()) {
                        EmptyLogsPlaceholder("No recorded fuel refills yet. Log your mileage gas fills to track your fuel costs per month.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(fuelLogs, key = { it.id }) { log ->
                                FuelLogCard(log = log, onDelete = { viewModel.deleteFuel(log) })
                            }
                        }
                    }
                }
                LogFilter.EXPENSE -> {
                    if (expenseLogs.isEmpty()) {
                        EmptyLogsPlaceholder("No monthly expenses logged yet. Save washes, spares, and license details.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(expenseLogs, key = { it.id }) { log ->
                                ExpenseLogCard(log = log, onDelete = { viewModel.deleteExpense(log) })
                            }
                        }
                    }
                }
                LogFilter.EMISSION -> {
                    if (emissionLogs.isEmpty()) {
                        EmptyLogsPlaceholder("No vehicle emission clearance records set yet. Keep emission compliance certificates updated.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(emissionLogs, key = { it.id }) { log ->
                                EmissionLogCard(log = log, onDelete = { viewModel.deleteEmission(log) })
                            }
                        }
                    }
                }
            }
        }

        // FAB to Add new logs
        FloatingActionButton(
            onClick = { showTypePicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 24.dp)
                .testTag("add_log_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add new Record")
        }

        // Choice sheet/picker dialog
        if (showTypePicker) {
            AddTypePickerDialog(
                onDismiss = { showTypePicker = false },
                onSelectType = { type ->
                    showTypePicker = false
                    when (type) {
                        LogFilter.FUEL -> { showAddFuelDialog = true }
                        LogFilter.SERVICE -> { showAddServiceDialog = true }
                        LogFilter.EXPENSE -> { showAddExpenseDialog = true }
                        LogFilter.EMISSION -> { showAddEmissionDialog = true }
                    }
                }
            )
        }

        // Log input dialogs
        if (showAddFuelDialog) {
            AddFuelDialog(
                onDismiss = { showAddFuelDialog = false },
                onSave = { odo, cost, liters, station, t ->
                    viewModel.addFuelLog(odo, cost, liters, station, t)
                    showAddFuelDialog = false
                    activeFilter = LogFilter.FUEL
                }
            )
        }

        if (showAddServiceDialog) {
            AddServiceDialog(
                onDismiss = { showAddServiceDialog = false },
                onSave = { odo, cost, notes, nextDue, t ->
                    viewModel.addServiceLog(odo, cost, notes, nextDue, t)
                    showAddServiceDialog = false
                    activeFilter = LogFilter.SERVICE
                }
            )
        }

        if (showAddExpenseDialog) {
            AddExpenseDialog(
                onDismiss = { showAddExpenseDialog = false },
                onSave = { category, cost, notes, t ->
                    viewModel.addExpenseLog(category, cost, notes, t)
                    showAddExpenseDialog = false
                    activeFilter = LogFilter.EXPENSE
                }
            )
        }

        if (showAddEmissionDialog) {
            AddEmissionDialog(
                onDismiss = { showAddEmissionDialog = false },
                onSave = { slip, center, pass, cost, expireT, t ->
                    viewModel.addEmissionLog(slip, center, pass, cost, expireT, t)
                    showAddEmissionDialog = false
                    activeFilter = LogFilter.EMISSION
                }
            )
        }
    }
}

@Composable
fun EmptyLogsPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "Empty icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

// Cards UI elements
@Composable
fun ServiceLogCard(log: ServiceLog, onDelete: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("service_log_card"),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateUtils.formatDate(log.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Maintenance Service",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.getDefault(), "Rs. %,.2f", log.cost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete record",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = log.serviceDetails,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Odometer: ${String.format(Locale.getDefault(), "%,.0f", log.odometer)} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Next Service target: ${String.format(Locale.getDefault(), "%,.0f", log.nextDueOdometer)} km",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FuelLogCard(log: FuelLog, onDelete: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fuel_log_card"),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateUtils.formatDate(log.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Fuel Refill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.getDefault(), "Rs. %,.2f", log.cost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete record",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Volume",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f Liters", log.liters),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column {
                    Text(
                        text = "Unit Cost",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "Rs. %.2f/L", if (log.liters > 0) log.cost / log.liters else 0.0),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Odometer Mileage",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f km", log.odometer),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (log.stationName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Station",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = log.stationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseLogCard(log: ExpenseLog, onDelete: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_log_card"),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateUtils.formatDate(log.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = log.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.getDefault(), "Rs. %,.2f", log.cost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete record",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (log.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = log.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun EmissionLogCard(log: EmissionLog, onDelete: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("emission_log_card"),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateUtils.formatDate(log.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Emission Test Certificate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.getDefault(), "Rs. %,.2f", log.cost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete record",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = (if (log.isPassed) Color(0xFF4CAF50) else Color(0xFFFF5252)).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (log.isPassed) "TEST PASSED" else "TEST FAILED",
                        color = if (log.isPassed) Color(0xFF4CAF50) else Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (log.slipNumber.isNotEmpty()) {
                    Text(
                        text = "Slip #: ${log.slipNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Center: " + log.testCenter.ifEmpty { "Official Inspector" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Next due: ${DateUtils.formatDate(log.expiryDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Subordinate pop dialogues
@Composable
fun AddTypePickerDialog(
    onDismiss: () -> Unit,
    onSelectType: (LogFilter) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Log Record Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                TypeSelectorButton(
                    label = "Gas Fuel Fill",
                    description = "Refuel costs and quantity metrics",
                    icon = Icons.Default.LocalGasStation,
                    onClick = { onSelectType(LogFilter.FUEL) }
                )

                TypeSelectorButton(
                    label = "Maintenance Service",
                    description = "Repairs, engine oil, spares mileage limit",
                    icon = Icons.Default.Build,
                    onClick = { onSelectType(LogFilter.SERVICE) }
                )

                TypeSelectorButton(
                    label = "General Expense",
                    description = "Washes, license costs, updates, insurance",
                    icon = Icons.Default.ShoppingBag,
                    onClick = { onSelectType(LogFilter.EXPENSE) }
                )

                TypeSelectorButton(
                    label = "Emission Test Clearance",
                    description = "Emission results and document timelines",
                    icon = Icons.Default.Co2,
                    onClick = { onSelectType(LogFilter.EMISSION) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun TypeSelectorButton(
    label: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}

// dialog forms
@Composable
fun AddFuelDialog(
    onDismiss: () -> Unit,
    onSave: (odometer: Double, cost: Double, liters: Double, station: String, date: Long) -> Unit
) {
    val context = LocalContext.current
    var odometerStr by remember { mutableStateOf("") }
    var costStr by remember { mutableStateOf("") }
    var litersStr by remember { mutableStateOf("") }
    var station by remember { mutableStateOf("") }
    var dateInMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Add Fuel Refill Log",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = odometerStr,
                    onValueChange = { odometerStr = it },
                    label = { Text("Odometer Mileage (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text("Total Cost (LKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = litersStr,
                        onValueChange = { litersStr = it },
                        label = { Text("Gas Quantity (L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = station,
                    onValueChange = { station = it },
                    label = { Text("Gas Station (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Picker Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val c = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val sel = Calendar.getInstance()
                                    sel.set(y, m, d)
                                    dateInMillis = sel.timeInMillis
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fill Date", style = MaterialTheme.typography.bodyMedium)
                        Text(DateUtils.formatDate(dateInMillis), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val odo = odometerStr.toDoubleOrNull() ?: 0.0
                            val cost = costStr.toDoubleOrNull() ?: 0.0
                            val lit = litersStr.toDoubleOrNull() ?: 0.0
                            onSave(odo, cost, lit, station, dateInMillis)
                        }
                    ) {
                        Text("Add Fuel")
                    }
                }
            }
        }
    }
}

@Composable
fun AddServiceDialog(
    onDismiss: () -> Unit,
    onSave: (odometer: Double, cost: Double, details: String, nextDue: Double, date: Long) -> Unit
) {
    val context = LocalContext.current
    var odometerStr by remember { mutableStateOf("") }
    var costStr by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var targetOdoStr by remember { mutableStateOf("") }
    var dateInMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Add Maintenance Log",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Details (Oil change, spark plug, brakes...)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = odometerStr,
                        onValueChange = { odometerStr = it },
                        label = { Text("Odometer (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text("Service Cost (LKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = targetOdoStr,
                    onValueChange = { targetOdoStr = it },
                    label = { Text("Next Service Due Target (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val c = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val sel = Calendar.getInstance()
                                    sel.set(y, m, d)
                                    dateInMillis = sel.timeInMillis
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Service Date", style = MaterialTheme.typography.bodyMedium)
                        Text(DateUtils.formatDate(dateInMillis), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val odo = odometerStr.toDoubleOrNull() ?: 0.0
                            val cost = costStr.toDoubleOrNull() ?: 0.0
                            val next = targetOdoStr.toDoubleOrNull() ?: (odo + 5000.0) // fallback default plus 5k km
                            onSave(odo, cost, details, next, dateInMillis)
                        }
                    ) {
                        Text("Save Service")
                    }
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (category: String, cost: Double, notes: String, date: Long) -> Unit
) {
    val context = LocalContext.current
    var category by remember { mutableStateOf("Wash & Detail") }
    var costStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dateInMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val categories = listOf("Wash & Detail", "Accessories", "Road Tax & License", "Insurance Premium", "Spares / Parts", "Parking/Tolls", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Add General Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Category selector chip carousel
                Text("Select Category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text(category + " \u25BC")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    label = { Text("Cost Amount (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Comments") },
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val c = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val sel = Calendar.getInstance()
                                    sel.set(y, m, d)
                                    dateInMillis = sel.timeInMillis
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Expense Date", style = MaterialTheme.typography.bodyMedium)
                        Text(DateUtils.formatDate(dateInMillis), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cost = costStr.toDoubleOrNull() ?: 0.0
                            onSave(category, cost, notes, dateInMillis)
                        }
                    ) {
                        Text("Save Expense")
                    }
                }
            }
        }
    }
}

@Composable
fun AddEmissionDialog(
    onDismiss: () -> Unit,
    onSave: (slip: String, center: String, isPassed: Boolean, cost: Double, expireDate: Long, date: Long) -> Unit
) {
    val context = LocalContext.current
    var slipNo by remember { mutableStateOf("") }
    var center by remember { mutableStateOf("") }
    var isPassed by remember { mutableStateOf(true) }
    var costStr by remember { mutableStateOf("") }
    var dateInMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var expiryInMillis by remember {
        val c = Calendar.getInstance()
        c.add(Calendar.YEAR, 1) // default test lasts 1 year
        mutableStateOf(c.timeInMillis)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Add Emission Test",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = slipNo,
                    onValueChange = { slipNo = it },
                    label = { Text("Slip Certificate Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = center,
                    onValueChange = { center = it },
                    label = { Text("Testing Station / Center") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Inspection Status", fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val passColor = if (isPassed) Color(0xFF4CAF50) else Color(0xFFFF5252)
                        Text(
                            text = if (isPassed) "PASSED" else "FAILED",
                            color = passColor,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = isPassed,
                            onCheckedChange = { isPassed = it }
                        )
                    }
                }

                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    label = { Text("Inspection Fee Cost (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Test Date
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val c = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val sel = Calendar.getInstance()
                                        sel.set(y, m, d)
                                        dateInMillis = sel.timeInMillis
                                    },
                                    c.get(Calendar.YEAR),
                                    c.get(Calendar.MONTH),
                                    c.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Test Date", style = MaterialTheme.typography.labelSmall)
                            Text(DateUtils.formatDateShort(dateInMillis), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Expiry Date
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val c = Calendar.getInstance().apply { timeInMillis = expiryInMillis }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val sel = Calendar.getInstance()
                                        sel.set(y, m, d)
                                        expiryInMillis = sel.timeInMillis
                                    },
                                    c.get(Calendar.YEAR),
                                    c.get(Calendar.MONTH),
                                    c.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Expiry Date", style = MaterialTheme.typography.labelSmall)
                            Text(DateUtils.formatDateShort(expiryInMillis), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cost = costStr.toDoubleOrNull() ?: 0.0
                            onSave(slipNo, center, isPassed, cost, expiryInMillis, dateInMillis)
                        }
                    ) {
                        Text("Add Emission")
                    }
                }
            }
        }
    }
}
