package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AlertSeverity
import com.example.ui.AppTab
import com.example.ui.TowerOpsViewModel
import com.example.ui.components.StatusPulseDot
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TowerOpsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TowerOpsApp(viewModel: TowerOpsViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val criticalAlertCount = activeAlerts.count { it.severity == AlertSeverity.CRITICAL }

    // Collect real-time alerts for live toast notifications
    LaunchedEffect(Unit) {
        viewModel.recentAlertEvent.collectLatest { alert ->
            val result = snackbarHostState.showSnackbar(
                message = "ANOMALI: ${alert.towerName} - ${alert.title}",
                actionLabel = "Lihat",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.selectTower(alert.towerId)
                viewModel.selectTab(AppTab.ALERTS)
            }
        }
    }

    // Handle back button to return to dashboard
    BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
        viewModel.selectTab(AppTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = StatusRed,
                    contentColor = Color.White,
                    actionColor = Color.Yellow,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TowerOps",
                            color = CyanPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(DarkSurfaceElevated, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            StatusPulseDot(
                                color = if (criticalAlertCount > 0) StatusRed else StatusGreen,
                                size = 6,
                                pulse = true
                            )
                            Text(
                                text = if (criticalAlertCount > 0) "$criticalAlertCount ALARM" else "NOC OPTIMAL",
                                color = if (criticalAlertCount > 0) StatusRed else StatusGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    if (currentTab != AppTab.REPORTS) {
                        IconButton(
                            onClick = { viewModel.selectTab(AppTab.REPORTS) },
                            modifier = Modifier.testTag("top_action_web_portal")
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = "Dasbor Web",
                                tint = CyanPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Triple(AppTab.DASHBOARD, "NOC", Icons.Default.Dashboard),
                    Triple(AppTab.TOWERS, "Menara", Icons.Default.CellTower),
                    Triple(AppTab.ALERTS, "Kerusakan", Icons.Default.Warning),
                    Triple(AppTab.MAP, "Peta", Icons.Default.Map),
                    Triple(AppTab.TECHNICIANS, "Teknisi", Icons.Default.Engineering),
                    Triple(AppTab.REPORTS, "Web & Lap", Icons.Default.Assessment)
                )

                tabs.forEach { (tab, label, icon) ->
                    val isSelected = currentTab == tab
                    val hasAlertBadge = tab == AppTab.ALERTS && criticalAlertCount > 0

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (hasAlertBadge) {
                                        Badge(containerColor = StatusRed) {
                                            Text("$criticalAlertCount", color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppTab.TOWERS -> TowerListDetailScreen(viewModel = viewModel)
                AppTab.ALERTS -> FaultDetectionScreen(viewModel = viewModel)
                AppTab.MAP -> InteractiveMapScreen(viewModel = viewModel)
                AppTab.TECHNICIANS -> TechnicianTeamScreen(viewModel = viewModel)
                AppTab.REPORTS -> WebDashboardReportScreen(viewModel = viewModel)
            }
        }
    }
}
