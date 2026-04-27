package com.fordmx.cluster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.fordmx.cluster.di.ClusterViewModelFactory
import com.fordmx.cluster.presentation.mvi.ClusterIntent
import com.fordmx.cluster.presentation.screen.ClusterScreen
import com.fordmx.cluster.presentation.viewmodel.ClusterViewModel
import com.fordmx.cluster.ui.theme.FordClusterTheme

class ClusterActivity : ComponentActivity() {

    private val viewModel: ClusterViewModel by viewModels {
        ClusterViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
        }
        window.setDecorFitsSystemWindows(false)
        viewModel.onIntent(ClusterIntent.Connect)
        setContent {
            FordClusterTheme {
                ClusterScreen(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        viewModel.onIntent(ClusterIntent.Disconnect)
        super.onDestroy()
    }
}