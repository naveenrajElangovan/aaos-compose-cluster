package com.fordmx.cluster.di

import android.content.Context
import com.fordmx.cluster.data.repository.VehicleRepository
import com.fordmx.cluster.data.repository.VehicleRepositoryImpl

object AppModule {
    fun provideVehicleRepository(context: Context): VehicleRepository =
        VehicleRepositoryImpl(context)
}