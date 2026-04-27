package com.fordmx.cluster.domain.usecase

import com.fordmx.cluster.data.model.WarningFlags
import com.fordmx.cluster.data.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow

class ObserveWarningsUseCase(
    private val repository: VehicleRepository
) {
    operator fun invoke(): Flow<WarningFlags> = repository.observeWarnings()
}