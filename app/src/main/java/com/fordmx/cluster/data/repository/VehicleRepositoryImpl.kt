package com.fordmx.cluster.data.repository

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.car.hardware.property.Subscription
import android.content.Context
import android.util.Log
import com.fordmx.cluster.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class VehicleRepositoryImpl(context: Context) : VehicleRepository {

    private val car = Car.createCar(context)!!
    private val mgr = car.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager

    private val TAG = "FordCluster.Repo"

    // ── Speed ─────────────────────────────────────────────────────
    override fun observeSpeed(): Flow<Float> = callbackFlow {
        val cb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                val mps = (value.value as? Float) ?: 0f
                trySend(mps * 3.6f)
            }
            override fun onErrorEvent(propId: Int, zone: Int) {
                Log.e(TAG, "Speed error zone=$zone")
            }
        }
        try {
            mgr?.subscribePropertyEvents(
                listOf(Subscription.Builder(VehiclePropertyIds.PERF_VEHICLE_SPEED)
                    .setUpdateRateHz(10f).build()),
                Runnable::run, cb
            )
        } catch (e: Exception) {
            Log.e(TAG, "Speed subscribe failed: ${e.message}")
        }
        awaitClose { }
    }.distinctUntilChanged()

    // ── RPM ───────────────────────────────────────────────────────
    override fun observeRpm(): Flow<Float> = callbackFlow {
        val cb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                trySend((value.value as? Float) ?: 0f)
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }
        try {
            mgr?.subscribePropertyEvents(
                listOf(Subscription.Builder(VehiclePropertyIds.ENGINE_RPM)
                    .setUpdateRateHz(10f).build()),
                Runnable::run, cb
            )
        } catch (e: Exception) {
            Log.e(TAG, "RPM subscribe failed: ${e.message}")
        }
        awaitClose { }
    }.distinctUntilChanged()

    // ── Gear ──────────────────────────────────────────────────────
    override fun observeGear(): Flow<String> = callbackFlow {
        val cb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                val g = (value.value as? Int) ?: 2048
                trySend(gearToString(g))
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }
        try {
            mgr?.subscribePropertyEvents(
                listOf(Subscription.Builder(VehiclePropertyIds.GEAR_SELECTION)
                    .setUpdateRateHz(2f).build()),
                Runnable::run, cb
            )
        } catch (e: Exception) {
            Log.e(TAG, "Gear subscribe failed: ${e.message}")
        }
        awaitClose { }
    }.distinctUntilChanged()

    // ── Fuel ──────────────────────────────────────────────────────
    override fun observeFuel(): Flow<Float> = callbackFlow {
        val cb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                val ml  = (value.value as? Float) ?: 0f
                val pct = (ml / 50000f * 100f).coerceIn(0f, 100f)
                trySend(pct)
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }
        try {
            mgr?.subscribePropertyEvents(
                listOf(Subscription.Builder(VehiclePropertyIds.FUEL_LEVEL)
                    .setUpdateRateHz(1f).build()),
                Runnable::run, cb
            )
        } catch (e: Exception) {
            Log.e(TAG, "Fuel subscribe failed: ${e.message}")
            trySend(75f)
        }
        awaitClose { }
    }.distinctUntilChanged()

    // ── Engine temp ───────────────────────────────────────────────
    override fun observeEngineTemp(): Flow<Float> = callbackFlow {
        val cb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                trySend((value.value as? Float) ?: 20f)
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }
        try {
            mgr?.subscribePropertyEvents(
                listOf(Subscription.Builder(VehiclePropertyIds.ENGINE_COOLANT_TEMP)
                    .setUpdateRateHz(1f).build()),
                Runnable::run, cb
            )
        } catch (e: Exception) {
            Log.e(TAG, "Temp subscribe failed: ${e.message}")
        }
        awaitClose { }
    }.distinctUntilChanged()

    // ── Odometer ──────────────────────────────────────────────────
    override fun observeOdometer(): Flow<Float> = callbackFlow {
        val cb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                val meters = (value.value as? Float) ?: 0f
                trySend(meters / 1000f)
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }
        try {
            mgr?.subscribePropertyEvents(
                listOf(Subscription.Builder(VehiclePropertyIds.PERF_ODOMETER)
                    .setUpdateRateHz(1f).build()),
                Runnable::run, cb
            )
        } catch (e: Exception) {
            Log.e(TAG, "Odometer subscribe failed: ${e.message}")
        }
        awaitClose { }
    }.distinctUntilChanged()

    // ── Doors ─────────────────────────────────────────────────────
    override fun observeDoors(): Flow<DoorState> = callbackFlow {
        var current = DoorState()

        fun update(areaId: Int, pos: Int) {
            val open = pos > 0
            current = when (areaId) {
                1  -> current.copy(frontLeft  = open)
                4  -> current.copy(frontRight = open)
                16 -> current.copy(rearLeft   = open)
                64 -> current.copy(rearRight  = open)
                else -> current
            }
            trySend(current)
        }

        val doorCb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                val pos = (value.value as? Int) ?: 0
                update(value.areaId, pos)
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }

        try {
            mgr?.subscribePropertyEvents(
                listOf(
                    Subscription.Builder(VehiclePropertyIds.DOOR_POS)
                        .setUpdateRateHz(2f).build()
                ),
                Runnable::run,
                doorCb
            )
        } catch (e: Exception) {
            Log.e(TAG, "Doors subscribe failed: ${e.message}")
        }
        awaitClose { }
    }
    // ── Warnings ──────────────────────────────────────────────────
    override fun observeWarnings(): Flow<WarningFlags> = callbackFlow {
        var current = WarningFlags()

        fun emit() { trySend(current) }

        fun boolCb(update: (Boolean) -> WarningFlags) =
            object : CarPropertyManager.CarPropertyEventCallback {
                override fun onChangeEvent(value: CarPropertyValue<*>) {
                    current = update((value.value as? Boolean) ?: false)
                    emit()
                }
                override fun onErrorEvent(propId: Int, zone: Int) {}
            }

        val props = listOf(
            VehiclePropertyIds.PARKING_BRAKE_ON    to boolCb { current.copy(handbrakeOn    = it) },
            VehiclePropertyIds.ABS_ACTIVE           to boolCb { current.copy(absActive       = it) },
            VehiclePropertyIds.TRACTION_CONTROL_ACTIVE to boolCb { current.copy(tractionActive = it) },
            VehiclePropertyIds.HAZARD_LIGHTS_SWITCH to boolCb { current.copy(hazardOn        = it) },
        )

        val turnCb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                val v = (value.value as? Int) ?: 0
                current = current.copy(turnLeft = v == 1, turnRight = v == 2)
                emit()
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }

        val seatbeltCb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                val buckled = (value.value as? Boolean) ?: false
                current = when (value.areaId) {
                    1 -> current.copy(seatbeltDriver    = buckled)
                    4 -> current.copy(seatbeltPassenger = buckled)
                    else -> current
                }
                emit()
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }

        try {
            props.forEach { (id, cb) ->
                mgr?.subscribePropertyEvents(
                    listOf(Subscription.Builder(id).setUpdateRateHz(2f).build()),
                    Runnable::run, cb
                )
            }
            mgr?.subscribePropertyEvents(
                listOf(Subscription.Builder(VehiclePropertyIds.TURN_SIGNAL_STATE)
                    .setUpdateRateHz(2f).build()),
                Runnable::run, turnCb
            )
            mgr?.subscribePropertyEvents(
                listOf(Subscription.Builder(VehiclePropertyIds.SEAT_BELT_BUCKLED)
                    .setUpdateRateHz(2f).build()),
                Runnable::run, seatbeltCb
            )
        } catch (e: Exception) {
            Log.e(TAG, "Warnings subscribe failed: ${e.message}")
        }
        awaitClose { }
    }

    // ── Tires ─────────────────────────────────────────────────────
    override fun observeTirePressure(): Flow<TirePressure> = callbackFlow {
        var current = TirePressure()

        val cb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                val psi = ((value.value as? Float) ?: 220f) * 0.145f
                current = when (value.areaId) {
                    1  -> current.copy(frontLeft  = psi)
                    4  -> current.copy(frontRight = psi)
                    16 -> current.copy(rearLeft   = psi)
                    64 -> current.copy(rearRight  = psi)
                    else -> current
                }
                trySend(current)
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }
        try {
            mgr?.subscribePropertyEvents(
                listOf(Subscription.Builder(VehiclePropertyIds.TIRE_PRESSURE)
                    .setUpdateRateHz(1f).build()),
                Runnable::run, cb
            )
        } catch (e: Exception) {
            Log.e(TAG, "Tires subscribe failed: ${e.message}")
        }
        awaitClose { }
    }

    // ── Helpers ───────────────────────────────────────────────────
    private fun gearToString(gear: Int): String = when (gear) {
        2048 -> "P"; 128 -> "R"; 256 -> "N"; 4 -> "D"; 8 -> "S"; else -> "D"
    }

    fun release() {
        car.disconnect()
    }
}