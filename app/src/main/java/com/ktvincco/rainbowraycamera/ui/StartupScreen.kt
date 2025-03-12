package com.ktvincco.rainbowraycamera.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ktvincco.rainbowraycamera.R
import com.ktvincco.rainbowraycamera.AppSettings
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler
import com.ktvincco.rainbowraycamera.ui.components.GeneralComponents

class StartupScreen (
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private var generalComponents: GeneralComponents
) {


    @Composable
    private fun Checkbox(name: String, text: String) {

        var isCheckboxChecked by remember { mutableStateOf(false) }

        Box (
            modifier = Modifier
                .width(uiScale.dp * 88)
                .clickable {
                    // Switch and emit callback
                    isCheckboxChecked = !isCheckboxChecked
                    uiEventHandler.checkboxSwitched(name, isCheckboxChecked)
                }
        ){
            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box (
                    modifier = Modifier
                        .width(uiScale.dp * 9)
                        .height(uiScale.dp * 9)
                        .clickable {
                            // Switch and emit callback
                            isCheckboxChecked = !isCheckboxChecked
                            uiEventHandler.checkboxSwitched(name, isCheckboxChecked)
                        }
                ) {
                    var icon = R.drawable.icon_box
                    if (isCheckboxChecked) { icon = R.drawable.icon_box_checked }
                    generalComponents.ImageIcon1(icon,
                        modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(uiScale.dp * 5))
                generalComponents.TextMain(text = text, textAlign = TextAlign.Start)
            }
        }
    }


    @Composable
    private fun Page0() {
        // Text
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ){
            generalComponents.TextMain(
                text = "Welcome to RainbowRayCamera\n" +
                        "by KTVINCCO\n\n" +
                        "Thank you for choosing our application.\n" +
                        "Explore innovative mobile photography technologies right now!\n\n" +
                        "Press \"next\" to continue >")
        }
    }


    @Composable
    private fun Page1() {
        // Text
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ){
            generalComponents.TextMain(
                text = "To use this application\n\nyou must be over 18 years old")
            Spacer(modifier = Modifier.height(uiScale.dp * 8))
            Checkbox("OlderThan18", "I confirm that I am 18 years old or older")
        }
    }


    @Composable
    private fun Page2() {
        // Text
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ){
            generalComponents.TextMain(
                text = "To use this application you must read\n\n" +
                        "our Terms Of Use and Privacy Policy")
            Spacer(modifier = Modifier.height(uiScale.dp * 8))
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                generalComponents.OpenWebLinkButton(
                    "Terms Of Use", AppSettings().getTermsOfUseLink())
                Spacer(modifier = Modifier.width(uiScale.dp * 5))
                generalComponents.OpenWebLinkButton(
                    "Privacy Policy", AppSettings().getPrivacyPolicyLink())
            }
            Spacer(modifier = Modifier.height(uiScale.dp * 8))
            Checkbox("TermsOfUse", "I have read and accept the Terms of Use")
            Spacer(modifier = Modifier.height(uiScale.dp * 5))
            Checkbox("PrivacyPolicy", "I have read and accept the Privacy Policy")
        }
    }


    @Composable
    private fun Page3() {
        // Text
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ){
            generalComponents.TextMain(
                text = "All set!\n\n" +
                        "Enjoy using the application!\n\n" +
                        "Press \"Okay\" to continue")
        }
    }


    @Composable
    private fun NavigationBar() {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(uiScale.dp * 18)
                .background(Color(0xE51A1A1A))
        ) {

            val isCurrentBackButtonActive =
                modelData.isBackButtonActive.collectAsState().value
            var backButtonAlpha = 1F
            if (!isCurrentBackButtonActive) { backButtonAlpha = 0.33F }
            generalComponents.ImageIcon1(
                R.drawable.icon_arrow,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .rotate(180F)
                    .alpha(backButtonAlpha)
                    .clickable { uiEventHandler.backButtonClicked() }
            )

            val isConfirmButtonActive =
                modelData.isConfirmButtonActive.collectAsState().value
            var confirmButtonAlpha = 1F
            if (!isConfirmButtonActive) { confirmButtonAlpha = 0.33F }
            generalComponents.ImageIcon1(
                R.drawable.icon_confirm,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .alpha(confirmButtonAlpha)
                    .clickable { uiEventHandler.confirmButtonClicked() }
            )

            val isCurrentNextButtonActive =
                modelData.isNextButtonActive.collectAsState().value
            var nextButtonAlpha = 1F
            if (!isCurrentNextButtonActive) { nextButtonAlpha = 0.33F }
            generalComponents.ImageIcon1(
                R.drawable.icon_arrow,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .alpha(nextButtonAlpha)
                    .clickable { uiEventHandler.nextButtonClicked() }
            )
        }
    }


    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun StartupScreenPage() {

        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF202020))
        ) {

            // Page
            val currentPageIndex = modelData.currentPageIndex.collectAsState().value

            AnimatedVisibility(
                visible = currentPageIndex == 0,
                enter = scaleIn(animationSpec = tween(500)),
                exit = scaleOut(animationSpec = tween(500))
            ) {
                Page0()
            }
            AnimatedVisibility(
                visible = currentPageIndex == 1,
                enter = scaleIn(animationSpec = tween(500)),
                exit = scaleOut(animationSpec = tween(500))
            ) {
                Page1()
            }
            AnimatedVisibility(
                visible = currentPageIndex == 2,
                enter = scaleIn(animationSpec = tween(500)),
                exit = scaleOut(animationSpec = tween(500))
            ) {
                Page2()
            }
            AnimatedVisibility(
                visible = currentPageIndex == 3,
                enter = scaleIn(animationSpec = tween(500)),
                exit = scaleOut(animationSpec = tween(500))
            ) {
                Page3()
            }

            // View media controls
            Column (
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ){
                NavigationBar()
            }
        }
    }
}