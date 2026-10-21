package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TowerOpsDatabase
import com.example.data.model.*
import com.example.data.repository.TowerOpsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    DASHBOARD("NOC Dashboard"),
    TOWERS("Menara & IoT"),
    ALERTS("Deteksi Kerusakan"),
    MAP("Peta Interaktif"),
    TECHNICIANS("Tim Teknisi"),
    REPORTS("Dasbor Web & Laporan")
}

class TowerOpsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TowerOpsDatabase.getDatabase(application, viewModelScope)
    val repository = TowerOpsRepository(database, viewModelScope)

    val currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val selectedTowerId = MutableStateFlow<String?>("TWR-JKT-042")
    val isWebDashboardMode = MutableStateFlow(false)

    val towers: StateFlow<List<Tower>> = repository.allTowers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlerts: StateFlow<List<FaultAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAlerts: StateFlow<List<FaultAlert>> = repository.activeAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val technicians: StateFlow<List<Technician>> = repository.allTechnicians
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workOrders: StateFlow<List<WorkOrder>> = repository.allWorkOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<AutomatedReport>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val telemetryMap: StateFlow<Map<String, TelemetryData>> = repository.telemetryState

    val recentAlertEvent: SharedFlow<FaultAlert> = repository.recentAlertEvent

    // Filter states
    val towerFilterRegion = MutableStateFlow("Semua")
    val alertFilterSeverity = MutableStateFlow<AlertSeverity?>(null)

    // Derived states
    val filteredTowers: StateFlow<List<Tower>> = combine(towers, towerFilterRegion) { list, region ->
        if (region == "Semua") list else list.filter { it.region == region }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedTower: StateFlow<Tower?> = combine(towers, selectedTowerId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectTab(tab: AppTab) {
        currentTab.value = tab
    }

    fun selectTower(towerId: String) {
        selectedTowerId.value = towerId
    }

    fun toggleWebDashboardMode() {
        isWebDashboardMode.value = !isWebDashboardMode.value
    }

    fun triggerFaultSimulation(towerId: String, anomalyType: String, towerName: String) {
        viewModelScope.launch {
            repository.triggerFaultInjection(towerId, anomalyType, towerName)
        }
    }

    fun dispatchTechnician(
        alertId: String,
        towerId: String,
        towerName: String,
        technicianId: String,
        instructions: String,
        priority: AlertSeverity
    ) {
        viewModelScope.launch {
            repository.dispatchTechnician(
                alertId = alertId,
                towerId = towerId,
                towerName = towerName,
                technicianId = technicianId,
                instructions = instructions,
                priority = priority
            )
        }
    }

    fun updateWorkOrderStatus(workOrder: WorkOrder, nextStatus: String, notes: String = "") {
        viewModelScope.launch {
            repository.updateWorkOrderStatus(workOrder, nextStatus, notes)
        }
    }

    fun generateReport(period: String, onFinished: (AutomatedReport) -> Unit = {}) {
        viewModelScope.launch {
            val report = repository.generateAutomatedReport(period)
            onFinished(report)
        }
    }

    fun createTower(tower: Tower) {
        viewModelScope.launch {
            repository.addNewTower(tower)
        }
    }
}
