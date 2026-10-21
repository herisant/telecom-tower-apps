package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.AppTab
import com.example.ui.TowerOpsViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: TowerOpsViewModel,
    modifier: Modifier = Modifier
) {
    val towers by viewModel.towers.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    val technicians by viewModel.technicians.collectAsState()
    val telemetryMap by viewModel.telemetryMap.collectAsState()
    val selectedTowerId by viewModel.selectedTowerId.collectAsState()
    val selectedTower by viewModel.selectedTower.collectAsState()

    var showFaultDialog by remember { mutableStateOf(false) }
    var dispatchAlert by remember { mutableStateOf<FaultAlert?>(null) }

    val currentTelemetry = selectedTowerId?.let { telemetryMap[it] }
    val criticalAlerts = activeAlerts.filter { it.severity == AlertSeverity.CRITICAL }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Card with NOC Visual Asset
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_tower_hero),
                    contentDescription = "Tower Operations NOC Center",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC090D16), Color(0xF5090D16))
                            )
                        )
                )

                // Hero Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkSurface.copy(alpha = 0.85f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            StatusPulseDot(color = StatusGreen, size = 8)
                            Text(
                                text = "NOC TELEMETRY LIVE",
                                color = StatusGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Button(
                            onClick = { showFaultDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusAmber, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_trigger_anomaly_simulation")
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Uji Anomali", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Column {
                        Text(
                            text = "Pusat Kontrol Tower & IoT",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Pemantauan telemetri real-time, kesehatan BTS & respons tim teknisi presisi",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Active Alert Warning Banner (If Any Critical Alerts exist)
        if (criticalAlerts.isNotEmpty()) {
            val topAlert = criticalAlerts.first()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("critical_alert_card"),
                colors = CardDefaults.cardColors(containerColor = StatusRedBg.copy(alpha = 0.45f)),
                border = BorderStroke(1.5.dp, StatusRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusPulseDot(color = StatusRed, size = 12, pulse = true)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ALARM KRITIS TERDETEKSI",
                                color = StatusRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            AlertSeverityBadge(topAlert.severity)
                        }
                        Text(
                            text = "${topAlert.towerName}: ${topAlert.title}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = topAlert.description,
                            color = Color(0xFFFECACA),
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }

                    Button(
                        onClick = { dispatchAlert = topAlert },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_quick_dispatch")
                    ) {
                        Text("Tugaskan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick KPI Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val totalTowers = towers.size
            val normalCount = towers.count { it.status == TowerStatus.NORMAL }
            val slaUptime = if (criticalAlerts.isEmpty()) 99.98f else (99.98f - (criticalAlerts.size * 0.12f)).coerceAtLeast(98.2f)

            TelemetryMetricCard(
                title = "Ketersediaan SLA",
                value = String.format(Locale.US, "%.2f", slaUptime),
                unit = "%",
                icon = Icons.Default.Speed,
                statusColor = if (slaUptime > 99.5f) StatusGreen else StatusAmber,
                modifier = Modifier.weight(1f),
                subValue = "Target: 99.90%"
            )

            TelemetryMetricCard(
                title = "Menara Aktif",
                value = "$normalCount / $totalTowers",
                unit = "Site",
                icon = Icons.Default.CellTower,
                statusColor = CyanPrimary,
                modifier = Modifier.weight(1f),
                subValue = "${towers.count { it.status != TowerStatus.NORMAL }} Anomali"
            )

            val onDutyTech = technicians.count { it.status != "AVAILABLE" && it.status != "OFF_DUTY" }
            TelemetryMetricCard(
                title = "Tim Lapangan",
                value = "$onDutyTech",
                unit = "Di Jalan",
                icon = Icons.Default.Engineering,
                statusColor = ElectricBlue,
                modifier = Modifier.weight(1f),
                subValue = "${technicians.size} Total"
            )
        }

        // Selected Tower Real-Time Sensor Telemetry
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Sensor Telemetri Live",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            StatusPulseDot(color = CyanPrimary, size = 6)
                        }
                        Text(
                            text = selectedTower?.let { "${it.name} (${it.siteCode})" } ?: "Pilih Menara",
                            color = CyanPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    selectedTower?.let { TowerStatusBadge(it.status) }
                }

                // Sensor Grid
                if (currentTelemetry != null) {
                    // Power & Electrical Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TelemetryMetricCard(
                            title = "Tegangan PLN",
                            value = "${currentTelemetry.gridVoltage.toInt()}",
                            unit = "Volt AC",
                            icon = Icons.Default.ElectricBolt,
                            statusColor = if (currentTelemetry.gridOnline) StatusGreen else StatusRed,
                            modifier = Modifier.weight(1f),
                            subValue = if (currentTelemetry.gridOnline) "Grid Online" else "PLN Drop!"
                        )

                        TelemetryMetricCard(
                            title = "Baterai Lithium",
                            value = "${currentTelemetry.batterySoC.toInt()}",
                            unit = "% SoC",
                            icon = Icons.Default.BatteryChargingFull,
                            statusColor = if (currentTelemetry.batterySoC > 40f) StatusGreen else StatusRed,
                            modifier = Modifier.weight(1f),
                            subValue = "${currentTelemetry.batteryTemp}°C"
                        )

                        TelemetryMetricCard(
                            title = "BBM Genset",
                            value = "${currentTelemetry.gensetFuelPercent.toInt()}",
                            unit = "% Solar",
                            icon = Icons.Default.LocalGasStation,
                            statusColor = if (currentTelemetry.gensetFuelPercent > 30f) CyanPrimary else StatusAmber,
                            modifier = Modifier.weight(1f),
                            subValue = if (currentTelemetry.gensetRunning) "Genset ON" else "Standby"
                        )
                    }

                    // Radio RF & Environment Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TelemetryMetricCard(
                            title = "Suhu Shelter",
                            value = String.format(Locale.US, "%.1f", currentTelemetry.cabinTemp),
                            unit = "°C",
                            icon = Icons.Default.Thermostat,
                            statusColor = if (currentTelemetry.cabinTemp < 30f) StatusGreen else StatusAmber,
                            modifier = Modifier.weight(1f),
                            subValue = "Kelembaban: ${currentTelemetry.humidity.toInt()}%"
                        )

                        TelemetryMetricCard(
                            title = "VSWR Antena",
                            value = String.format(Locale.US, "%.2f", currentTelemetry.vswrRatio),
                            unit = ": 1",
                            icon = Icons.Default.Sensors,
                            statusColor = if (currentTelemetry.vswrRatio < 1.8f) StatusGreen else StatusRed,
                            modifier = Modifier.weight(1f),
                            subValue = if (currentTelemetry.vswrRatio < 1.8f) "Normal Match" else "RF Fault!"
                        )

                        TelemetryMetricCard(
                            title = "Throughput 5G",
                            value = "${currentTelemetry.throughputMbps.toInt()}",
                            unit = "Mbps",
                            icon = Icons.Default.Speed,
                            statusColor = ElectricBlue,
                            modifier = Modifier.weight(1f),
                            subValue = "Ping: ${currentTelemetry.latencyMs}ms"
                        )
                    }

                    // Progress bars for subsystem health
                    MetricProgressBar(
                        label = "Beban Komputasi BBU (Baseband Unit)",
                        valueString = "${currentTelemetry.bbuLoadPercent.toInt()}%",
                        progress = currentTelemetry.bbuLoadPercent / 100f,
                        color = if (currentTelemetry.bbuLoadPercent > 80f) StatusAmber else CyanPrimary
                    )
                }
            }
        }

        // Mini GIS Interactive Map Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBorder)
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
                        Text("Peta Persebaran Menara Presisi", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Koordinat GPS aktual BTS telekomunikasi dan posisi teknisi", color = TextSecondary, fontSize = 11.sp)
                    }
                    TextButton(onClick = { viewModel.selectTab(AppTab.MAP) }) {
                        Text("Peta Penuh", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                    }
                }

                InteractiveTowerMap(
                    towers = towers,
                    technicians = technicians,
                    activeAlerts = activeAlerts,
                    selectedTowerId = selectedTowerId,
                    onTowerSelected = { tower ->
                        viewModel.selectTower(tower.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }
    }

    // Modals
    if (showFaultDialog) {
        FaultInjectionDialog(
            towers = towers,
            selectedTowerId = selectedTowerId ?: "TWR-JKT-042",
            onDismiss = { showFaultDialog = false },
            onInject = { towerId, anomaly, name ->
                viewModel.triggerFaultSimulation(towerId, anomaly, name)
            }
        )
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
