# 📡 TowerOps — Sistem Pemantauan Menara Telekomunikasi Real-Time

**TowerOps** adalah aplikasi Android berbasis Jetpack Compose dan Room Database yang dirancang khusus untuk Network Operations Center (NOC) infrastruktur telekomunikasi. Aplikasi ini menyediakan pemantauan telemetri perangkat menara Base Transceiver Station (BTS) secara *real-time*, deteksi anomali/kerusakan perangkat cerdas, manajemen regu teknisi lapangan, peta interaktif presisi, serta dasbor pelaporan otomatis berformat web responsif.

---

## 🚀 Fitur Utama

### 1. 🎛️ NOC Dashboard & Pemantauan Telemetri Real-Time
- **4 Subsistem Sensor Terintegrasi:**
  - **Subsistem Daya & Kelistrikan:** Tegangan PLN (*Grid Voltage* AC 220V), status *Automatic Transfer Switch* (ATS), bahan bakar Genset cadangan (% Solar & status *running*), dan kapasitas baterai *Lithium-Ion* (SoC % dan suhu baterai).
  - **Subsistem Radio Frekuensi (RF & Antena):** Rasio impedansi *Voltage Standing Wave Ratio* (VSWR) antena sektoral, suhu *Remote Radio Unit* (RRU), *attenuation loss* kabel *Fiber Optic SFP*, utilisasi CPU *Baseband Unit* (BBU), *throughput* live 5G (Mbps), dan *latency ping*.
  - **Subsistem Shelter & Lingkungan IoT:** Sensor pendingin AC kabin, kelembaban udara, sensor intrusi pintu (*door access control*), sensor asap & deteksi banjir.
  - **Subsistem Struktur & Keselamatan:** Sensor lampu suar menara (*Obstruction Beacon Light* - OBL), dan sensor kemiringan/getaran tiang menara (*tilt & vibration sensor*).
- **Background Telemetry Engine:** Simulasi fluktuasi sensor dinamis setiap beberapa detik untuk merefleksikan kondisi beban jaringan dan cuaca riil.

### 2. ⚠️ Deteksi Kerusakan Otomatis & Analisis Akar Masalah (RCA)
- Klasifikasi tingkat alarm otomatis: **CRITICAL**, **MAJOR**, **MINOR**, dan **INFO**.
- Analisis Akar Masalah (*Root Cause Recommendation*) yang menyajikan rekomendasi tindakan dan SOP teknis di lapangan.
- **Simulator Injeksi Anomali (*Fault Injection*):** Kemampuan untuk menguji skenario darurat secara langsung (Pemadaman PLN + kegagalan ATS Genset, Lonjakan VSWR > 2.8:1, *Overheat* Shelter > 41°C, Pembobolan Pintu Shelter, atau Kemiringan Tiang Menara > 2.4°).
- Banner darurat berkedip dan *in-app snackbar notification* dengan tombol aksi langsung ke tiket perbaikan.

### 3. 🗺️ Integrasi Peta Interaktif Presisi (GIS Canvas Map)
- Peta vektor interaktif berbasis kanvas dengan kendali gestur:
  - **Pinch-to-Zoom** dan tombol pembesaran (+ / - / Reset View).
  - **Drag-to-Pan** melintasi koordinat BTS di seluruh wilayah Indonesia (Jabodetabek, Jawa Barat, Jawa Timur, Jawa Tengah, Bali, dan Sumatera).
- Penanda menara BTS presisi dengan indikator warna status (*Green = Normal*, *Yellow = Warning*, *Red = Critical Alert*, *Blue = Maintenance*) lengkap dengan denyut radar aktif.
- Visualisasi **Cincin Jangkauan Sinyal (Coverage Rings 4G/5G)** dan posisi armada regu teknisi lapangan.
- **Garis Respons Rute Darurat (*Emergency Dispatch Lines*):** Menghubungkan posisi teknisi yang ditugaskan ke lokasi menara yang mengalami insiden.
- Kartu info mengambang (*Floating Site Intelligence Sheet*) saat menara ditekan untuk memeriksa telemetri cepat dan tombol disposisi.

### 4. 👷 Manajemen Tim Teknisi & Surat Perintah Kerja (SPK)
- Roster personel teknisi lapangan bersertifikasi (K3 Ketinggian TKPK, Genset & Kelistrikan Arus Kuat, Spesialis *Fiber Optic* & SFP, serta Rigger Menara).
- Status kesiapan teknisi (*Available, On Duty, En Route, On Site*).
- Perhitungan estimasi jarak otomatis (dalam km) dari posisi teknisi ke lokasi menara sasaran.
- Integrasi tombol panggilan telepon langsung dan tautan obrolan WhatsApp darurat.
- Pelacakan alur kerja tiket SPK: `Assigned` ➔ `En Route` ➔ `On Site` ➔ `Resolved & BTS Restored`.

### 5. 📊 Dasbor Berbasis Web & Pelaporan Otomatis
- **Tampilan Portal Web Responsif:** Antarmuka bergaya konsol web operasional NOC lengkap dengan URL bar simulasi (`https://noc-portal.towerops.telecom.id`), kalkulator ketersediaan SLA 99.94%, konsumsi BBM Genset, dan riwayat waktu perbaikan rata-rata (*Mean Time to Repair* / MTTR).
- **Generator Laporan Otomatis:** Pembuatan laporan berkala (Harian, Mingguan, Bulanan) dalam format resmi berstandar regulasi kementerian/operator dengan kop surat, nomor dokumen, ringkasan eksekutif, tabel audit site, dan segel verifikasi digital.
- **Fitur Ekspor & Bagikan:** Dukungan berbagi langsung (*Android Share Sheet*) ke WhatsApp, Email, atau aplikasi pengolah dokumen lainnya.

---

## 🏗️ Arsitektur Proyek & Struktur Kode

Aplikasi dibangun mengikuti arsitektur **MVVM (Model-View-ViewModel)** dengan Clean Architecture principles:

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Activity utama dengan Edge-to-Edge & NavigationBar
├── data/
│   ├── model/
│   │   └── TowerModels.kt               # Entity Room: Tower, TelemetryData, FaultAlert, Technician, WorkOrder, AutomatedReport
│   ├── local/
│   │   ├── Converters.kt                # TypeConverters Room untuk Enum (TowerStatus, AlertSeverity, SubsystemType, dll)
│   │   ├── TowerDaos.kt                 # Room DAOs (TowerDao, FaultAlertDao, TechnicianDao, WorkOrderDao, AutomatedReportDao)
│   │   └── TowerOpsDatabase.kt          # Room Database konfigurasi & seeder data BTS realistis Indonesia
│   └── repository/
│       └── TowerOpsRepository.kt        # Repository layer: simulasi telemetri live, fault detection, dispatch, auto-report
└── ui/
    ├── AppTab.kt & TowerOpsViewModel.kt # StateFlow/SharedFlow & logika bisnis antarmuka
    ├── theme/
    │   ├── Color.kt                     # Skema warna Dark NOC Palette (Cyan Neon, Slate Navy, Status Amber/Red/Green)
    │   ├── Theme.kt                     # Material 3 Theme setup
    │   └── Type.kt                      # Konfigurasi tipografi M3
    ├── components/
    │   ├── CommonUiComponents.kt        # Komponen UI: StatusPulseDot, TowerStatusBadge, AlertSeverityBadge, MetricCard, Gauge
    │   ├── InteractiveTowerMap.kt       # Kanvas Peta Interaktif presisi (GIS rendering, radar waves, coverage, rute teknisi)
    │   └── OperationDialogs.kt          # Dialog Disposisi Teknisi, Simulator Injeksi Anomali, Registrasi Menara Baru
    └── screens/
        ├── DashboardScreen.kt           # NOC Executive Dashboard, live telemetry stream & KPI card
        ├── TowerListDetailScreen.kt     # Inventaris menara & deep diagnostics subsistem perangkat
        ├── FaultDetectionScreen.kt      # Deteksi kerusakan real-time, RCA & riwayat alarm
        ├── TechnicianTeamScreen.kt      # Direktori personel lapangan, kontak cepat & tiket SPK
        ├── InteractiveMapScreen.kt      # Tampilan layar penuh peta telekomunikasi presisi
        └── WebDashboardReportScreen.kt  # Portal web responsif & generator laporan resmi otomatis
```

---

## 🛠️ Tumpukan Teknologi (Tech Stack)

- **Bahasa Pemrograman:** Kotlin 2.x
- **UI Toolkit:** Jetpack Compose (Material Design 3)
- **Database Lokal:** Android Room Database (SQLite dengan Coroutines & Flow)
- **Asynchronous / Concurrency:** Kotlin Coroutines & StateFlow / SharedFlow
- **Image Loading:** Coil Compose
- **Desain & Gaya:** Dark Mission-Control NOC Theme dengan aksen Cyan `#00E5FF` dan Amber `#F59E0B`
- **Build System:** Gradle (Kotlin DSL - `.gradle.kts`) dengan Version Catalog (`libs.versions.toml`)

---

## 📋 Panduan Operasional / Cara Menguji Fitur

1. **Menguji Deteksi Kerusakan & Anomali:**
   - Masuk ke tab **NOC** atau **Kerusakan**.
   - Klik tombol **"Uji Anomali"**.
   - Pilih menara target dan jenis insiden (misalnya: *Pemadaman PLN & Kegagalan ATS Genset* atau *Refleksi VSWR Antena > 2.8:1*).
   - Klik **"Mulai Pemicuan Anomali"**.
   - Sistem akan langsung mendeteksi kerusakan, mengubah status menara menjadi *Critical/Warning*, memperbarui data telemetri, dan memunculkan banner alarm darurat.
2. **Menugaskan Teknisi:**
   - Pada banner alarm atau kartu deteksi kerusakan, klik tombol **"Tugaskan Tim"**.
   - Sistem akan menyajikan daftar teknisi terdekat dengan perhitungan jarak GPS otomatis.
   - Pilih teknisi dan klik **"Kirim Penugasan SPK"**.
   - Di tab **Peta**, garis rute darurat (*emergency line*) akan terhubung dari lokasi teknisi ke menara tersebut.
3. **Menyelesaikan Tiket Kerja:**
   - Buka tab **Teknisi** ➔ pilih sub-tab **Tiket SPK Kerja**.
   - Klik **"Konfirmasi Tiba di Site"** kemudian klik **"Selesaikan & Pulihkan Site"**.
   - Status menara dan sensor telemetri akan pulih kembali ke normal secara otomatis.
4. **Membuat & Mengekspor Laporan Otomatis:**
   - Buka tab **Web & Lap**.
   - Pilih periode laporan (Harian, Mingguan, atau Bulanan) dan klik **"Buat Laporan Baru"**.
   - Sistem akan mengalkulasi statistik SLA, MTTR, konsumsi BBM, dan rekapitulasi audit.
   - Klik **"Ekspor / Bagikan"** untuk membagikan laporan resmi berformat kementerian melalui WhatsApp, email, atau dokumen.

---

## 🔄 Pemeliharaan & Sinkronisasi Kode

> **Catatan Pemeliharaan:**  
> File `README.md` ini harus selalu diperbarui setiap kali ada perubahan pada model data, penambahan subsistem sensor telemetri baru, penyesuaian dependensi pada `libs.versions.toml`, atau penambahan alur navigasi di `MainActivity.kt`.
