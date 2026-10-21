package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ui.TowerOpsViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FaultDetectionScreen(
    viewModel: TowerOpsViewModel,
    modifier: Modifier = Modifier
) {
    val allAlerts by viewModel.allAlerts.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    val towers by viewModel.towers.collectAsState()
    val technicians by viewModel.technicians.collectAsState()
    val selectedTowerId by viewModel.selectedTowerId.collectAsState()

    var filterActiveOnly by remember { mutableStateOf(true) }
    var filterSeverity by remember { mutableStateOf<AlertSeverity?>(null) }
    var showFaultDialog by remember { mutableStateOf(false) }
    var dispatchTargetAlert by remember { mutableStateOf<FaultAlert?>(null) }

    val displayAlerts = (if (filterActiveOnly) activeAlerts else allAlerts).filter { alert ->
        filterSeverity == null || alert.severity == filterSeverity
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss 'WIB'", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Deteksi Kerusakan Real-Time", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    StatusPulseDot(color = StatusRed, size = 8, pulse = true)
                }
                Text("Algoritma deteksi anomali sensor telekomunikasi & RCA", color = TextSecondary, fontSize = 12.sp)
            }

            Button(
                onClick = { showFaultDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = StatusAmber, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_trigger_fault_screen")
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Uji Anomali", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Summary Bar: Critical vs Major vs Minor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val critCount = activeAlerts.count { it.severity == AlertSeverity.CRITICAL }
            val majorCount = activeAlerts.count { it.severity == AlertSeverity.MAJOR }
            val minorCount = activeAlerts.count { it.severity == AlertSeverity.MINOR }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = StatusRedBg.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, StatusRed)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPulseDot(color = StatusRed, size = 8, pulse = critCount > 0)
                    Column {
                        Text("$critCount Kritis", color = StatusRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Dampak SLA Tinggi", color = TextSecondary, fontSize = 9.sp)
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = StatusAmberBg.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, StatusAmber)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPulseDot(color = StatusAmber, size = 8, pulse = false)
                    Column {
                        Text("$majorCount Mayor", color = StatusAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Perlu Tindakan", color = TextSecondary, fontSize = 9.sp)
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPulseDot(color = StatusBlue, size = 8, pulse = false)
                    Column {
                        Text("$minorCount Minor", color = StatusBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Preventif", color = TextSecondary, fontSize = 9.sp)
                    }
                }
            }
        }

        // Filters: Toggle Active / History & Severity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = filterActiveOnly,
                    onClick = { filterActiveOnly = true },
                    label = { Text("Aktif (${activeAlerts.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusRed.copy(alpha = 0.2f),
                        selectedLabelColor = StatusRed
                    )
                )
                FilterChip(
                    selected = !filterActiveOnly,
                    onClick = { filterActiveOnly = false },
                    label = { Text("Semua Riwayat (${allAlerts.size})", fontSize = 11.sp) }
                )
            }

            // Severity Filter Dropdown or Quick Pills
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val severities = listOf(null, AlertSeverity.CRITICAL, AlertSeverity.MAJOR, AlertSeverity.MINOR)
                severities.forEach { sev ->
                    val isSelected = filterSeverity == sev
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CyanPrimary.copy(alpha = 0.25f) else DarkSurfaceElevated)
                            .border(1.dp, if (isSelected) CyanPrimary else DarkBorder, RoundedCornerShape(6.dp))
                            .clickable { filterSeverity = sev }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = sev?.name ?: "All",
                            color = if (isSelected) CyanPrimary else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Alerts List
        if (displayAlerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(48.dp))
                    Text("Semua Perangkat Menara Beroperasi Normal", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Tidak ada anomali atau alarm aktif pada filter ini", color = TextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayAlerts, key = { it.id }) { alert ->
                    val isAssigned = alert.assignedTechnicianId != null
                    val alertTower = towers.find { it.id == alert.towerId }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alert_card_${alert.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (alert.isResolved) DarkSurface.copy(alpha = 0.6f) else DarkSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            when {
                                alert.isResolved -> DarkBorder
                                alert.severity == AlertSeverity.CRITICAL -> StatusRed
                                alert.severity == AlertSeverity.MAJOR -> StatusAmber
                                else -> DarkBorder
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Top Row: Severity, Subsystem, Timestamp
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AlertSeverityBadge(alert.severity)
                                    SubsystemBadge(alert.subsystem)
                                    if (alert.isResolved) {
                                        Text("[TERSELESAIKAN]", color = StatusGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    timeFormat.format(Date(alert.timestamp)),
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            // Alert Title & Tower
                            Text(
                                text = alert.title,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "${alert.towerName} (${alert.towerId})",
                                    color = CyanPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Description & Root Cause Recommendation
                            Text(
                                text = alert.description,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            // AI & Expert Root Cause Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(10.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Analisis Akar Masalah & Rekomendasi SOP:", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(alert.rootCauseRecommendation, color = TextPrimary, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Bottom Dispatch Action Bar
                            if (!alert.isResolved) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isAssigned) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Engineering, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                                            Text(
                                                "Ditugaskan ke: ${alert.assignedTechnicianName}",
                                                color = StatusGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Text("Status: Belum Ditugaskan", color = StatusAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = { dispatchTargetAlert = alert },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAssigned) DarkSurfaceElevated else CyanPrimary,
                                            contentColor = if (isAssigned) CyanPrimary else Color.Black
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("btn_dispatch_${alert.id}")
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (isAssigned) "Ganti Teknisi" else "Tugaskan Tim", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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

    dispatchTargetAlert?.let { alert ->
        val targetTower = towers.find { it.id == alert.towerId }
        DispatchDialog(
            alert = alert,
            tower = targetTower,
            availableTechnicians = technicians,
            onDismiss = { dispatchTargetAlert = null },
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
