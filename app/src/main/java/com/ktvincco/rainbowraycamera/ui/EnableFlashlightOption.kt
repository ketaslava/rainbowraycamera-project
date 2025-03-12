package com.ktvincco.rainbowraycamera.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler
import com.ktvincco.rainbowraycamera.ui.components.GeneralComponents

class EnableFlashlightOption(
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private var generalComponents: GeneralComponents
){


    @Composable
    fun EnableFlashlightOptionPage() {
        Column (
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red)
        ) {
            Column (
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(uiScale.dp * 150)
                    .padding(uiScale.dp * 12)
            ) {
                generalComponents.TextMain( textSize = 24.sp, text =
                    "Enable flashlight\noption WARNING"
                )
                generalComponents.TextMain( textSize = 18.sp, text =
                    "\nAttention! Enabling the flash can significantly degrade the quality of your photos. Turning on the flash may lead to:"
                )
                generalComponents.TextMain( textSize = 18.sp, textAlign = TextAlign.Start, text =
                    "\n* Overexposure\n* Color distortion\n* Unrealistic appearance of the photo\n* Glare and haze in the photo"
                )
                generalComponents.TextMain( textSize = 20.sp, text =
                    "\nDo you want to enable flashlight option?"
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = uiScale.dp * 9)
            ) {
                Box(
                    modifier = Modifier
                        .width(uiScale.dp * 30)
                        .height(uiScale.dp * 12)
                        .background(Color.White)
                        .clickable { modelData.openAboutApp()
                            uiEventHandler.toggleFlashlightOptionButtonClicked() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        generalComponents.TextMain(
                            text = "Yes", color = Color.Black
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(uiScale.dp * 30)
                        .height(uiScale.dp * 12)
                        .background(Color.White)
                        .clickable { modelData.openAboutApp() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        generalComponents.TextMain(
                            text = "No", color = Color.Black
                        )
                    }
                }
            }
        }
    }


}