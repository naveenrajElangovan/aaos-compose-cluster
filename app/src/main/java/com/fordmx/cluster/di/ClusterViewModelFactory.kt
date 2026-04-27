package com.fordmx.cluster.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fordmx.cluster.data.repository.VehicleRepositoryImpl
import com.fordmx.cluster.domain.usecase.*
import com.fordmx.cluster.presentation.viewmodel.ClusterViewModel

class ClusterViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = VehicleRepositoryImpl(context)

        val viewModel = ClusterViewModel(
            repository            = repository,
            observeSpeed          = ObserveSpeedUseCase(repository),
            observeRpm            = ObserveRpmUseCase(repository),
            observeGear           = ObserveGearUseCase(repository),
            observeFuel           = ObserveFuelUseCase(repository),
            observeEngineTemp     = ObserveEngineTempUseCase(repository),
            observeOdometer       = ObserveOdometerUseCase(repository),
            observeDoors          = ObserveDoorsUseCase(repository),
            observeWarnings       = ObserveWarningsUseCase(repository),
            observeTirePressure   = ObserveTirePressureUseCase(repository)
        )
        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}