package com.fordmx.cluster.domain.usecase

import com.fordmx.cluster.data.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow

class ObserveEngineTempUseCase(
    private val repository: VehicleRepository
) {
    operator fun invoke(): Flow<Float> = repository.observeEngineTemp()
}