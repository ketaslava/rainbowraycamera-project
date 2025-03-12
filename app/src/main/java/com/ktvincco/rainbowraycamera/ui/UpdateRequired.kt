package com.ktvincco.rainbowraycamera.ui

import android.annotation.SuppressLint
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ktvincco.rainbowraycamera.AppSettings
import com.ktvincco.rainbowraycamera.ui.components.GeneralComponents


class UpdateRequired (
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private var generalComponents: GeneralComponents
) {
    @SuppressLint("NotConstructor")
    @Composable
    fun UpdateRequired() {
        // Text
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ){
            generalComponents.TextMain(modifier = Modifier.width(uiScale.dp * 88),
                text = "Update required\n\n" +
                        "To use this application, you need to install an update\n\n" +
                        "You can download the current version of the application\nusing Google Play Market\n\n")
            Spacer(modifier = Modifier.height(uiScale.dp * 4))
            generalComponents.OpenWebLinkButton(text = "Open Google PlayMarket",
                link = AppSettings().getLinkToTheApplicationInGooglePlayMarket())
        }
    }
}