package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
fun TechnicianTeamScreen(
    viewModel: TowerOpsViewModel,
    modifier: Modifier = Modifier
) {
    val technicians by viewModel.technicians.collectAsState()
    val workOrders by viewModel.workOrders.collectAsState()
    val context = LocalContext.current

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Tim Teknisi, 1 = Tiket SPK Kerja

    val timeFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Manajemen Tim Teknisi", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Personel K3 ketinggian, genset, dan tiket perbaikan", color = TextSecondary, fontSize = 12.sp)
            }
        }

        // Sub-tabs: Roster vs Work Orders
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = DarkSurface,
            contentColor = CyanPrimary,
            divider = { HorizontalDivider(color = DarkBorder) }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Personel Lapangan (${technicians.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            val activeWo = workOrders.count { it.status != "RESOLVED" }
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Tiket SPK Kerja ($activeWo Aktif)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        if (selectedSubTab == 0) {
            // Technicians Roster
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(technicians, key = { it.id }) { tech ->
                    val statusColor = when (tech.status) {
                        "AVAILABLE" -> StatusGreen
                        "EN_ROUTE" -> StatusAmber
                        "ON_SITE" -> CyanPrimary
                        else -> Color.Gray
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tech_card_${tech.id}"),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder),
                        shape = RoundedCornerShape(12.dp)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(DarkSurfaceElevated)
                                            .border(1.5.dp, statusColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Engineering,
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column {
                                        Text(tech.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(tech.specialization, color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    StatusPulseDot(color = statusColor, size = 6)
                                    Text(tech.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Certifications & Rating
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sertifikasi: ${tech.certifications}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(14.dp))
                                    Text("${tech.rating}", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(color = DarkBorder)

                            // Contact Actions (Phone & WhatsApp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "GPS: ${String.format(Locale.US, "%.4f, %.4f", tech.latitude, tech.longitude)}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${tech.phone}"))
                                            context.startActivity(intent)
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = CyanPrimary)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Telepon", fontSize = 11.sp, color = CyanPrimary)
                                    }

                                    Button(
                                        onClick = {
                                            val uri = Uri.parse("https://wa.me/${tech.phone.replace("+", "")}?text=Halo%20${tech.name},%20instruksi%20NOC%20TowerOps:")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Color.White),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Work Orders List
            if (workOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(48.dp))
                        Text("Tidak Ada Tiket Kerja Terbuka", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Semua insiden telah diselesaikan oleh tim teknisi", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(workOrders, key = { it.id }) { wo ->
                        val isResolved = wo.status == "RESOLVED"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("work_order_${wo.id}"),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, if (isResolved) DarkBorder else CyanPrimary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(wo.id, color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        AlertSeverityBadge(wo.priority)
                                    }

                                    Text(
                                        timeFormat.format(Date(wo.createdAt)),
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(wo.issueTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Petunjuk Lapangan: ${wo.instructions}", color = TextSecondary, fontSize = 12.sp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Teknisi: ${wo.technicianName}", color = ElectricBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Status: ${wo.status}", color = if (isResolved) StatusGreen else StatusAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                if (!isResolved) {
                                    HorizontalDivider(color = DarkBorder)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (wo.status == "ASSIGNED" || wo.status == "EN_ROUTE") {
                                            Button(
                                                onClick = { viewModel.updateWorkOrderStatus(wo, "ON_SITE", "Teknisi telah tiba di shelter BTS.") },
                                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("btn_wo_arrive_${wo.id}")
                                            ) {
                                                Text("Konfirmasi Tiba di Site", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (wo.status == "ON_SITE" || wo.status == "IN_PROGRESS") {
                                            Button(
                                                onClick = {
                                                    viewModel.updateWorkOrderStatus(
                                                        wo,
                                                        "RESOLVED",
                                                        "Perbaikan selesai. Komponen diganti & telemetri normal kembali."
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Color.White),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("btn_wo_resolve_${wo.id}")
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Selesaikan & Pulihkan Site", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
