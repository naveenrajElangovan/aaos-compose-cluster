package com.fordmx.cluster.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fordmx.cluster.data.repository.VehicleRepository
import com.fordmx.cluster.data.repository.VehicleRepositoryImpl
import com.fordmx.cluster.domain.usecase.*
import com.fordmx.cluster.presentation.mvi.ClusterEffect
import com.fordmx.cluster.presentation.mvi.ClusterIntent
import com.fordmx.cluster.presentation.mvi.ClusterState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClusterViewModel(
    private val repository: VehicleRepository,
    private val observeSpeed: ObserveSpeedUseCase,
    private val observeRpm: ObserveRpmUseCase,
    private val observeGear: ObserveGearUseCase,
    private val observeFuel: ObserveFuelUseCase,
    private val observeEngineTemp: ObserveEngineTempUseCase,
    private val observeOdometer: ObserveOdometerUseCase,
    private val observeDoors: ObserveDoorsUseCase,
    private val observeWarnings: ObserveWarningsUseCase,
    private val observeTirePressure: ObserveTirePressureUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ClusterState())
    val state: StateFlow<ClusterState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ClusterEffect>(extraBufferCapacity = 8)
    val effect: SharedFlow<ClusterEffect> = _effect.asSharedFlow()

    fun onIntent(intent: ClusterIntent) {
        when (intent) {
            is ClusterIntent.Connect    -> startObserving()
            is ClusterIntent.Disconnect -> engineOff()
        }
    }

    private fun startObserving() {
        viewModelScope.launch {
            observeSpeed().collect { speed ->
                reduce { copy(speedKmh = speed) }
                if (state.value.doorState.anyOpen && speed > 5f)
                    _effect.emit(ClusterEffect.DoorOpenWhileMoving(speed))
            }
        }
        viewModelScope.launch {
            observeRpm().collect { rpm ->
                reduce { copy(rpm = rpm) }
            }
        }
        viewModelScope.launch {
            observeGear().collect { gear ->
                reduce { copy(gear = gear) }
            }
        }
        viewModelScope.launch {
            observeFuel().collect { pct ->
                // Sync lowFuel into WarningFlags so WarningBar component also sees it
                reduce { copy(
                    fuelPercent = pct,
                    warnings    = warnings.copy(lowFuel = pct < 15f)
                )}
                if (pct < 15f) _effect.emit(ClusterEffect.LowFuelAlert)
            }
        }
        viewModelScope.launch {
            observeEngineTemp().collect { temp ->
                // Sync engineTempHigh into WarningFlags so WarningBar component also sees it
                reduce { copy(
                    engineTemp = temp,
                    warnings   = warnings.copy(engineTempHigh = temp > 100f)
                )}
                if (temp > 100f) _effect.emit(ClusterEffect.EngineOverheat)
            }
        }
        viewModelScope.launch {
            observeOdometer().collect { km ->
                reduce { copy(odometerKm = km) }
            }
        }
        viewModelScope.launch {
            observeDoors().collect { doors ->
                reduce { copy(doorState = doors) }
            }
        }
        viewModelScope.launch {
            observeWarnings().collect { flags ->
                val prev = state.value.warnings
                // Merge — preserve lowFuel/engineTempHigh derived from sensors
                reduce { copy(warnings = flags.copy(
                    lowFuel        = fuelPercent < 15f,
                    engineTempHigh = engineTemp > 100f
                ))}
                if (prev.seatbeltDriver && !flags.seatbeltDriver)
                    _effect.emit(ClusterEffect.SeatbeltUnbuckled)
            }
        }
        viewModelScope.launch {
            observeTirePressure().collect { tires ->
                reduce { copy(tirePressure = tires) }
                tires.criticalTire?.let { tire ->
                    _effect.emit(ClusterEffect.TirePressureLow(tire, tires.lowest))
                }
            }
        }
        reduce { copy(isConnected = true) }
    }

    // ── Engine off — reset all dynamic values to safe defaults ────────────────
    private fun engineOff() {
        reduce { copy(
            speedKmh   = 0f,
            rpm        = 0f,
            gear       = "P",
            engineTemp = 20f,
            warnings   = warnings.copy(
                handbrakeOn    = true,
                engineTempHigh = false,
                absActive      = false,
                tractionActive = false,
                turnLeft       = false,
                turnRight      = false,
                hazardOn       = false
            ),
            isConnected = false
        )}
        releaseRepository()
    }

    private fun reduce(reducer: ClusterState.() -> ClusterState) {
        _state.update { it.reducer() }
    }

    private fun releaseRepository() {
        (repository as? VehicleRepositoryImpl)?.release()
    }
}