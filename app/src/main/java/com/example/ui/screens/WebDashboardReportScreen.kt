package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
fun WebDashboardReportScreen(
    viewModel: TowerOpsViewModel,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.reports.collectAsState()
    val towers by viewModel.towers.collectAsState()
    val allAlerts by viewModel.allAlerts.collectAsState()
    val technicians by viewModel.technicians.collectAsState()
    val context = LocalContext.current

    var selectedPeriod by remember { mutableStateOf("Harian") }
    var selectedReportForPreview by remember { mutableStateOf<AutomatedReport?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val activeReport = selectedReportForPreview ?: reports.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Web Browser-like Address Bar & NOC Portal Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Browser URL bar simulation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(14.dp))
                    Text(
                        "https://noc-portal.towerops.telecom.id/analytics/executive-report",
                        color = CyanPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFEF4444)))
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF59E0B)))
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF10B981)))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Web NOC Management Portal", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            StatusPulseDot(color = StatusGreen, size = 6)
                        }
                        Text("Dasbor Pelaporan Otomatis & Kinerja Menara Nasional", color = TextSecondary, fontSize = 11.sp)
                    }

                    // Generator Action Button
                    Button(
                        onClick = {
                            isGenerating = true
                            viewModel.generateReport(selectedPeriod) { newReport ->
                                isGenerating = false
                                selectedReportForPreview = newReport
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_generate_auto_report")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.Black)
                            Spacer(Modifier.width(6.dp))
                        } else {
                            Icon(Icons.Default.AutoGraph, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text("Buat Laporan Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Period Selection Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Harian", "Mingguan", "Bulanan").forEach { period ->
                val isSelected = period == selectedPeriod
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedPeriod = period },
                    label = { Text("Laporan $period", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = CyanPrimary,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Main Report Preview Card (Official Letterhead Format)
        if (activeReport != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Report Header & Kop Surat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "KEMENTERIAN KOMUNIKASI DAN DIGITAL RI",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "DIREKTORAT JENDERAL INFRASTRUKTUR TELEKOMUNIKASI",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Share / Export Button
                        Button(
                            onClick = {
                                val shareText = """
                                    ===== ${activeReport.title} =====
                                    Tanggal Terbit: ${activeReport.generatedDate}
                                    SLA Uptime: ${String.format(Locale.US, "%.2f", activeReport.overallSlaUptimePercent)}%
                                    Total Menara Dipantau: ${activeReport.totalTowersMonitored} Site
                                    Insiden: ${activeReport.totalIncidents} (Terselesaikan: ${activeReport.resolvedIncidents})
                                    Rata-rata MTTR: ${activeReport.avgMttrMinutes} Menit
                                    Konsumsi BBM Genset: ${activeReport.fuelConsumedLiters} Liter
                                    
                                    Ringkasan Eksekutif:
                                    ${activeReport.summaryText}
                                    
                                    Diverifikasi oleh: NOC Telecommunications Automated Monitoring System
                                """.trimIndent()

                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Ekspor Laporan Kinerja Menara"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_export_report")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Ekspor / Bagikan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = DarkBorder, thickness = 1.5.dp)

                    // Title & Metadata
                    Text(
                        activeReport.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "No. Dokumen: ${activeReport.id} • Dibuat: ${activeReport.generatedDate}",
                        color = CyanPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    // Summary KPI Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("SLA UPTIME", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${String.format(Locale.US, "%.2f", activeReport.overallSlaUptimePercent)}%",
                                    color = if (activeReport.overallSlaUptimePercent >= 99.5f) StatusGreen else StatusAmber,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text("Standar Kominfo: 99.9%", color = TextMuted, fontSize = 8.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("MTTR RESPONS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("${activeReport.avgMttrMinutes} Min", color = CyanPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Target: < 60 Min", color = TextMuted, fontSize = 8.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("TOTAL INSIDEN", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("${activeReport.totalIncidents}", color = StatusAmber, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                Text("${activeReport.resolvedIncidents} Resolved", color = StatusGreen, fontSize = 8.sp)
                            }
                        }
                    }

                    // Executive Summary Text
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("RINGKASAN EKSEKUTIF OPERASIONAL:", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(activeReport.summaryText, color = TextPrimary, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }

                    // Audit Matrix of Monitored Sites
                    Text("STATUS AUDIT SITE BTS:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        towers.forEach { tower ->
                            val siteAlert = allAlerts.find { it.towerId == tower.id && !it.isResolved }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${tower.name} (${tower.siteCode})", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${tower.city} • ${tower.operatorShared}", color = TextSecondary, fontSize = 10.sp)
                                }
                                TowerStatusBadge(tower.status)
                            }
                        }
                    }

                    // Digital Verification Signature Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(28.dp))
                        Column {
                            Text("DIVERIFIKASI SECARA ELEKTRONIK", color = CyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Sistem Pemantauan Menara Telekomunikasi Nasional (TowerOps Engine v2.6)", color = TextSecondary, fontSize = 9.sp)
                            Text("Token Verifikasi: NOC-SHA256-99A82D • PIC: Tim Ahli Telekomunikasi", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
