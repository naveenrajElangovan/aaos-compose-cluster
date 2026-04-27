# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Project Overview

This is a production-grade Android Automotive OS (AAOS) instrument cluster application for Ford Mexico, running on Display 1 (400×600px) of a Cuttlefish emulator. The architecture follows a "build once, brand many" approach using a single AOSP base with per-OEM Runtime Resource Overlays (RRO).

# Architecture Pattern

The application follows MVI (Model-View-Intent) architecture with Clean Architecture principles:

- **Intent** → `ClusterIntent` (Connect/Disconnect)
- **State** → `ClusterState` (immutable data class with derived computed properties)
- **Effect** → `ClusterEffect` (one-shot side effects via `SharedFlow`)
- **ViewModel** → collects 9 VHAL flows, merges into single state via `reduce()`
- **Repository** → `callbackFlow` wrapping `CarPropertyManager.subscribePropertyEvents()`
- **Use Cases** → thin wrappers delegating to repository, one per VHAL property

# Key Components

## Data Layer
- `VehicleRepository` interface and `VehicleRepositoryImpl` implementation
- VHAL property subscriptions using `CarPropertyManager`
- Models: `DoorState`, `TirePressure`, `WarningFlags`, `VehicleData`

## Domain Layer
- 9 use cases, each implementing a specific VHAL property observation:
  - `ObserveSpeedUseCase`
  - `ObserveRpmUseCase`
  - `ObserveGearUseCase`
  - `ObserveFuelUseCase`
  - `ObserveEngineTempUseCase`
  - `ObserveOdometerUseCase`
  - `ObserveDoorsUseCase`
  - `ObserveWarningsUseCase`
  - `ObserveTirePressureUseCase`

## Presentation Layer
- `ClusterViewModel` implementing MVI pattern with 9 coroutine collectors
- `ClusterScreen` Compose UI with components:
  - SpeedometerGauge.kt
  - RpmGauge.kt
  - FuelRing.kt
  - SweepArcs.kt
  - DoorWarningOverlay.kt
  - WarningBar.kt
  - GearSelector.kt

## Dependency Injection
- Manual DI via `ClusterViewModelFactory` (no Hilt due to AOSP build constraints)
- `AppModule` and `UseCaseModule` for dependency configuration

# VHAL Properties & Permissions

The application uses various Android Automotive VHAL properties with specific permissions:
- `PERF_VEHICLE_SPEED`, `ENGINE_RPM`, `GEAR_SELECTION`, `FUEL_LEVEL`, `ENGINE_COOLANT_TEMP`, `PERF_ODOMETER`, `DOOR_POS`, etc.
- All required permissions are declared in AndroidManifest.xml and granted via privapp-permissions XML file

# Development Workflow

## Build System
- AOSP builds using `mmm` command in Lima VM
- Build target: `aosp_cf_x86_64_auto_md-trunk_staging-userdebug`
- Deployment to Cuttlefish emulator using ADB commands

## Key ADB Commands
- `adb connect localhost:6520` to connect to emulator
- `adb -s localhost:6520 shell cmd car_service emulate-driving-state drive` to set driving state
- `adb -s localhost:6520 shell am start --display 1 -n com.fordmx.cluster/com.fordmx.cluster.ClusterActivity` to launch on Display 1

## Testing
- Unit tests for use cases and repository logic
- Integration tests for ViewModel behavior
- Manual testing with vehicle simulator (`ford_simulation.py`)

## Deployment
1. Build in Lima VM using `mmm packages/apps/FordCluster`
2. Copy APK to Mac: `limactl copy android:/home/naveenrajelangovan.guest/aosp/out/target/product/vsoc_x86_64_only/system/priv-app/FordCluster/FordCluster.apk ~/Desktop/FordCluster-platform-signed.apk`
3. Deploy to Cuttlefish: `adb -s localhost:6520 push ~/Desktop/FordCluster-platform-signed.apk /system/priv-app/FordCluster/FordCluster.apk`
4. Restart system: `adb -s localhost:6520 shell stop && sleep 5 && adb -s localhost:6520 shell start`

# Special Considerations

- No Hilt/Dagger support due to AOSP build constraints
- `DOOR_POS` is used instead of `DOOR_OPEN`/`TRUNK_OPEN` (area-based INT values)
- Warning flags are synchronized between derived values and sensor values
- All permissions are granted via privapp whitelist XML at boot time
- Multi-OEM architecture with RRO overlays for different OEM branding