package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TowerStatus {
    NORMAL,
    WARNING,
    CRITICAL,
    MAINTENANCE
}

enum class TowerType {
    SST_4_LEG, // Self Supporting Tower 4 Kaki
    MONOPOLE,  // Tiang Tunggal
    GUYED_MAST // Menara Kawat Tarik
}

enum class SubsystemType {
    POWER,        // PLN, Genset, Baterai
    RADIO_RF,     // RRU, VSWR, Antena Sektoral
    ENVIRONMENT,  // Suhu kabin, Kelembaban, Banjir, Pintu
    TRANSMISSION, // Fiber Optic, Microwave SFP
    STRUCTURAL    // Lampu suar (OBL), Getaran, Kemiringan
}

enum class AlertSeverity {
    CRITICAL,
    MAJOR,
    MINOR,
    INFO
}

@Entity(tableName = "towers")
data class Tower(
    @PrimaryKey val id: String, // e.g. TWR-JKT-042
    val name: String,
    val siteCode: String,
    val region: String,
    val city: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val towerType: TowerType,
    val heightMeters: Int,
    val operatorShared: String, // "Telkomsel, Indosat, XL, Smartfren"
    val status: TowerStatus,
    val lastInspected: String,
    val powerSource: String // "PLN 16.5 kVA + Genset 20 kVA + Lithium-Ion Backup"
)

data class TelemetryData(
    val towerId: String,
    val timestamp: Long = System.currentTimeMillis(),
    // Power subsystem
    val gridVoltage: Float,      // 220V nominal (180-240V normal)
    val gridOnline: Boolean,
    val gensetFuelPercent: Float, // 0 - 100%
    val gensetRunning: Boolean,
    val batterySoC: Float,       // 0 - 100%
    val batteryTemp: Float,      // Celcius
    // Environment subsystem
    val cabinTemp: Float,        // Celcius (normal 20-27C)
    val humidity: Float,         // % (normal 40-70%)
    val doorOpen: Boolean,
    val smokeDetected: Boolean,
    val waterIntrusion: Boolean,
    // Radio & RF Subsystem
    val rruTemp: Float,          // Celcius (normal 35-55C)
    val vswrRatio: Float,        // Ratio 1.1 - 1.5 normal (>1.8 fault)
    val opticalSfpLossDb: Float, // Optical link attenuation
    val bbuLoadPercent: Float,   // CPU Load %
    val activeCarriers: Int,     // 4G/5G Bands
    val throughputMbps: Float,   // Live Traffic
    val latencyMs: Int,          // Ping to Gateway
    // Structural / Safety
    val obstructionLightOk: Boolean,
    val tiltAngleDegrees: Float  // < 1.0 deg normal
)

@Entity(tableName = "fault_alerts")
data class FaultAlert(
    @PrimaryKey val id: String,
    val towerId: String,
    val towerName: String,
    val severity: AlertSeverity,
    val subsystem: SubsystemType,
    val title: String,
    val description: String,
    val rootCauseRecommendation: String,
    val timestamp: Long,
    val isResolved: Boolean = false,
    val resolvedAt: Long? = null,
    val assignedTechnicianId: String? = null,
    val assignedTechnicianName: String? = null
)

@Entity(tableName = "technicians")
data class Technician(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val specialization: String,
    val status: String, // "AVAILABLE", "ON_DUTY", "EN_ROUTE", "ON_SITE", "OFF_DUTY"
    val latitude: Double,
    val longitude: Double,
    val rating: Float,
    val certifications: String,
    val activeWorkOrderId: String? = null
)

@Entity(tableName = "work_orders")
data class WorkOrder(
    @PrimaryKey val id: String, // e.g. WO-2026-1082
    val faultAlertId: String,
    val towerId: String,
    val towerName: String,
    val technicianId: String,
    val technicianName: String,
    val priority: AlertSeverity,
    val status: String, // "ASSIGNED", "EN_ROUTE", "IN_PROGRESS", "RESOLVED"
    val issueTitle: String,
    val instructions: String,
    val createdAt: Long,
    val resolvedAt: Long? = null,
    val repairNotes: String = "",
    val partsReplaced: String = ""
)

@Entity(tableName = "automated_reports")
data class AutomatedReport(
    @PrimaryKey val id: String,
    val title: String,
    val period: String, // "Harian", "Mingguan", "Bulanan"
    val generatedDate: String,
    val totalTowersMonitored: Int,
    val overallSlaUptimePercent: Float,
    val totalIncidents: Int,
    val resolvedIncidents: Int,
    val avgMttrMinutes: Int,
    val fuelConsumedLiters: Float,
    val criticalAlertCount: Int,
    val summaryText: String
)
