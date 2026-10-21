package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Tower::class,
        FaultAlert::class,
        Technician::class,
        WorkOrder::class,
        AutomatedReport::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TowerOpsDatabase : RoomDatabase() {
    abstract fun towerDao(): TowerDao
    abstract fun faultAlertDao(): FaultAlertDao
    abstract fun technicianDao(): TechnicianDao
    abstract fun workOrderDao(): WorkOrderDao
    abstract fun automatedReportDao(): AutomatedReportDao

    companion object {
        @Volatile
        private var INSTANCE: TowerOpsDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TowerOpsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TowerOpsDatabase::class.java,
                    "tower_ops_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: TowerOpsDatabase) {
            val towers = listOf(
                Tower(
                    id = "TWR-JKT-042",
                    name = "Menara Merdeka Macro 5G",
                    siteCode = "JKT-042-MRD",
                    region = "Jabodetabek",
                    city = "Jakarta Pusat",
                    address = "Jl. Medan Merdeka Barat No. 12",
                    latitude = -6.175392,
                    longitude = 106.827153,
                    towerType = TowerType.SST_4_LEG,
                    heightMeters = 72,
                    operatorShared = "Telkomsel, Indosat Ooredoo Hutchison, XL Axiata",
                    status = TowerStatus.CRITICAL,
                    lastInspected = "2026-10-18",
                    powerSource = "PLN 33 kVA + Genset Cummins 40 kVA + Lithium-Ion 48V"
                ),
                Tower(
                    id = "TWR-JKT-108",
                    name = "Kuningan Hub Monopole",
                    siteCode = "JKT-108-KNG",
                    region = "Jabodetabek",
                    city = "Jakarta Selatan",
                    address = "Jl. HR Rasuna Said Blok X-5",
                    latitude = -6.224160,
                    longitude = 106.831200,
                    towerType = TowerType.MONOPOLE,
                    heightMeters = 42,
                    operatorShared = "Telkomsel, Smartfren",
                    status = TowerStatus.NORMAL,
                    lastInspected = "2026-10-15",
                    powerSource = "PLN 16.5 kVA + Solar Cell Hybrid 5 kW"
                ),
                Tower(
                    id = "TWR-BDG-019",
                    name = "Dago Highland Repeater",
                    siteCode = "BDG-019-DGO",
                    region = "Jawa Barat",
                    city = "Bandung",
                    address = "Jl. Ir. H. Juanda No. 390, Dago",
                    latitude = -6.865420,
                    longitude = 107.618900,
                    towerType = TowerType.GUYED_MAST,
                    heightMeters = 60,
                    operatorShared = "Telkomsel, XL Axiata, Indosat",
                    status = TowerStatus.WARNING,
                    lastInspected = "2026-10-12",
                    powerSource = "PLN 22 kVA + Genset Perkins 30 kVA"
                ),
                Tower(
                    id = "TWR-SBY-088",
                    name = "Gubeng Gateway Central",
                    siteCode = "SBY-088-GBG",
                    region = "Jawa Timur",
                    city = "Surabaya",
                    address = "Jl. Raya Gubeng No. 54",
                    latitude = -7.275440,
                    longitude = 112.753300,
                    towerType = TowerType.SST_4_LEG,
                    heightMeters = 68,
                    operatorShared = "Telkomsel, Indosat, XL, Smartfren",
                    status = TowerStatus.NORMAL,
                    lastInspected = "2026-10-19",
                    powerSource = "PLN 41.5 kVA + Genset Kohler 50 kVA"
                ),
                Tower(
                    id = "TWR-DPS-007",
                    name = "Kuta Coastal Tourism BTS",
                    siteCode = "DPS-007-KTA",
                    region = "Bali & Nusa Tenggara",
                    city = "Badung, Bali",
                    address = "Jl. Pantai Kuta No. 10B",
                    latitude = -8.722510,
                    longitude = 115.169800,
                    towerType = TowerType.MONOPOLE,
                    heightMeters = 36,
                    operatorShared = "Telkomsel, XL Axiata",
                    status = TowerStatus.WARNING,
                    lastInspected = "2026-10-14",
                    powerSource = "PLN 16.5 kVA + Backup Battery LiFePO4"
                ),
                Tower(
                    id = "TWR-MDN-033",
                    name = "Medan Amplas Backbone SST",
                    siteCode = "MDN-033-AMP",
                    region = "Sumatera Bagian Utara",
                    city = "Medan",
                    address = "Jl. Sisingamangaraja KM 9.5",
                    latitude = 3.541200,
                    longitude = 98.701100,
                    towerType = TowerType.SST_4_LEG,
                    heightMeters = 80,
                    operatorShared = "Telkomsel, Indosat, XL",
                    status = TowerStatus.NORMAL,
                    lastInspected = "2026-10-10",
                    powerSource = "PLN 33 kVA + Dual Genset Auto-Failover"
                ),
                Tower(
                    id = "TWR-SMG-051",
                    name = "Simpang Lima Rooftop Node",
                    siteCode = "SMG-051-SPL",
                    region = "Jawa Tengah",
                    city = "Semarang",
                    address = "Kawasan Simpang Lima No. 1",
                    latitude = -6.992400,
                    longitude = 110.423000,
                    towerType = TowerType.MONOPOLE,
                    heightMeters = 40,
                    operatorShared = "Telkomsel, Indosat",
                    status = TowerStatus.NORMAL,
                    lastInspected = "2026-10-17",
                    powerSource = "PLN 22 kVA + Smart Battery Bank"
                )
            )
            db.towerDao().insertTowers(towers)

            val technicians = listOf(
                Technician(
                    id = "TECH-01",
                    name = "Bambang Kurniawan, S.T.",
                    phone = "+6281234567801",
                    specialization = "Power & Genset Electrical",
                    status = "AVAILABLE",
                    latitude = -6.182000,
                    longitude = 106.832000,
                    rating = 4.9f,
                    certifications = "Sertifikasi K3 Listrik Arus Kuat, Genset ATS Specialist"
                ),
                Technician(
                    id = "TECH-02",
                    name = "Rahmat Hidayat",
                    phone = "+6281234567802",
                    specialization = "RF & Antenna Microwave Rigger",
                    status = "AVAILABLE",
                    latitude = -6.215000,
                    longitude = 106.820000,
                    rating = 4.8f,
                    certifications = "K3 Tenaga Kerja Pada Ketinggian (TKPK Tingkat 2), VSWR Analyzer"
                ),
                Technician(
                    id = "TECH-03",
                    name = "Dian Permana",
                    phone = "+6281234567803",
                    specialization = "Fiber Optic & Transmission",
                    status = "ON_DUTY",
                    latitude = -6.872000,
                    longitude = 107.610000,
                    rating = 4.95f,
                    certifications = "OTDR Certified, SFP Link Specialist, Huawei/Ericsson BBU Certified"
                ),
                Technician(
                    id = "TECH-04",
                    name = "Made Arya Sedana",
                    phone = "+6281234567804",
                    specialization = "IoT Sensor & Environmental Control",
                    status = "AVAILABLE",
                    latitude = -8.718000,
                    longitude = 115.175000,
                    rating = 4.75f,
                    certifications = "HVAC Shelter Master, Smart Remote Monitoring System (RMS)"
                ),
                Technician(
                    id = "TECH-05",
                    name = "Faisal Pratama",
                    phone = "+6281234567805",
                    specialization = "Rapid Emergency Response & Climbing",
                    status = "AVAILABLE",
                    latitude = -7.269000,
                    longitude = 112.748000,
                    rating = 4.85f,
                    certifications = "Tower Rigging Master, First Aid Rescue WAH Level 3"
                )
            )
            db.technicianDao().insertTechnicians(technicians)

            val initialAlerts = listOf(
                FaultAlert(
                    id = "ALT-2026-901",
                    towerId = "TWR-JKT-042",
                    towerName = "Menara Merdeka Macro 5G",
                    severity = AlertSeverity.CRITICAL,
                    subsystem = SubsystemType.POWER,
                    title = "PLN Mains Outage & Low Battery Backup (< 32%)",
                    description = "Tegangan PLN drop ke 0V. ATS gagal memicu genset otomatis. Baterai lithium tersisa 32% (estimasi daya habis 42 menit).",
                    rootCauseRecommendation = "Segera kirim teknisi Genset untuk manual start atau bypass breaker ATS modul 2.",
                    timestamp = System.currentTimeMillis() - 1000L * 60 * 25,
                    isResolved = false
                ),
                FaultAlert(
                    id = "ALT-2026-884",
                    towerId = "TWR-BDG-019",
                    towerName = "Dago Highland Repeater",
                    severity = AlertSeverity.MAJOR,
                    subsystem = SubsystemType.RADIO_RF,
                    title = "VSWR Spike Sector 2 Antena 2100MHz (Ratio: 2.6:1)",
                    description = "Refleksi daya tinggi terdeteksi pada Sektor 2. Penurunan jangkauan sinyal 4G sekitar 40%.",
                    rootCauseRecommendation = "Indikasi jumper cable terkelupas akibat cuaca atau konektor DIN 7/16 longgar.",
                    timestamp = System.currentTimeMillis() - 1000L * 60 * 95,
                    isResolved = false
                ),
                FaultAlert(
                    id = "ALT-2026-871",
                    towerId = "TWR-DPS-007",
                    towerName = "Kuta Coastal Tourism BTS",
                    severity = AlertSeverity.MINOR,
                    subsystem = SubsystemType.ENVIRONMENT,
                    title = "Shelter Air Conditioner Failure - Cabin Temp 36.8°C",
                    description = "AC unit 1 compressor trip. Suhu kabin melebihi threshold 30°C.",
                    rootCauseRecommendation = "Switch ke pendingin sekunder atau bersihkan filter kondensor luar.",
                    timestamp = System.currentTimeMillis() - 1000L * 60 * 180,
                    isResolved = false
                )
            )
            db.faultAlertDao().insertAlerts(initialAlerts)

            val initialReports = listOf(
                AutomatedReport(
                    id = "REP-2026-10-W3",
                    title = "Laporan Mingguan Ketersediaan Infrastruktur Menara (W3 Oktober 2026)",
                    period = "Mingguan",
                    generatedDate = "2026-10-20 18:00 WIB",
                    totalTowersMonitored = 7,
                    overallSlaUptimePercent = 99.94f,
                    totalIncidents = 14,
                    resolvedIncidents = 12,
                    avgMttrMinutes = 48,
                    fuelConsumedLiters = 345.5f,
                    criticalAlertCount = 2,
                    summaryText = "Performa jaringan telekomunikasi nasional stabil. Ketersediaan daya listrik PLN mencatat 98.2% uptime dengan dukungan Genset dan Baterai Lithium. Dua insiden kritis pada sektor Merdeka Macro dan Amplas telah direspons sesuai SOP 60 menit MTTR."
                )
            )
            initialReports.forEach { db.automatedReportDao().insertReport(it) }
        }
    }
}
