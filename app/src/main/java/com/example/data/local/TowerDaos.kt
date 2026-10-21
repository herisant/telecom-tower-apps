package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TowerDao {
    @Query("SELECT * FROM towers ORDER BY name ASC")
    fun getAllTowers(): Flow<List<Tower>>

    @Query("SELECT * FROM towers WHERE id = :id")
    suspend fun getTowerById(id: String): Tower?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTowers(towers: List<Tower>)

    @Update
    suspend fun updateTower(tower: Tower)

    @Query("UPDATE towers SET status = :status WHERE id = :towerId")
    suspend fun updateTowerStatus(towerId: String, status: TowerStatus)
}

@Dao
interface FaultAlertDao {
    @Query("SELECT * FROM fault_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<FaultAlert>>

    @Query("SELECT * FROM fault_alerts WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getActiveAlerts(): Flow<List<FaultAlert>>

    @Query("SELECT * FROM fault_alerts WHERE towerId = :towerId ORDER BY timestamp DESC")
    fun getAlertsForTower(towerId: String): Flow<List<FaultAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: FaultAlert)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<FaultAlert>)

    @Update
    suspend fun updateAlert(alert: FaultAlert)

    @Query("UPDATE fault_alerts SET isResolved = 1, resolvedAt = :resolvedAt WHERE id = :alertId")
    suspend fun resolveAlert(alertId: String, resolvedAt: Long = System.currentTimeMillis())

    @Query("UPDATE fault_alerts SET assignedTechnicianId = :techId, assignedTechnicianName = :techName WHERE id = :alertId")
    suspend fun assignTechnicianToAlert(alertId: String, techId: String, techName: String)
}

@Dao
interface TechnicianDao {
    @Query("SELECT * FROM technicians ORDER BY name ASC")
    fun getAllTechnicians(): Flow<List<Technician>>

    @Query("SELECT * FROM technicians WHERE id = :id")
    suspend fun getTechnicianById(id: String): Technician?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnicians(technicians: List<Technician>)

    @Update
    suspend fun updateTechnician(technician: Technician)

    @Query("UPDATE technicians SET status = :status, activeWorkOrderId = :workOrderId WHERE id = :techId")
    suspend fun updateTechnicianStatus(techId: String, status: String, workOrderId: String?)
}

@Dao
interface WorkOrderDao {
    @Query("SELECT * FROM work_orders ORDER BY createdAt DESC")
    fun getAllWorkOrders(): Flow<List<WorkOrder>>

    @Query("SELECT * FROM work_orders WHERE status != 'RESOLVED' ORDER BY createdAt DESC")
    fun getActiveWorkOrders(): Flow<List<WorkOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrder(workOrder: WorkOrder)

    @Update
    suspend fun updateWorkOrder(workOrder: WorkOrder)

    @Query("UPDATE work_orders SET status = :status, resolvedAt = :resolvedAt, repairNotes = :notes WHERE id = :woId")
    suspend fun resolveWorkOrder(woId: String, status: String = "RESOLVED", resolvedAt: Long = System.currentTimeMillis(), notes: String)
}

@Dao
interface AutomatedReportDao {
    @Query("SELECT * FROM automated_reports ORDER BY id DESC")
    fun getAllReports(): Flow<List<AutomatedReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: AutomatedReport)
}
