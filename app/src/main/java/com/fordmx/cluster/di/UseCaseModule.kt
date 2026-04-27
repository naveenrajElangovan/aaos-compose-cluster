package com.fordmx.cluster.di

import com.fordmx.cluster.data.repository.VehicleRepository
import com.fordmx.cluster.domain.usecase.*

object UseCaseModule {
    fun provideUseCases(repository: VehicleRepository) = UseCases(
        observeSpeed        = ObserveSpeedUseCase(repository),
        observeRpm          = ObserveRpmUseCase(repository),
        observeGear         = ObserveGearUseCase(repository),
        observeFuel         = ObserveFuelUseCase(repository),
        observeEngineTemp   = ObserveEngineTempUseCase(repository),
        observeOdometer     = ObserveOdometerUseCase(repository),
        observeDoors        = ObserveDoorsUseCase(repository),
        observeWarnings     = ObserveWarningsUseCase(repository),
        observeTirePressure = ObserveTirePressureUseCase(repository)
    )
}

data class UseCases(
    val observeSpeed: ObserveSpeedUseCase,
    val observeRpm: ObserveRpmUseCase,
    val observeGear: ObserveGearUseCase,
    val observeFuel: ObserveFuelUseCase,
    val observeEngineTemp: ObserveEngineTempUseCase,
    val observeOdometer: ObserveOdometerUseCase,
    val observeDoors: ObserveDoorsUseCase,
    val observeWarnings: ObserveWarningsUseCase,
    val observeTirePressure: ObserveTirePressureUseCase
)