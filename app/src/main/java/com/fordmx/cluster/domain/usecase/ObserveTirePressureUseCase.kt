package com.fordmx.cluster.domain.usecase

import com.fordmx.cluster.data.model.TirePressure
import com.fordmx.cluster.data.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow

class ObserveTirePressureUseCase(
    private val repository: VehicleRepository
) {
    operator fun invoke(): Flow<TirePressure> = repository.observeTirePressure()
}