package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.AppTab
import com.example.ui.TowerOpsViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun InteractiveMapScreen(
    viewModel: TowerOpsViewModel,
    modifier: Modifier = Modifier
) {
    val towers by viewModel.towers.collectAsState()
    val technicians by viewModel.technicians.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    val telemetryMap by viewModel.telemetryMap.collectAsState()
    val selectedTowerId by viewModel.selectedTowerId.collectAsState()
    val selectedTower by viewModel.selectedTower.collectAsState()

    var showCoverageRings by remember { mutableStateOf(true) }
    var showTechnicians by remember { mutableStateOf(true) }
    var dispatchAlert by remember { mutableStateOf<FaultAlert?>(null) }

    val currentTelemetry = selectedTowerId?.let { telemetryMap[it] }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Full Canvas Map
        InteractiveTowerMap(
            towers = towers,
            technicians = technicians,
            activeAlerts = activeAlerts,
            selectedTowerId = selectedTowerId,
            onTowerSelected = { tower ->
                viewModel.selectTower(tower.id)
            },
            showCoverageRings = showCoverageRings,
            showTechnicians = showTechnicians,
            modifier = Modifier.fillMaxSize()
        )

        // Top Glassmorphic Controls Bar
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.92f)),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Peta Interaktif Presisi", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            StatusPulseDot(color = CyanPrimary, size = 6)
                        }
                        Text("${towers.size} Menara • ${technicians.size} Teknisi Siaga", color = TextSecondary, fontSize = 11.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = showCoverageRings,
                            onClick = { showCoverageRings = !showCoverageRings },
                            label = { Text("Jangkauan", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary
                            )
                        )
                        FilterChip(
                            selected = showTechnicians,
                            onClick = { showTechnicians = !showTechnicians },
                            label = { Text("Tim", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                selectedLabelColor = ElectricBlue
                            )
                        )
                    }
                }
            }
        }

        // Floating Bottom Card for Selected Tower
        selectedTower?.let { tower ->
            val activeTowerAlert = activeAlerts.find { it.towerId == tower.id && !it.isResolved }
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("map_selected_tower_sheet"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.95f)),
                border = BorderStroke(1.5.dp, if (activeTowerAlert != null) StatusRed else CyanPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(tower.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                TowerStatusBadge(tower.status)
                            }
                            Text(
                                "Site ID: ${tower.siteCode} • Lat: ${String.format(Locale.US, "%.4f", tower.latitude)}, Long: ${String.format(Locale.US, "%.4f", tower.longitude)}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(onClick = { viewModel.selectTab(AppTab.TOWERS) }) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Buka Detail", tint = CyanPrimary)
                        }
                    }

                    // Telemetry Quick Glance
                    if (currentTelemetry != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text("PLN Grid", color = TextSecondary, fontSize = 9.sp)
                                    Text("${currentTelemetry.gridVoltage.toInt()} V", color = if (currentTelemetry.gridOnline) StatusGreen else StatusRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text("Baterai", color = TextSecondary, fontSize = 9.sp)
                                    Text("${currentTelemetry.batterySoC.toInt()}%", color = if (currentTelemetry.batterySoC > 35) StatusGreen else StatusRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text("VSWR RF", color = TextSecondary, fontSize = 9.sp)
                                    Text(String.format(Locale.US, "%.2f", currentTelemetry.vswrRatio), color = if (currentTelemetry.vswrRatio < 1.8) StatusGreen else StatusRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text("Kabin", color = TextSecondary, fontSize = 9.sp)
                                    Text("${currentTelemetry.cabinTemp.toInt()}°C", color = if (currentTelemetry.cabinTemp < 30) StatusGreen else StatusAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (activeTowerAlert != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusRedBg.copy(alpha = 0.4f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Alarm: ${activeTowerAlert.title}", color = StatusRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("RCA: ${activeTowerAlert.rootCauseRecommendation}", color = TextPrimary, fontSize = 10.sp, maxLines = 1)
                            }
                            Button(
                                onClick = { dispatchAlert = activeTowerAlert },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Tugaskan", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    dispatchAlert?.let { alert ->
        val targetTower = towers.find { it.id == alert.towerId }
        DispatchDialog(
            alert = alert,
            tower = targetTower,
            availableTechnicians = technicians,
            onDismiss = { dispatchAlert = null },
            onDispatch = { techId, instructions, priority ->
                viewModel.dispatchTechnician(
                    alertId = alert.id,
                    towerId = alert.towerId,
                    towerName = alert.towerName,
                    technicianId = techId,
                    instructions = instructions,
                    priority = priority
                )
            }
        )
    }
}
