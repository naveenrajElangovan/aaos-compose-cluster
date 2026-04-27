# AAOS Instrument Cluster — Jetpack Compose

> **Android project** — requires Android Studio + Android Automotive OS environment  
> Part of the [AutoOS Multi-OEM Platform](https://github.com/naveenrajElangovan) · Built on AOSP Android 16 (Baklava)

A production-grade **instrument cluster app** for Android Automotive OS (AAOS), built entirely with Jetpack Compose. Targets **Display 1** (400×600px) on Cuttlefish — the dedicated cluster screen behind the steering wheel.

All gauge data flows from real AOSP CarService via `CarPropertyManager`. Zero hardcoded or simulated values — every number on screen originates from a live VHAL property.

---

## What This Is

```
Android Automotive OS (AAOS)
        │
        ├── Display 0 (1080×600)  ←  Infotainment / Center Stack
        │
        └── Display 1 (400×600)   ←  THIS APP runs here
                  │
            Instrument Cluster
            Speed · RPM · Gear · Fuel
```

This is **not a phone app**. It is an AAOS system-level application that:
- Binds to `ClusterHomeService` via `ClusterRenderingService`
- Routes itself to **Display 1** using `ActivityOptions.setLaunchDisplayId(1)`
- Subscribes to live VHAL properties through `CarPropertyManager`
- Recomposes only changed gauge sections via Compose `StateFlow`

---

## Data Pipeline

```
Vehicle ECU / VHAL Injection
        ↓
VHAL (C++ HAL layer)
        ↓  Binder IPC
CarService (AOSP system service)
        ↓  CarPropertyManager.registerCallback()
ClusterViewModel (StateFlow<ClusterState>)
        ↓  collectAsState()
Jetpack Compose UI  →  Canvas arc redraws in ~16ms
```

---

## Gauges

| Gauge | VHAL Property | Range | Notes |
|---|---|---|---|
| Speedometer arc | `PERF_VEHICLE_SPEED` | 0 – 220 km/h | Raw m/s × 3.6 |
| RPM bar | `PERF_ENGINE_RPM` | 0 – 8000 RPM | Redline > 6500 |
| Gear indicator | `CURRENT_GEAR` | P / R / N / D / S | Int32 enum |
| Fuel ring | `FUEL_LEVEL` | 0 – 100 % | Warning pulse < 15% |
| Battery | `EV_BATTERY_LEVEL` | 0 – 100 % | EV mode |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.0 |
| UI | Jetpack Compose + Canvas API |
| Architecture | MVVM · StateFlow · ViewModel |
| Car APIs | CarPropertyManager · ClusterRenderingService |
| Min SDK | 33 (Android Automotive Tiramisu) |
| Build | AGP 8.7.0 · Gradle KTS |
| Emulator | Cuttlefish AAOS on GCP with KVM |

---

## Project Structure

```
app/src/main/java/com/fordmx/cluster/
├── ClusterActivity.kt               ← Compose host, keeps screen on
├── ClusterApplication.kt            ← Timber init
├── service/
│   └── ClusterRenderingService.kt   ← Binds to ClusterHomeService, routes to Display 1
├── presentation/
│   ├── screen/
│   │   └── ClusterScreen.kt         ← Root composable, full cluster layout
│   ├── components/
│   │   ├── SpeedometerGauge.kt      ← Canvas arc gauge with animateFloatAsState
│   │   ├── RpmBar.kt               ← Horizontal bar, redline zone
│   │   ├── GearIndicator.kt        ← P/R/N/D/S with transition animation
│   │   └── FuelRing.kt             ← Circular ring, warning pulse below 15%
│   ├── viewmodel/
│   │   └── ClusterViewModel.kt     ← CarPropertyManager subscriptions → StateFlow
│   └── theme/
│       ├── Theme.kt                ← FordClusterTheme dark
│       ├── Color.kt                ← #0A0E1A bg · #1A6EF5 accent · #4FC3F7 cyan
│       └── Type.kt                 ← Automotive-safe typography
└── data/
    └── ClusterState.kt             ← Single state data class for all gauges
```

---

## Requirements

### To build and run

- **Android Studio** Hedgehog or later
- **Android SDK** API 33+
- **AAOS emulator** — one of:
  - Android Studio AVD → Automotive (1024p landscape) with API 33
  - Cuttlefish on GCP (see setup below)

### To see live VHAL data

You need an AAOS environment (Cuttlefish or real hardware). The app will install on a standard Automotive AVD but `CarPropertyManager` returns null outside a real AAOS build — gauges will show 0 until VHAL injection.

---

## Quick Start

### 1. Clone and open

```bash
git clone https://github.com/naveenrajElangovan/aaos-compose-cluster.git
cd aaos-compose-cluster
```

Open in **Android Studio** → let Gradle sync.

### 2. Run on Android Studio Automotive AVD

```
Tools → Device Manager → Create Virtual Device
→ Category: Automotive
→ Hardware: Automotive (1024p landscape)
→ System Image: API 33 arm64
→ Finish → Launch
```

```bash
# Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Testing VHAL Injection (Cuttlefish)

Inject vehicle data directly into VHAL to see gauges respond live:

```bash
# Speed — continuous sweep 0 → 120 → 0 km/h
adb shell genfakedata --startlinear \
  PERF_VEHICLE_SPEED 0.0 33.3 0.5

# Single speed value (22.2 m/s = 80 km/h)
adb shell dumpsys vehicle_hal set PERF_VEHICLE_SPEED 22.2

# RPM
adb shell dumpsys vehicle_hal set PERF_ENGINE_RPM 3500.0

# Gear — 1=D, 2=N, 3=R, 4=P
adb shell dumpsys vehicle_hal set CURRENT_GEAR 1

# Fuel level (%)
adb shell dumpsys vehicle_hal set FUEL_LEVEL 72.0

# Low fuel warning trigger
adb shell dumpsys vehicle_hal set FUEL_LEVEL 12.0
```

---

## Display Setup on Cuttlefish

Cuttlefish exposes two displays via WebRTC:

| Display | Resolution | Role |
|---|---|---|
| Display 0 | 1080 × 600 | Infotainment (center stack) |
| Display 1 | 400 × 600 | **Instrument cluster ← this app** |

Access via browser after SSH tunnel:


## Theme

Ford dark minimal — Polestar/BMW iDrive inspired. No Android-looking components.

| Token | Value | Usage |
|---|---|---|
| Background | `#0A0E1A` | Full screen |
| Accent blue | `#1A6EF5` | Speedometer arc, active states |
| Cyan | `#4FC3F7` | RPM bar fill, highlights |
| Warning | `#FF5252` | Redline, low fuel |
| Text primary | `#E8EDF5` | Speed digits, labels |
| Text secondary | `#7A8499` | Unit labels, inactive |

---

## Automotive UI Rules Applied

| Rule | Implementation |
|---|---|
| Glanceable in < 2s | Large numerals, minimal decoration |
| No interaction while driving | Read-only display, no touch targets on gauges |
| Touch targets ≥ 76dp | N/A — cluster is display-only |
| Day / Night theme | `UiModeManager` drives `FordClusterTheme` |
| No video content | Static Compose Canvas only |
| Screen always on | `FLAG_KEEP_SCREEN_ON` in ClusterActivity |

---

## Part of AutoOS Platform

This cluster app is one component of a larger multi-OEM AAOS platform:

```
AutoOS Platform
├── Custom VHAL (C++)          ← AUTOSAR signals + ISO 26262
├── Python vehicle simulator   ← City / highway / ADAS scenarios
├── aaos-compose-cluster       ← THIS REPO — Display 1
├── FordHome launcher          ← Display 0 custom home
├── Dashboard app              ← Live WebSocket vehicle data
└── RRO theme overlays         ← Multi-OEM branding
```

---

## Author

**Naveenraj Elangovan** — Senior Android & Automotive OS Developer  
[LinkedIn](https://linkedin.com/in/naveenrajelangovan) · [GitHub](https://github.com/naveenrajElangovan)  
Building Android Automotive OS platforms from AOSP source · Mexico City

---

## License

```
Copyright 2026 Naveenraj Elangovan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
