package com.fordmx.cluster.domain.usecase

import com.fordmx.cluster.data.model.DoorState
import com.fordmx.cluster.data.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow

class ObserveDoorsUseCase (
    private val repository: VehicleRepository
) {
    operator fun invoke(): Flow<DoorState> = repository.observeDoors()
}