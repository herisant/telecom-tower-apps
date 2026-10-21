package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun StatusPulseDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Int = 10,
    pulse: Boolean = true
) {
    if (pulse) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun TowerStatusBadge(status: TowerStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        TowerStatus.NORMAL -> Triple(StatusGreen.copy(alpha = 0.2f), StatusGreen, "NORMAL")
        TowerStatus.WARNING -> Triple(StatusAmber.copy(alpha = 0.2f), StatusAmber, "WARNING")
        TowerStatus.CRITICAL -> Triple(StatusRed.copy(alpha = 0.25f), StatusRed, "CRITICAL")
        TowerStatus.MAINTENANCE -> Triple(StatusBlue.copy(alpha = 0.2f), StatusBlue, "MAINTENANCE")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StatusPulseDot(color = textColor, size = 7, pulse = status == TowerStatus.CRITICAL)
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AlertSeverityBadge(severity: AlertSeverity, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label, icon) = when (severity) {
        AlertSeverity.CRITICAL -> Quad(StatusRed.copy(alpha = 0.25f), StatusRed, "CRITICAL", Icons.Default.Warning)
        AlertSeverity.MAJOR -> Quad(StatusAmber.copy(alpha = 0.2f), StatusAmber, "MAJOR", Icons.Default.PriorityHigh)
        AlertSeverity.MINOR -> Quad(StatusBlue.copy(alpha = 0.2f), StatusBlue, "MINOR", Icons.Default.Info)
        AlertSeverity.INFO -> Quad(Color.Gray.copy(alpha = 0.2f), Color.LightGray, "INFO", Icons.Default.Notifications)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun SubsystemBadge(subsystem: SubsystemType, modifier: Modifier = Modifier) {
    val (label, icon) = when (subsystem) {
        SubsystemType.POWER -> Pair("POWER / GENSET", Icons.Default.Bolt)
        SubsystemType.RADIO_RF -> Pair("RADIO / RRU", Icons.Default.CellTower)
        SubsystemType.ENVIRONMENT -> Pair("SHELTER / IOT", Icons.Default.Thermostat)
        SubsystemType.TRANSMISSION -> Pair("TRANSMISSION", Icons.Default.Router)
        SubsystemType.STRUCTURAL -> Pair("TOWER MAST", Icons.Default.Apartment)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyanPrimary,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TelemetryMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    statusColor: Color = CyanPrimary,
    modifier: Modifier = Modifier,
    subValue: String? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 17.sp
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            if (subValue != null) {
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun MetricProgressBar(
    label: String,
    valueString: String,
    progress: Float, // 0.0 to 1.0
    color: Color = CyanPrimary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
            Text(text = valueString, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = DarkSurface
        )
    }
}
