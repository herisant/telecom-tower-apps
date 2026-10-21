package com.example.ui.screens

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
import com.example.ui.AppTab
import com.example.ui.TowerOpsViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TowerListDetailScreen(
    viewModel: TowerOpsViewModel,
    modifier: Modifier = Modifier
) {
    val towers by viewModel.towers.collectAsState()
    val telemetryMap by viewModel.telemetryMap.collectAsState()
    val selectedTowerId by viewModel.selectedTowerId.collectAsState()
    val selectedTower by viewModel.selectedTower.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("Semua") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showFaultDialog by remember { mutableStateOf(false) }

    val regions = listOf("Semua", "Jabodetabek", "Jawa Barat", "Jawa Timur", "Jawa Tengah", "Bali & Nusa Tenggara", "Sumatera Bagian Utara")

    val filtered = towers.filter { tower ->
        (selectedRegion == "Semua" || tower.region == selectedRegion) &&
        (searchQuery.isEmpty() || tower.name.contains(searchQuery, ignoreCase = true) || tower.siteCode.contains(searchQuery, ignoreCase = true) || tower.city.contains(searchQuery, ignoreCase = true))
    }

    val currentTelemetry = selectedTowerId?.let { telemetryMap[it] }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Inventaris Menara & IoT", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Diagnostik perangkat telekomunikasi per site", color = TextSecondary, fontSize = 12.sp)
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_add_tower")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Tambah Site", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Search & Region Filter Chips
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari kode site, nama, atau kota...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = DarkBorder
            )
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(regions) { region ->
                val isSelected = region == selectedRegion
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedRegion = region },
                    label = { Text(region, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = CyanPrimary,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = DarkBorder,
                        selectedBorderColor = CyanPrimary
                    )
                )
            }
        }

        // Content Area: Horizontal List of Towers + Detailed Subsystem Inspector
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Horizontal Carousel / Selector for Sites
            item {
                Text("Daftar Menara (${filtered.size} Site):", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered) { tower ->
                        val isSelected = tower.id == selectedTowerId
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { viewModel.selectTower(tower.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.12f) else DarkSurface
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) CyanPrimary else DarkBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tower.siteCode, color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    TowerStatusBadge(tower.status)
                                }
                                Text(
                                    tower.name,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    "${tower.city} • ${tower.heightMeters}m ${tower.towerType.name.replace("_", " ")}",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Subsystem Diagnostic Inspector for Selected Tower
            item {
                if (selectedTower != null) {
                    val tower = selectedTower!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Header Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(tower.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        TowerStatusBadge(tower.status)
                                    }
                                    Text(
                                        "${tower.address} • Lat: ${String.format(Locale.US, "%.5f", tower.latitude)}, Long: ${String.format(Locale.US, "%.5f", tower.longitude)}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        "Operator Terkoneksi: ${tower.operatorShared}",
                                        color = ElectricBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = { viewModel.selectTab(AppTab.MAP) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = "Lihat Peta", tint = CyanPrimary)
                                    }
                                    IconButton(
                                        onClick = { showFaultDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.BugReport, contentDescription = "Uji Anomali", tint = StatusAmber)
                                    }
                                }
                            }

                            HorizontalDivider(color = DarkBorder)

                            // Subsystem 1: Power & Genset
                            Text("1. SUBSISTEM KELISTRIKAN & GENSET", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(tower.powerSource, color = TextSecondary, fontSize = 11.sp)

                            if (currentTelemetry != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TelemetryMetricCard(
                                        title = "Grid PLN",
                                        value = "${currentTelemetry.gridVoltage.toInt()}",
                                        unit = "V",
                                        icon = Icons.Default.Bolt,
                                        statusColor = if (currentTelemetry.gridOnline) StatusGreen else StatusRed,
                                        modifier = Modifier.weight(1f),
                                        subValue = if (currentTelemetry.gridOnline) "Normal" else "Mati Total!"
                                    )
                                    TelemetryMetricCard(
                                        title = "Genset Cadangan",
                                        value = "${currentTelemetry.gensetFuelPercent.toInt()}",
                                        unit = "% Solar",
                                        icon = Icons.Default.LocalGasStation,
                                        statusColor = if (currentTelemetry.gensetFuelPercent > 30f) CyanPrimary else StatusAmber,
                                        modifier = Modifier.weight(1f),
                                        subValue = if (currentTelemetry.gensetRunning) "Running" else "Standby"
                                    )
                                    TelemetryMetricCard(
                                        title = "Lithium SoC",
                                        value = "${currentTelemetry.batterySoC.toInt()}",
                                        unit = "%",
                                        icon = Icons.Default.BatteryChargingFull,
                                        statusColor = if (currentTelemetry.batterySoC > 35f) StatusGreen else StatusRed,
                                        modifier = Modifier.weight(1f),
                                        subValue = "${currentTelemetry.batteryTemp}°C"
                                    )
                                }
                            }

                            HorizontalDivider(color = DarkBorder)

                            // Subsystem 2: Radio Frequency & RRU
                            Text("2. SUBSISTEM RADIO FREKUENSI (RF & ANTENA)", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (currentTelemetry != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TelemetryMetricCard(
                                        title = "VSWR Antena",
                                        value = String.format(Locale.US, "%.2f", currentTelemetry.vswrRatio),
                                        unit = ":1",
                                        icon = Icons.Default.CellTower,
                                        statusColor = if (currentTelemetry.vswrRatio < 1.8f) StatusGreen else StatusRed,
                                        modifier = Modifier.weight(1f),
                                        subValue = if (currentTelemetry.vswrRatio < 1.8f) "Impedansi OK" else "Refleksi Tinggi!"
                                    )
                                    TelemetryMetricCard(
                                        title = "Suhu RRU",
                                        value = String.format(Locale.US, "%.1f", currentTelemetry.rruTemp),
                                        unit = "°C",
                                        icon = Icons.Default.Thermostat,
                                        statusColor = if (currentTelemetry.rruTemp < 55f) StatusGreen else StatusAmber,
                                        modifier = Modifier.weight(1f),
                                        subValue = "Carrier: ${currentTelemetry.activeCarriers} Bands"
                                    )
                                    TelemetryMetricCard(
                                        title = "Loss SFP Optik",
                                        value = String.format(Locale.US, "%.1f", currentTelemetry.opticalSfpLossDb),
                                        unit = "dB",
                                        icon = Icons.Default.Router,
                                        statusColor = if (currentTelemetry.opticalSfpLossDb < 3.0f) StatusGreen else StatusAmber,
                                        modifier = Modifier.weight(1f),
                                        subValue = "Fiber Backhaul"
                                    )
                                }
                            }

                            HorizontalDivider(color = DarkBorder)

                            // Subsystem 3: Shelter IoT & Keamanan Lingkungan
                            Text("3. IOT SHELTER, LINGKUNGAN & KESELAMATAN STRUKTUR", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (currentTelemetry != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TelemetryMetricCard(
                                        title = "Suhu Kabin",
                                        value = String.format(Locale.US, "%.1f", currentTelemetry.cabinTemp),
                                        unit = "°C",
                                        icon = Icons.Default.Air,
                                        statusColor = if (currentTelemetry.cabinTemp < 30f) StatusGreen else StatusAmber,
                                        modifier = Modifier.weight(1f),
                                        subValue = "Hum: ${currentTelemetry.humidity.toInt()}%"
                                    )
                                    TelemetryMetricCard(
                                        title = "Pintu Shelter",
                                        value = if (currentTelemetry.doorOpen) "TERBUKA" else "TERKUNCI",
                                        unit = "",
                                        icon = if (currentTelemetry.doorOpen) Icons.Default.DoorSliding else Icons.Default.Lock,
                                        statusColor = if (!currentTelemetry.doorOpen) StatusGreen else StatusRed,
                                        modifier = Modifier.weight(1f),
                                        subValue = if (!currentTelemetry.doorOpen) "Aman" else "Akses Ilegal!"
                                    )
                                    TelemetryMetricCard(
                                        title = "Kemiringan Tiang",
                                        value = String.format(Locale.US, "%.1f", currentTelemetry.tiltAngleDegrees),
                                        unit = "°",
                                        icon = Icons.Default.Architecture,
                                        statusColor = if (currentTelemetry.tiltAngleDegrees < 1.0f) StatusGreen else StatusRed,
                                        modifier = Modifier.weight(1f),
                                        subValue = if (currentTelemetry.obstructionLightOk) "Lampu OBL OK" else "OBL Mati!"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTowerDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newTower ->
                viewModel.createTower(newTower)
            }
        )
    }

    if (showFaultDialog && selectedTower != null) {
        FaultInjectionDialog(
            towers = towers,
            selectedTowerId = selectedTower!!.id,
            onDismiss = { showFaultDialog = false },
            onInject = { towerId, anomaly, name ->
                viewModel.triggerFaultSimulation(towerId, anomaly, name)
            }
        )
    }
}
