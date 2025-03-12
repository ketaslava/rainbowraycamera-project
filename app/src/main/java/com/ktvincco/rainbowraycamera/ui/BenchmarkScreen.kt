package com.ktvincco.rainbowraycamera.ui

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler
import com.ktvincco.rainbowraycamera.ui.components.GeneralComponents

class BenchmarkScreen (
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private var generalComponents: GeneralComponents
) {

    @SuppressLint("NotConstructor")
    @Composable
    fun BenchmarkScreen() {
        // Text
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ){
            generalComponents.TextMain(
                modifier = Modifier.width(uiScale.dp * 88),
                text = "Benchmarking\n\n" +
                        "The app runs tests to work with your hardware\n" +
                        "This can take a maximum of " +
                        "${modelData.maxBenchmarkTimeSec.collectAsState().value} seconds\n" +
                        "Current progress:\n\n" +
                        "${modelData.benchmarkProgressText.collectAsState().value}\n\n" +
                        "Please wait"
                        )
        }
    }

}