package com.fordmx.cluster.data.repository

import com.fordmx.cluster.data.model.*
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeSpeed(): Flow<Float>
    fun observeRpm(): Flow<Float>
    fun observeGear(): Flow<String>
    fun observeFuel(): Flow<Float>
    fun observeEngineTemp(): Flow<Float>
    fun observeOdometer(): Flow<Float>
    fun observeDoors(): Flow<DoorState>
    fun observeWarnings(): Flow<WarningFlags>
    fun observeTirePressure(): Flow<TirePressure>
}