package com.fordmx.cluster.data.model

data class DoorState(
    val frontLeft: Boolean = false,
    val frontRight: Boolean = false,
    val rearLeft: Boolean = false,
    val rearRight: Boolean = false,
    val trunk: Boolean = false,
    val hood: Boolean = false
) {
    val anyOpen: Boolean
        get() = frontLeft || frontRight || rearLeft || rearRight || trunk || hood

    fun openDoorNames(): List<String> = buildList {
        if (frontLeft)  add("FL")
        if (frontRight) add("FR")
        if (rearLeft)   add("RL")
        if (rearRight)  add("RR")
        if (trunk)      add("TRUNK")
        if (hood)       add("HOOD")
    }
}