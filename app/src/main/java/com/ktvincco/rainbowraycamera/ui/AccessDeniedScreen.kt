package com.ktvincco.rainbowraycamera.ui


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler
import com.ktvincco.rainbowraycamera.ui.components.GeneralComponents


class AccessDeniedScreen (
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private var generalComponents: GeneralComponents
) {

    private fun openAppPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", mainActivity.packageName, null)
        intent.data = uri
        mainActivity.startActivity(intent)
    }

    @Composable
    private fun OpenAppPermissionSettingsButton(text: String) {
        Box (
            modifier = Modifier
                .clickable {
                    openAppPermissionSettings()
                }
                .background(Color.White)
        ){
            generalComponents.TextMain(text = text, color = Color.Blue,
                modifier = Modifier.padding(uiScale.dp * 2))
        }
    }

    @Composable
    fun AccessDeniedPage() {
        // Text
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ){
            generalComponents.TextMain(modifier = Modifier.width(uiScale.dp * 88),
                text = "Access denied\n\n" +
                        "To use this application, you need to grant all required permissions.\n\n" +
                        "To grant permissions, OPEN THE APP PERMISSIONS SETTINGS and PROVIDE all necessary permissions, then RESTART THE APPLICATION.\n")
            OpenAppPermissionSettingsButton("Open Settings")
        }
    }
}