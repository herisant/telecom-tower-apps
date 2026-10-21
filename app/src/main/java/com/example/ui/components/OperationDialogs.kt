package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatchDialog(
    alert: FaultAlert,
    tower: Tower?,
    availableTechnicians: List<Technician>,
    onDismiss: () -> Unit,
    onDispatch: (technicianId: String, instructions: String, priority: AlertSeverity) -> Unit
) {
    var selectedTechId by remember {
        mutableStateOf(availableTechnicians.firstOrNull { it.status == "AVAILABLE" }?.id ?: availableTechnicians.firstOrNull()?.id ?: "")
    }
    var instructions by remember {
        mutableStateOf("Periksa sumber anomali: ${alert.title}. ${alert.rootCauseRecommendation}")
    }
    var selectedPriority by remember { mutableStateOf(alert.severity) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = CyanPrimary)
                Text("Disposisi Tim Teknisi", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Target Site & Alert summary card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Target Lokasi Insiden:", color = TextSecondary, fontSize = 11.sp)
                        Text(text = alert.towerName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Masalah: ${alert.title}", color = StatusAmber, fontSize = 12.sp)
                    }
                }

                Text("Pilih Teknisi Lapangan:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                availableTechnicians.forEach { tech ->
                    val isSelected = tech.id == selectedTechId
                    val distKm = if (tower != null) {
                        calculateDistanceKm(tower.latitude, tower.longitude, tech.latitude, tech.longitude)
                    } else 0.0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTechId = tech.id },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else DarkSurfaceElevated
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) CyanPrimary else DarkBorder
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tech.name,
                                    color = if (isSelected) CyanPrimary else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${tech.specialization} • Jarak: ${String.format("%.1f", distKm)} km",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Status: ${tech.status}",
                                    color = if (tech.status == "AVAILABLE") StatusGreen else StatusAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedTechId = tech.id }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Petunjuk / SOP Penanganan") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = DarkBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTechId.isNotEmpty()) {
                        onDispatch(selectedTechId, instructions, selectedPriority)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                modifier = Modifier.testTag("confirm_dispatch_btn")
            ) {
                Text("Kirim Penugasan SPK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun FaultInjectionDialog(
    towers: List<Tower>,
    selectedTowerId: String,
    onDismiss: () -> Unit,
    onInject: (towerId: String, anomalyType: String, towerName: String) -> Unit
) {
    var targetId by remember { mutableStateOf(selectedTowerId) }
    val anomalies = listOf(
        Pair("PLN_BLACKOUT", "Pemadaman PLN & Kegagalan ATS Genset (Kritis)"),
        Pair("VSWR_SPIKE", "Refleksi VSWR Antena Tinggi > 2.8:1 (Mayor)"),
        Pair("CABIN_OVERHEAT", "Suhu Shelter Overheat > 41°C (Mayor)"),
        Pair("DOOR_BREACH", "Sensor Pembobolan Pintu Shelter (Minor)"),
        Pair("TILT_VIBRATION", "Sensor Getaran & Kemiringan Tiang Menara (Kritis)")
    )
    var selectedAnomaly by remember { mutableStateOf("PLN_BLACKOUT") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = StatusAmber)
                Text("Simulasi Anomali Real-Time", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pilih Menara Target:", color = TextSecondary, fontSize = 12.sp)
                towers.take(6).forEach { tower ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { targetId = tower.id }
                            .background(if (tower.id == targetId) CyanPrimary.copy(alpha = 0.15f) else Color.Transparent)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = tower.id == targetId, onClick = { targetId = tower.id })
                        Text(tower.name, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
                    }
                }

                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 4.dp))
                Text("Jenis Kerusakan / Anomali Sensor:", color = TextSecondary, fontSize = 12.sp)

                anomalies.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedAnomaly = key }
                            .background(if (selectedAnomaly == key) StatusAmber.copy(alpha = 0.15f) else Color.Transparent)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedAnomaly == key, onClick = { selectedAnomaly = key })
                        Text(label, color = TextPrimary, fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = towers.find { it.id == targetId }?.name ?: "Tower BTS"
                    onInject(targetId, selectedAnomaly, name)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                modifier = Modifier.testTag("inject_fault_btn")
            ) {
                Text("Mulai Pemicuan Anomali", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = TextSecondary) }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun AddTowerDialog(
    onDismiss: () -> Unit,
    onAdd: (Tower) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var siteCode by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Jakarta") }
    var region by remember { mutableStateOf("Jabodetabek") }
    var latStr by remember { mutableStateOf("-6.200000") }
    var lngStr by remember { mutableStateOf("106.816666") }
    var heightStr by remember { mutableStateOf("52") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrasi Menara BTS Baru", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Menara / Site Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = siteCode,
                    onValueChange = { siteCode = it },
                    label = { Text("Kode Site (misal: JKT-199-PLZ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Kota / Wilayah") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latStr,
                        onValueChange = { latStr = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lngStr,
                        onValueChange = { lngStr = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it },
                    label = { Text("Tinggi Menara (Meter)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && siteCode.isNotEmpty()) {
                        val tower = Tower(
                            id = "TWR-" + System.currentTimeMillis().toString().takeLast(6),
                            name = name,
                            siteCode = siteCode,
                            region = region,
                            city = city,
                            address = "$city, Indonesia",
                            latitude = latStr.toDoubleOrNull() ?: -6.2,
                            longitude = lngStr.toDoubleOrNull() ?: 106.8,
                            towerType = TowerType.SST_4_LEG,
                            heightMeters = heightStr.toIntOrNull() ?: 50,
                            operatorShared = "Telkomsel, Indosat, XL",
                            status = TowerStatus.NORMAL,
                            lastInspected = "2026-10-21",
                            powerSource = "PLN 33 kVA + Genset 40 kVA"
                        )
                        onAdd(tower)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black)
            ) {
                Text("Simpan Site Menara", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextSecondary) }
        },
        containerColor = DarkSurface
    )
}

fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}
