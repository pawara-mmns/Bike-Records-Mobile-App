package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.util.*

@Composable
fun SpendingScreen(
    viewModel: BikeViewModel,
    modifier: Modifier = Modifier
) {
    val fuelLogs by viewModel.allFuelLogs.collectAsState()
    val expenseLogs by viewModel.allExpenseLogs.collectAsState()
    val serviceLogs by viewModel.allServiceLogs.collectAsState()
    val emissionLogs by viewModel.allEmissionLogs.collectAsState()

    val currentYearMonth = remember {
        val cal = Calendar.getInstance()
        val key = String.format(Locale.getDefault(), "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        key
    }

    // Spend aggregation grouping
    val monthlySpends = remember(fuelLogs, expenseLogs, serviceLogs, emissionLogs) {
        val map = mutableMapOf<String, Pair<Double, Double>>() // monthKey -> Pair(fuelSpend, otherSpend)

        fuelLogs.forEach { log ->
            val key = DateUtils.getYearMonthKey(log.date)
            val current = map[key] ?: Pair(0.0, 0.0)
            map[key] = Pair(current.first + log.cost, current.second)
        }

        expenseLogs.forEach { log ->
            val key = DateUtils.getYearMonthKey(log.date)
            val current = map[key] ?: Pair(0.0, 0.0)
            map[key] = Pair(current.first, current.second + log.cost)
        }

        serviceLogs.forEach { log ->
            val key = DateUtils.getYearMonthKey(log.date)
            val current = map[key] ?: Pair(0.0, 0.0)
            map[key] = Pair(current.first, current.second + log.cost)
        }

        emissionLogs.forEach { log ->
            val key = DateUtils.getYearMonthKey(log.date)
            val current = map[key] ?: Pair(0.0, 0.0)
            map[key] = Pair(current.first, current.second + log.cost)
        }

        map.entries.sortedByDescending { it.key }
    }

    // General Life averages
    val totalSpentAllTime = remember(fuelLogs, expenseLogs, serviceLogs, emissionLogs) {
        fuelLogs.sumOf { it.cost } + expenseLogs.sumOf { it.cost } + serviceLogs.sumOf { it.cost } + emissionLogs.sumOf { it.cost }
    }

    val totalFuelSpentAllTime = remember(fuelLogs) {
        fuelLogs.sumOf { it.cost }
    }

    // Fuel economy metric tracker
    val avgFuelEconomy = remember(fuelLogs) {
        if (fuelLogs.size < 2) return@remember null
        val sorted = fuelLogs.sortedBy { it.odometer }
        val minOdo = sorted.first().odometer
        val maxOdo = sorted.last().odometer
        val totalDistance = maxOdo - minOdo
        val totalVolumeRefills = sorted.drop(1).sumOf { it.liters }
        if (totalVolumeRefills > 0.0 && totalDistance > 0.0) {
            totalDistance / totalVolumeRefills // km per liter
        } else {
            null
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Title
        Text(
            text = "BIKE SPENDING ANALYSIS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // 1. Highlight statistics cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedCard(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = "Total spend icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TOTAL SPENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "Rs. %,.2f", totalSpentAllTime),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedCard(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = "Fuel economy icon",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AVG FUEL MILEAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (avgFuelEconomy != null) String.format(Locale.getDefault(), "%.1f km/L", avgFuelEconomy) else "Pending logs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (avgFuelEconomy != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // 2. This Current Month's Breakdown (Solving prompt explicit constraints: "How much spend per month? How much cost for fuel?")
        val currentMonthData = monthlySpends.find { it.key == currentYearMonth }
        val currentMonthFuel = currentMonthData?.value?.first ?: 0.0
        val currentMonthOther = currentMonthData?.value?.second ?: 0.0
        val currentMonthTotal = currentMonthFuel + currentMonthOther

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Current Month Summary (${DateUtils.formatYearMonthKey(currentYearMonth)})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fuel Cost This Month",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthFuel),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Other Expenses",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthOther),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom stacked visual progress tracker (Fuel vs Other)
                if (currentMonthTotal > 0.0) {
                    val fuelPercentage = (currentMonthFuel / currentMonthTotal).toFloat()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(fuelPercentage.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Box(
                            modifier = Modifier
                                .weight((1f - fuelPercentage).coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "Fuel (%.0f%%)", fuelPercentage * 100),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "Other (%.0f%%)", (1f - fuelPercentage) * 100),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No recorded costs for this current month yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // 3. Historical Monthly Spending Logs
        Text(
            text = "MONTHLY BILLING TRENDS",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp).padding(horizontal = 4.dp)
        )

        if (monthlySpends.isEmpty()) {
            // Empty state helper
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertChartOutlined,
                        contentDescription = "Analysis Empty",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Add some service, fuel, or washing records to populate monthly metrics chart maps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            // Display lists of months
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                monthlySpends.forEach { entry ->
                    val monthKey = entry.key
                    val fuelAmt = entry.value.first
                    val otherAmt = entry.value.second
                    val totalAmt = fuelAmt + otherAmt

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = DateUtils.formatYearMonthKey(monthKey),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = String.format(Locale.getDefault(), "Rs. %,.2f", totalAmt),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalGasStation,
                                        contentDescription = "Fuel spent",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Fuel cost: " + String.format(Locale.getDefault(), "Rs. %,.2f", fuelAmt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = "Misc spent",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Other: " + String.format(Locale.getDefault(), "Rs. %,.2f", otherAmt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
