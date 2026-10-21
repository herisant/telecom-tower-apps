package com.example.data.repository

import com.example.data.local.TowerOpsDatabase
import com.example.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

class TowerOpsRepository(
    private val db: TowerOpsDatabase,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    val allTowers: Flow<List<Tower>> = db.towerDao().getAllTowers()
    val allAlerts: Flow<List<FaultAlert>> = db.faultAlertDao().getAllAlerts()
    val activeAlerts: Flow<List<FaultAlert>> = db.faultAlertDao().getActiveAlerts()
    val allTechnicians: Flow<List<Technician>> = db.technicianDao().getAllTechnicians()
    val allWorkOrders: Flow<List<WorkOrder>> = db.workOrderDao().getAllWorkOrders()
    val allReports: Flow<List<AutomatedReport>> = db.automatedReportDao().getAllReports()

    // Live Telemetry memory cache keyed by towerId
    private val _telemetryState = MutableStateFlow<Map<String, TelemetryData>>(emptyMap())
    val telemetryState: StateFlow<Map<String, TelemetryData>> = _telemetryState.asStateFlow()

    // Real-time Event bus for live alert toast notifications
    private val _recentAlertEvent = MutableSharedFlow<FaultAlert>(extraBufferCapacity = 5)
    val recentAlertEvent: SharedFlow<FaultAlert> = _recentAlertEvent.asSharedFlow()

    init {
        // Start background telemetry simulation loop
        externalScope.launch {
            initInitialTelemetry()
            startTelemetrySimulationLoop()
        }
    }

    private suspend fun initInitialTelemetry() {
        val initialMap = mutableMapOf<String, TelemetryData>()
        val defaultTowers = listOf(
            "TWR-JKT-042" to TelemetryData(
                towerId = "TWR-JKT-042",
                gridVoltage = 0f,
                gridOnline = false,
                gensetFuelPercent = 42f,
                gensetRunning = false,
                batterySoC = 31f,
                batteryTemp = 38.5f,
                cabinTemp = 31.2f,
                humidity = 58f,
                doorOpen = false,
                smokeDetected = false,
                waterIntrusion = false,
                rruTemp = 48.2f,
                vswrRatio = 1.25f,
                opticalSfpLossDb = 2.1f,
                bbuLoadPercent = 74f,
                activeCarriers = 12,
                throughputMbps = 845f,
                latencyMs = 18,
                obstructionLightOk = true,
                tiltAngleDegrees = 0.2f
            ),
            "TWR-JKT-108" to TelemetryData(
                towerId = "TWR-JKT-108",
                gridVoltage = 224f,
                gridOnline = true,
                gensetFuelPercent = 90f,
                gensetRunning = false,
                batterySoC = 98f,
                batteryTemp = 27.0f,
                cabinTemp = 23.5f,
                humidity = 52f,
                doorOpen = false,
                smokeDetected = false,
                waterIntrusion = false,
                rruTemp = 42.0f,
                vswrRatio = 1.15f,
                opticalSfpLossDb = 1.2f,
                bbuLoadPercent = 45f,
                activeCarriers = 8,
                throughputMbps = 620f,
                latencyMs = 12,
                obstructionLightOk = true,
                tiltAngleDegrees = 0.1f
            ),
            "TWR-BDG-019" to TelemetryData(
                towerId = "TWR-BDG-019",
                gridVoltage = 218f,
                gridOnline = true,
                gensetFuelPercent = 75f,
                gensetRunning = false,
                batterySoC = 88f,
                batteryTemp = 26.5f,
                cabinTemp = 24.1f,
                humidity = 64f,
                doorOpen = false,
                smokeDetected = false,
                waterIntrusion = false,
                rruTemp = 52.8f,
                vswrRatio = 2.65f, // Anomaly!
                opticalSfpLossDb = 3.8f,
                bbuLoadPercent = 62f,
                activeCarriers = 10,
                throughputMbps = 410f,
                latencyMs = 28,
                obstructionLightOk = true,
                tiltAngleDegrees = 0.4f
            ),
            "TWR-SBY-088" to TelemetryData(
                towerId = "TWR-SBY-088",
                gridVoltage = 221f,
                gridOnline = true,
                gensetFuelPercent = 88f,
                gensetRunning = false,
                batterySoC = 95f,
                batteryTemp = 28.2f,
                cabinTemp = 22.8f,
                humidity = 55f,
                doorOpen = false,
                smokeDetected = false,
                waterIntrusion = false,
                rruTemp = 41.5f,
                vswrRatio = 1.18f,
                opticalSfpLossDb = 1.4f,
                bbuLoadPercent = 53f,
                activeCarriers = 14,
                throughputMbps = 910f,
                latencyMs = 14,
                obstructionLightOk = true,
                tiltAngleDegrees = 0.1f
            ),
            "TWR-DPS-007" to TelemetryData(
                towerId = "TWR-DPS-007",
                gridVoltage = 219f,
                gridOnline = true,
                gensetFuelPercent = 60f,
                gensetRunning = false,
                batterySoC = 84f,
                batteryTemp = 34.0f,
                cabinTemp = 36.8f, // Anomaly!
                humidity = 72f,
                doorOpen = false,
                smokeDetected = false,
                waterIntrusion = false,
                rruTemp = 56.4f,
                vswrRatio = 1.32f,
                opticalSfpLossDb = 2.0f,
                bbuLoadPercent = 68f,
                activeCarriers = 8,
                throughputMbps = 550f,
                latencyMs = 21,
                obstructionLightOk = true,
                tiltAngleDegrees = 0.2f
            ),
            "TWR-MDN-033" to TelemetryData(
                towerId = "TWR-MDN-033",
                gridVoltage = 226f,
                gridOnline = true,
                gensetFuelPercent = 95f,
                gensetRunning = false,
                batterySoC = 99f,
                batteryTemp = 27.5f,
                cabinTemp = 23.2f,
                humidity = 59f,
                doorOpen = false,
                smokeDetected = false,
                waterIntrusion = false,
                rruTemp = 43.1f,
                vswrRatio = 1.12f,
                opticalSfpLossDb = 1.1f,
                bbuLoadPercent = 48f,
                activeCarriers = 16,
                throughputMbps = 1120f,
                latencyMs = 15,
                obstructionLightOk = true,
                tiltAngleDegrees = 0.1f
            ),
            "TWR-SMG-051" to TelemetryData(
                towerId = "TWR-SMG-051",
                gridVoltage = 222f,
                gridOnline = true,
                gensetFuelPercent = 82f,
                gensetRunning = false,
                batterySoC = 91f,
                batteryTemp = 28.0f,
                cabinTemp = 24.5f,
                humidity = 50f,
                doorOpen = false,
                smokeDetected = false,
                waterIntrusion = false,
                rruTemp = 44.0f,
                vswrRatio = 1.20f,
                opticalSfpLossDb = 1.6f,
                bbuLoadPercent = 50f,
                activeCarriers = 10,
                throughputMbps = 680f,
                latencyMs = 17,
                obstructionLightOk = true,
                tiltAngleDegrees = 0.15f
            )
        )
        defaultTowers.forEach { (id, data) -> initialMap[id] = data }
        _telemetryState.value = initialMap
    }

    private suspend fun startTelemetrySimulationLoop() {
        while (currentCoroutineContext().isActive) {
            delay(3500) // update telemetry every 3.5 seconds
            val current = _telemetryState.value.toMutableMap()

            current.keys.forEach { towerId ->
                val prev = current[towerId] ?: return@forEach
                // Small natural jitter
                val jitterVolt = if (prev.gridOnline) (Random.nextFloat() * 4f - 2f) else 0f
                val newVolt = if (prev.gridOnline) (prev.gridVoltage + jitterVolt).coerceIn(205f, 235f) else 0f
                val jitterThroughput = (Random.nextFloat() * 40f - 20f)
                val newThroughput = (prev.throughputMbps + jitterThroughput).coerceIn(120f, 1500f)
                val jitterBbu = (Random.nextFloat() * 4f - 2f)
                val newBbu = (prev.bbuLoadPercent + jitterBbu).coerceIn(20f, 95f)

                // If battery discharging because grid is offline
                val newBattery = if (!prev.gridOnline && !prev.gensetRunning) {
                    (prev.batterySoC - 0.15f).coerceAtLeast(5f)
                } else if (prev.gridOnline && prev.batterySoC < 98f) {
                    (prev.batterySoC + 0.1f).coerceAtMost(100f)
                } else prev.batterySoC

                val newLatency = (prev.latencyMs + Random.nextInt(-2, 3)).coerceIn(8, 90)

                current[towerId] = prev.copy(
                    timestamp = System.currentTimeMillis(),
                    gridVoltage = newVolt,
                    batterySoC = newBattery,
                    bbuLoadPercent = newBbu,
                    throughputMbps = newThroughput,
                    latencyMs = newLatency
                )
            }
            _telemetryState.value = current
        }
    }

    // Trigger Fault Simulation (Demo / QA / Ops test)
    suspend fun triggerFaultInjection(
        towerId: String,
        anomalyType: String,
        towerName: String
    ) {
        val currentTelemetry = _telemetryState.value[towerId]
        val alertId = "ALT-" + System.currentTimeMillis().toString().takeLast(6)
        val alert: FaultAlert

        when (anomalyType) {
            "PLN_BLACKOUT" -> {
                currentTelemetry?.let {
                    _telemetryState.value = _telemetryState.value + (towerId to it.copy(
                        gridOnline = false,
                        gridVoltage = 0f,
                        gensetRunning = false,
                        batterySoC = 34f
                    ))
                }
                alert = FaultAlert(
                    id = alertId,
                    towerId = towerId,
                    towerName = towerName,
                    severity = AlertSeverity.CRITICAL,
                    subsystem = SubsystemType.POWER,
                    title = "PLN Blackout & ATS Genset Failure",
                    description = "Tegangan PLN 0V. ATS gagal transfer ke genset cadangan. Baterai DC bus drop ke 34%.",
                    rootCauseRecommendation = "Kirim teknisi Power untuk manual starter genset dan pemeriksaan kontaktor ATS.",
                    timestamp = System.currentTimeMillis()
                )
                db.towerDao().updateTowerStatus(towerId, TowerStatus.CRITICAL)
            }
            "VSWR_SPIKE" -> {
                currentTelemetry?.let {
                    _telemetryState.value = _telemetryState.value + (towerId to it.copy(
                        vswrRatio = 2.85f,
                        throughputMbps = (it.throughputMbps * 0.45f)
                    ))
                }
                alert = FaultAlert(
                    id = alertId,
                    towerId = towerId,
                    towerName = towerName,
                    severity = AlertSeverity.MAJOR,
                    subsystem = SubsystemType.RADIO_RF,
                    title = "High VSWR Reflection Anomaly (> 2.8:1)",
                    description = "Refleksi sinyal RF abnormal terdeteksi pada port feeder antena sektor utama.",
                    rootCauseRecommendation = "Kemungkinan konektor jumper kemasukan air atau kabel feeder tertekuk/rusak.",
                    timestamp = System.currentTimeMillis()
                )
                db.towerDao().updateTowerStatus(towerId, TowerStatus.WARNING)
            }
            "CABIN_OVERHEAT" -> {
                currentTelemetry?.let {
                    _telemetryState.value = _telemetryState.value + (towerId to it.copy(
                        cabinTemp = 41.5f,
                        rruTemp = 62.0f
                    ))
                }
                alert = FaultAlert(
                    id = alertId,
                    towerId = towerId,
                    towerName = towerName,
                    severity = AlertSeverity.MAJOR,
                    subsystem = SubsystemType.ENVIRONMENT,
                    title = "Shelter High Temperature Warning (41.5°C)",
                    description = "Suhu shelter melebihi batas operasional 30°C. Risiko degradasi umur baterai & throttling BBU.",
                    rootCauseRecommendation = "Cek pendingin AC utama shelter, bersihkan sirip kondensor luar dari debu.",
                    timestamp = System.currentTimeMillis()
                )
                db.towerDao().updateTowerStatus(towerId, TowerStatus.WARNING)
            }
            "DOOR_BREACH" -> {
                currentTelemetry?.let {
                    _telemetryState.value = _telemetryState.value + (towerId to it.copy(
                        doorOpen = true
                    ))
                }
                alert = FaultAlert(
                    id = alertId,
                    towerId = towerId,
                    towerName = towerName,
                    severity = AlertSeverity.MINOR,
                    subsystem = SubsystemType.ENVIRONMENT,
                    title = "Unauthorized Shelter Door Open Sensor",
                    description = "Pintu shelter terbuka di luar jadwal pemeliharaan resmi tanpa otorisasi access control.",
                    rootCauseRecommendation = "Verifikasi CCTV site dan hubungi tim security perimeter setempat.",
                    timestamp = System.currentTimeMillis()
                )
            }
            "TILT_VIBRATION" -> {
                currentTelemetry?.let {
                    _telemetryState.value = _telemetryState.value + (towerId to it.copy(
                        tiltAngleDegrees = 2.4f
                    ))
                }
                alert = FaultAlert(
                    id = alertId,
                    towerId = towerId,
                    towerName = towerName,
                    severity = AlertSeverity.CRITICAL,
                    subsystem = SubsystemType.STRUCTURAL,
                    title = "Mast Structural Tilt & Vibration Alert (> 2.4°)",
                    description = "Sensor kemiringan menara mendeteksi deviasi struktural di atas ambang batas aman.",
                    rootCauseRecommendation = "Periksa ketegangan kawat guyed-wire atau pondasi baut angkur menara segera.",
                    timestamp = System.currentTimeMillis()
                )
                db.towerDao().updateTowerStatus(towerId, TowerStatus.CRITICAL)
            }
            else -> {
                alert = FaultAlert(
                    id = alertId,
                    towerId = towerId,
                    towerName = towerName,
                    severity = AlertSeverity.INFO,
                    subsystem = SubsystemType.TRANSMISSION,
                    title = "Periodic Diagnostic Health Ping",
                    description = "Pemeriksaan kesehatan rutin otomatis berhasil.",
                    rootCauseRecommendation = "Tidak ada tindakan yang diperlukan.",
                    timestamp = System.currentTimeMillis()
                )
            }
        }

        db.faultAlertDao().insertAlert(alert)
        _recentAlertEvent.emit(alert)
    }

    // Dispatch Technician to resolve an alert
    suspend fun dispatchTechnician(
        alertId: String,
        towerId: String,
        towerName: String,
        technicianId: String,
        instructions: String,
        priority: AlertSeverity
    ) {
        val tech = db.technicianDao().getTechnicianById(technicianId) ?: return
        val woId = "WO-" + System.currentTimeMillis().toString().takeLast(6)

        val workOrder = WorkOrder(
            id = woId,
            faultAlertId = alertId,
            towerId = towerId,
            towerName = towerName,
            technicianId = technicianId,
            technicianName = tech.name,
            priority = priority,
            status = "EN_ROUTE",
            issueTitle = "Penanganan Insiden $alertId pada $towerName",
            instructions = instructions,
            createdAt = System.currentTimeMillis()
        )

        db.workOrderDao().insertWorkOrder(workOrder)
        db.faultAlertDao().assignTechnicianToAlert(alertId, tech.id, tech.name)
        db.technicianDao().updateTechnicianStatus(technicianId, "EN_ROUTE", woId)
        db.towerDao().updateTowerStatus(towerId, TowerStatus.MAINTENANCE)
    }

    // Update Work Order Progress
    suspend fun updateWorkOrderStatus(
        workOrder: WorkOrder,
        newStatus: String,
        repairNotes: String = "",
        partsReplaced: String = ""
    ) {
        if (newStatus == "RESOLVED") {
            db.workOrderDao().resolveWorkOrder(
                woId = workOrder.id,
                status = "RESOLVED",
                resolvedAt = System.currentTimeMillis(),
                notes = repairNotes
            )
            // Resolve the underlying fault alert
            db.faultAlertDao().resolveAlert(workOrder.faultAlertId)

            // Release technician
            db.technicianDao().updateTechnicianStatus(workOrder.technicianId, "AVAILABLE", null)

            // Restore tower status
            db.towerDao().updateTowerStatus(workOrder.towerId, TowerStatus.NORMAL)

            // Normalize telemetry in memory
            val current = _telemetryState.value[workOrder.towerId]
            if (current != null) {
                _telemetryState.value = _telemetryState.value + (workOrder.towerId to current.copy(
                    gridOnline = true,
                    gridVoltage = 222f,
                    vswrRatio = 1.15f,
                    cabinTemp = 24.0f,
                    doorOpen = false,
                    smokeDetected = false,
                    waterIntrusion = false,
                    tiltAngleDegrees = 0.1f
                ))
            }
        } else {
            val updated = workOrder.copy(
                status = newStatus,
                repairNotes = repairNotes.ifEmpty { workOrder.repairNotes },
                partsReplaced = partsReplaced.ifEmpty { workOrder.partsReplaced }
            )
            db.workOrderDao().updateWorkOrder(updated)
            db.technicianDao().updateTechnicianStatus(workOrder.technicianId, newStatus, workOrder.id)
        }
    }

    // Generate Official Automated Report
    suspend fun generateAutomatedReport(period: String): AutomatedReport {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm 'WIB'", Locale.getDefault())
        val generatedDate = dateFormat.format(Date())
        val reportId = "REP-" + System.currentTimeMillis().toString().takeLast(6)

        // Read snapshot of data
        val alertsSnapshot = db.faultAlertDao().getAllAlerts().first()
        val totalIncidents = alertsSnapshot.size
        val resolvedIncidents = alertsSnapshot.count { it.isResolved }
        val criticalAlerts = alertsSnapshot.count { it.severity == AlertSeverity.CRITICAL }

        val sla = if (criticalAlerts == 0) 99.98f else (99.98f - (criticalAlerts * 0.08f)).coerceAtLeast(98.5f)
        val mttr = if (resolvedIncidents > 0) Random.nextInt(35, 58) else 45
        val fuelEst = 180f + (criticalAlerts * 45f)

        val periodTitle = when (period) {
            "Harian" -> "Laporan Harian Operasional & Kesehatan Tower ($generatedDate)"
            "Mingguan" -> "Laporan Mingguan SLA & Ketersediaan BTS ($generatedDate)"
            else -> "Laporan Bulanan Komprehensif Infrastruktur Telekomunikasi ($generatedDate)"
        }

        val summary = """
            Sistem Pemantauan Menara Telekomunikasi Otomatis melaporkan status operasional menyeluruh:
            - Ketersediaan Layanan (SLA): ${String.format(Locale.US, "%.2f", sla)}%
            - Total Insiden Terdeteksi: $totalIncidents kejadian ($resolvedIncidents terselesaikan)
            - Rata-rata Waktu Perbaikan (MTTR): $mttr Menit
            - Total Estimasi Konsumsi BBM Genset: ${fuelEst.roundToInt()} Liter
            - Status Alokasi Teknisi: Tim siaga lapangan berhasil memitigasi anomali prioritas tinggi sesuai SOP K3 dan regulasi Kominfo.
        """.trimIndent()

        val report = AutomatedReport(
            id = reportId,
            title = periodTitle,
            period = period,
            generatedDate = generatedDate,
            totalTowersMonitored = 7,
            overallSlaUptimePercent = sla,
            totalIncidents = totalIncidents,
            resolvedIncidents = resolvedIncidents,
            avgMttrMinutes = mttr,
            fuelConsumedLiters = fuelEst,
            criticalAlertCount = criticalAlerts,
            summaryText = summary
        )

        db.automatedReportDao().insertReport(report)
        return report
    }

    // Add New Custom Tower
    suspend fun addNewTower(tower: Tower) {
        db.towerDao().insertTowers(listOf(tower))
        // Init default telemetry
        val defaultTelem = TelemetryData(
            towerId = tower.id,
            gridVoltage = 220f,
            gridOnline = true,
            gensetFuelPercent = 90f,
            gensetRunning = false,
            batterySoC = 95f,
            batteryTemp = 28f,
            cabinTemp = 24f,
            humidity = 55f,
            doorOpen = false,
            smokeDetected = false,
            waterIntrusion = false,
            rruTemp = 42f,
            vswrRatio = 1.15f,
            opticalSfpLossDb = 1.2f,
            bbuLoadPercent = 45f,
            activeCarriers = 8,
            throughputMbps = 650f,
            latencyMs = 15,
            obstructionLightOk = true,
            tiltAngleDegrees = 0.1f
        )
        _telemetryState.value = _telemetryState.value + (tower.id to defaultTelem)
    }
}
