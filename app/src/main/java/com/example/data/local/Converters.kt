package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AlertSeverity
import com.example.data.model.SubsystemType
import com.example.data.model.TowerStatus
import com.example.data.model.TowerType

class Converters {
    @TypeConverter
    fun fromTowerStatus(value: TowerStatus?): String = value?.name ?: TowerStatus.NORMAL.name

    @TypeConverter
    fun toTowerStatus(value: String?): TowerStatus = try {
        value?.let { TowerStatus.valueOf(it) } ?: TowerStatus.NORMAL
    } catch (_: Exception) {
        TowerStatus.NORMAL
    }

    @TypeConverter
    fun fromTowerType(value: TowerType?): String = value?.name ?: TowerType.SST_4_LEG.name

    @TypeConverter
    fun toTowerType(value: String?): TowerType = try {
        value?.let { TowerType.valueOf(it) } ?: TowerType.SST_4_LEG
    } catch (_: Exception) {
        TowerType.SST_4_LEG
    }

    @TypeConverter
    fun fromSubsystemType(value: SubsystemType?): String = value?.name ?: SubsystemType.POWER.name

    @TypeConverter
    fun toSubsystemType(value: String?): SubsystemType = try {
        value?.let { SubsystemType.valueOf(it) } ?: SubsystemType.POWER
    } catch (_: Exception) {
        SubsystemType.POWER
    }

    @TypeConverter
    fun fromAlertSeverity(value: AlertSeverity?): String = value?.name ?: AlertSeverity.MINOR.name

    @TypeConverter
    fun toAlertSeverity(value: String?): AlertSeverity = try {
        value?.let { AlertSeverity.valueOf(it) } ?: AlertSeverity.MINOR
    } catch (_: Exception) {
        AlertSeverity.MINOR
    }
}
