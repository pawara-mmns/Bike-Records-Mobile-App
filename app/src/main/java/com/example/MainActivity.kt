package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.BikeViewModel
import com.example.ui.DashboardScreen
import com.example.ui.LogsScreen
import com.example.ui.SpendingScreen
import com.example.ui.AlertRemindersHubDialog
import com.example.ui.theme.MyApplicationTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Instantiate our bike records ViewModel
                val viewModel: BikeViewModel = viewModel()

                // Seed sample bike and records if first run so the UI is immediately fully interactive
                LaunchedEffect(Unit) {
                    viewModel.initializeIfEmpty()
                }

                BikeRecordsApp(viewModel)
            }
        }
    }
}

enum class ScreenTab {
    DASHBOARD, SPENDING, RECORD_LOGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeRecordsApp(viewModel: BikeViewModel) {
    var selectedTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    var showRemindersHub by remember { mutableStateOf(false) }
    val bike by viewModel.bikeDetails.collectAsState()
    val activeBikeName = if (bike.brand.isEmpty() && bike.model == "My Bike") "Your Bike Profile" else "${bike.brand} ${bike.model}".trim()
    val activeOdometer = String.format(Locale.getDefault(), "%,.0f", bike.currentOdometer)

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_top_bar"),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // The sleek accent color-coded motorcycle avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = "Bike Active Badge",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = activeBikeName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Active • $activeOdometer km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Elegant notifications/action bell
                    IconButton(
                        onClick = { showRemindersHub = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications indicator",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == ScreenTab.DASHBOARD,
                    onClick = { selectedTab = ScreenTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home Cockpit") },
                    label = { Text("Cockpit") },
                    modifier = Modifier.testTag("nav_tab_cockpit")
                )
                NavigationBarItem(
                    selected = selectedTab == ScreenTab.SPENDING,
                    onClick = { selectedTab = ScreenTab.SPENDING },
                    icon = { Icon(Icons.Default.InsertChart, contentDescription = "Monthly Spend") },
                    label = { Text("Spending") },
                    modifier = Modifier.testTag("nav_tab_spending")
                )
                NavigationBarItem(
                    selected = selectedTab == ScreenTab.RECORD_LOGS,
                    onClick = { selectedTab = ScreenTab.RECORD_LOGS },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "History Log Logs") },
                    label = { Text("Log Registry") },
                    modifier = Modifier.testTag("nav_tab_logs")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                ScreenTab.DASHBOARD -> {
                    DashboardScreen(viewModel = viewModel)
                }
                ScreenTab.SPENDING -> {
                    SpendingScreen(viewModel = viewModel)
                }
                ScreenTab.RECORD_LOGS -> {
                    LogsScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showRemindersHub) {
        AlertRemindersHubDialog(
            viewModel = viewModel,
            bike = bike,
            onDismiss = { showRemindersHub = false }
        )
    }
}

