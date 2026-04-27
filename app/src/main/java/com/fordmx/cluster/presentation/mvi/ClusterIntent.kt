package com.fordmx.cluster.presentation.mvi

sealed class ClusterIntent {
    object Connect : ClusterIntent()
    object Disconnect : ClusterIntent()
}