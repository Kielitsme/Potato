<div align="center">

# LowSpec Launcher

### Launcher Minecraft Java Edition untuk Android yang diprioritaskan untuk HP low-end

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026%2B)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)

> **Proyek independen dan TIDAK RESMI.** Fork dari **NUX Launcher**, yang dibangun di atas
> **[ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2)** dan
> **[PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)**.
> Tidak berafiliasi dengan Mojang Studios, Microsoft, tim Zalith, maupun tim PojavLauncher.

</div>

---

## Tujuan

Performa di perangkat low-end lebih penting daripada jumlah fitur. Karena itu fork ini:

- **Tanpa server pihak launcher.** Tidak ada login email, premium, teman/voice, pengumuman, atau cek update lewat server NUX.
- **Hanya dua renderer ringan**: GL4ES 1.1.4 (OpenGL ES 2, Minecraft 1.7-1.16.5) dan Krypton / NG-GL4ES (OpenGL ES 3, Minecraft 1.17+). Renderer berat berbasis Mesa/Vulkan (Zink, Freedreno, VirGL, Panfrost) dan MobileGlues sudah dibuang, jadi **shader tidak didukung**.
- **JVM disetel untuk heap kecil**: SerialGC untuk heap <= 3 GB, tanpa file hsperfdata.
- **Default per tingkat RAM**: heap dan resolusi render fresh-install menyesuaikan RAM perangkat; slider RAM dibatasi 65% dari RAM fisik.
- **Opsi grafis game otomatis ringan** (render distance rendah, partikel minimal, FPS maks 60, dst).
- **UI launcher tanpa efek berat**: tanpa blur, video latar, bitmap latar besar, maupun viewer skin 3D (WebView/WebGL).
- Fitur AI, perekam layar, lemari skin, dan semua library native yang tidak dipakai sudah dihapus (aset + native turun dari ~365 MB ke ~209 MB).

Yang tetap ada: login Microsoft / Ely.by / offline, unduh versi dari Mojang, Fabric/Forge/NeoForge/Quilt, mod browser Modrinth, kontrol sentuh kustom, dan upload log ke mclo.gs.

---

## Kepatuhan Lisensi (GPL-3.0)

Proyek ini adalah turunan dari karya berlisensi **GNU GPL v3.0**. Seluruh kode sumber dipublikasikan, siapa pun bebas
memeriksa, memodifikasi, dan mendistribusikan ulang sesuai ketentuan GPL v3. Pemberitahuan hak cipta dan atribusi hulu
dipertahankan (lihat dialog "Tentang" di aplikasi).

## Kredit Hulu

- **[NUX Launcher](https://github.com/IsraaDeveloper/nuxlabs)** — basis fork ini.
- **[ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2)** — Movtery dan kontributor (GPL-3.0).
- **[PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)** — artdeell, khanhtran, PojavLauncherTeam (GPL-3.0).
- **[GL4ES](https://github.com/ptitSeb/gl4es)** — ptitSeb (MIT); renderer Krypton memakai fork NG-GL4ES oleh BZLZHH.
- **[Fold Craft Launcher](https://github.com/FCL-Team/FoldCraftLauncher)** — inspirasi modul dan utilitas (GPL-3.0).
- **[Ely.by](https://ely.by)** dan **[Modrinth](https://modrinth.com)** — layanan autentikasi dan katalog mod.

---

## Build

Prasyarat: Android Studio terbaru, JDK 17, Android SDK (compile 37, min 26), NDK r25+.

```bash
git clone https://github.com/Kielitsme/Potato.git
cd Potato
cp local.properties.example local.properties   # isi sdk.dir
./gradlew assembleDebug
```

APK debug ada di `app/build/outputs/apk/debug/`. Build release memakai R8 (`isMinifyEnabled = true`); jika build release
bermasalah, set `isMinifyEnabled` dan `isShrinkResources` ke `false` di `app/build.gradle.kts`.

Application ID: `io.github.kielitsme.lowspec` (package Kotlin tetap `com.israadev.nuxlauncher` karena JNI native
terikat ke nama package tersebut, jadi jangan di-rename).

---

## Penafian

- **BUKAN PRODUK RESMI MINECRAFT. TIDAK DISETUJUI ATAU TERKAIT DENGAN MOJANG ATAU MICROSOFT.**
- *Minecraft* adalah merek dagang Mojang AB / Microsoft Corporation.
- File game diunduh langsung dari server distribusi resmi Mojang sesuai lisensi akun pengguna.
