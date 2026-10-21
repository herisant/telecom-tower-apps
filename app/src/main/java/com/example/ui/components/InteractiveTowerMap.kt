package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.hypot

@OptIn(ExperimentalTextApi::class)
@Composable
fun InteractiveTowerMap(
    towers: List<Tower>,
    technicians: List<Technician>,
    activeAlerts: List<FaultAlert>,
    selectedTowerId: String?,
    onTowerSelected: (Tower) -> Unit,
    modifier: Modifier = Modifier,
    showCoverageRings: Boolean = true,
    showTechnicians: Boolean = true
) {
    // Zoom & Pan state
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Pulse animation for critical alerts & radar sweep
    val infiniteTransition = rememberInfiniteTransition(label = "mapRadar")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    // Text measurer for labels
    val textMeasurer = rememberTextMeasurer()

    // Bounding box of coordinates in Indonesia
    // Lat: ~ 4.0 to -9.0, Lng: ~ 98.0 to 116.0
    val minLat = -9.0
    val maxLat = 4.0
    val minLng = 97.5
    val maxLng = 116.5

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF070B14))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("interactive_map_canvas")
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.7f, 4.5f)
                        offset += pan
                    }
                }
                .pointerInput(towers, scale, offset) {
                    detectTapGestures { tapOffset ->
                        // Hit-test towers
                        val canvasW = size.width
                        val canvasH = size.height

                        var clickedTower: Tower? = null
                        var minDistance = Float.MAX_VALUE

                        towers.forEach { tower ->
                            val normX = ((tower.longitude - minLng) / (maxLng - minLng)).toFloat()
                            val normY = ((maxLat - tower.latitude) / (maxLat - minLat)).toFloat()

                            val screenX = (normX * canvasW) * scale + offset.x
                            val screenY = (normY * canvasH) * scale + offset.y

                            val dist = hypot(tapOffset.x - screenX, tapOffset.y - screenY)
                            if (dist < 45f && dist < minDistance) {
                                minDistance = dist
                                clickedTower = tower
                            }
                        }

                        clickedTower?.let { onTowerSelected(it) }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw GIS coordinate grid lines
            val gridStep = 60f * scale
            val gridColor = Color(0xFF1E293B).copy(alpha = 0.5f)
            var gx = (offset.x % gridStep)
            while (gx < canvasW) {
                drawLine(
                    color = gridColor,
                    start = Offset(gx, 0f),
                    end = Offset(gx, canvasH),
                    strokeWidth = 1f
                )
                gx += gridStep
            }
            var gy = (offset.y % gridStep)
            while (gy < canvasH) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, gy),
                    end = Offset(canvasW, gy),
                    strokeWidth = 1f
                )
                gy += gridStep
            }

            // Map projection helper
            fun project(lat: Double, lng: Double): Offset {
                val normX = ((lng - minLng) / (maxLng - minLng)).toFloat()
                val normY = ((maxLat - lat) / (maxLat - minLat)).toFloat()
                val sx = (normX * canvasW) * scale + offset.x
                val sy = (normY * canvasH) * scale + offset.y
                return Offset(sx, sy)
            }

            // 2. Draw Network Backbone transmission links between major BTS towers
            for (i in 0 until towers.size) {
                for (j in i + 1 until towers.size) {
                    val t1 = towers[i]
                    val t2 = towers[j]
                    // Link towers in the same region or backbone
                    val isBackbone = (t1.region == t2.region) || (t1.id.contains("JKT") && t2.id.contains("BDG"))
                    if (isBackbone) {
                        val p1 = project(t1.latitude, t1.longitude)
                        val p2 = project(t2.latitude, t2.longitude)
                        drawLine(
                            color = CyanPrimary.copy(alpha = 0.18f),
                            start = p1,
                            end = p2,
                            strokeWidth = 1.5f * scale.coerceAtMost(2f),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    }
                }
            }

            // 3. Draw Emergency Dispatch Vectors (Technician -> Incident Tower)
            activeAlerts.filter { !it.isResolved && it.assignedTechnicianId != null }.forEach { alert ->
                val tech = technicians.find { it.id == alert.assignedTechnicianId }
                val targetTower = towers.find { it.id == alert.towerId }
                if (tech != null && targetTower != null) {
                    val pTech = project(tech.latitude, tech.longitude)
                    val pTower = project(targetTower.latitude, targetTower.longitude)

                    // Draw glowing emergency response line
                    drawLine(
                        color = StatusAmber.copy(alpha = 0.8f),
                        start = pTech,
                        end = pTower,
                        strokeWidth = 3f * scale.coerceAtMost(2f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), radarPulse * 24f)
                    )
                }
            }

            // 4. Draw Towers
            towers.forEach { tower ->
                val pt = project(tower.latitude, tower.longitude)
                val isSelected = tower.id == selectedTowerId
                val hasCritical = activeAlerts.any { it.towerId == tower.id && it.severity == AlertSeverity.CRITICAL && !it.isResolved }

                val pinColor = when {
                    hasCritical || tower.status == TowerStatus.CRITICAL -> StatusRed
                    tower.status == TowerStatus.WARNING -> StatusAmber
                    tower.status == TowerStatus.MAINTENANCE -> StatusBlue
                    else -> StatusGreen
                }

                // Coverage radius heat rings
                if (showCoverageRings) {
                    val radiusBase = (if (tower.towerType == TowerType.SST_4_LEG) 38f else 26f) * scale
                    drawCircle(
                        color = pinColor.copy(alpha = 0.08f),
                        radius = radiusBase,
                        center = pt
                    )
                    drawCircle(
                        color = pinColor.copy(alpha = 0.25f),
                        radius = radiusBase,
                        center = pt,
                        style = Stroke(width = 1f)
                    )
                }

                // Pulse wave for critical or active alerts
                if (hasCritical || isSelected) {
                    val pulseRadius = (16f + (radarPulse * 28f)) * scale
                    drawCircle(
                        color = pinColor.copy(alpha = (1f - radarPulse) * 0.7f),
                        radius = pulseRadius,
                        center = pt,
                        style = Stroke(width = 2f)
                    )
                }

                // Outer halo
                drawCircle(
                    color = pinColor.copy(alpha = 0.35f),
                    radius = (if (isSelected) 14f else 9f) * scale,
                    center = pt
                )

                // Tower Pin Core
                drawCircle(
                    color = pinColor,
                    radius = (if (isSelected) 7f else 5.5f) * scale,
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5f * scale,
                    center = pt
                )

                // Tower label
                if (scale >= 1.0f || isSelected) {
                    val labelText = tower.siteCode
                    val textLayout = textMeasurer.measure(
                        text = AnnotatedString(labelText),
                        style = TextStyle(
                            color = if (isSelected) CyanPrimary else TextPrimary,
                            fontSize = (9 * scale.coerceIn(0.9f, 1.4f)).sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(pt.x - (textLayout.size.width / 2f), pt.y + (10f * scale))
                    )
                }
            }

            // 5. Draw Technicians
            if (showTechnicians) {
                technicians.forEach { tech ->
                    val pt = project(tech.latitude, tech.longitude)
                    val techColor = when (tech.status) {
                        "AVAILABLE" -> StatusGreen
                        "EN_ROUTE" -> StatusAmber
                        "ON_SITE" -> CyanPrimary
                        else -> Color.Gray
                    }

                    // Tech Icon Diamond Marker
                    val diamondSize = 6f * scale
                    val path = Path().apply {
                        moveTo(pt.x, pt.y - diamondSize)
                        lineTo(pt.x + diamondSize, pt.y)
                        lineTo(pt.x, pt.y + diamondSize)
                        lineTo(pt.x - diamondSize, pt.y)
                        close()
                    }
                    drawPath(path, color = techColor)
                    drawCircle(color = Color.Black, radius = 2f * scale, center = pt)

                    if (scale >= 1.1f) {
                        val techLabel = textMeasurer.measure(
                            text = AnnotatedString(tech.name.split(" ").first() + " [${tech.status}]"),
                            style = TextStyle(
                                color = TextSecondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                        drawText(
                            textLayoutResult = techLabel,
                            topLeft = Offset(pt.x - (techLabel.size.width / 2f), pt.y - (14f * scale))
                        )
                    }
                }
            }
        }

        // Overlay Map Controls (Zoom In, Zoom Out, Reset, Legend)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FloatingActionButton(
                onClick = { scale = (scale * 1.3f).coerceAtMost(4.5f) },
                modifier = Modifier.size(36.dp),
                containerColor = DarkSurfaceElevated,
                contentColor = CyanPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
            }
            FloatingActionButton(
                onClick = { scale = (scale / 1.3f).coerceAtLeast(0.7f) },
                modifier = Modifier.size(36.dp),
                containerColor = DarkSurfaceElevated,
                contentColor = CyanPrimary
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
            }
            FloatingActionButton(
                onClick = {
                    scale = 1.0f
                    offset = Offset.Zero
                },
                modifier = Modifier.size(36.dp),
                containerColor = DarkSurfaceElevated,
                contentColor = TextSecondary
            ) {
                Icon(imageVector = Icons.Default.FilterCenterFocus, contentDescription = "Reset Center", modifier = Modifier.size(18.dp))
            }
        }

        // Legend overlay at bottom-left
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface.copy(alpha = 0.85f))
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(StatusGreen))
                Text("Normal", fontSize = 9.sp, color = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(StatusAmber))
                Text("Warning", fontSize = 9.sp, color = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(StatusRed))
                Text("Critical", fontSize = 9.sp, color = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.dp)).background(CyanPrimary))
                Text("Teknisi", fontSize = 9.sp, color = TextSecondary)
            }
        }
    }
}
