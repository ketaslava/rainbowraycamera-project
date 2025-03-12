package com.ktvincco.rainbowraycamera.ui

import android.app.Activity
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ktvincco.rainbowraycamera.R
import com.ktvincco.rainbowraycamera.AppSettings
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler
import com.ktvincco.rainbowraycamera.ui.components.GeneralComponents

class AboutApp (
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private var generalComponents: GeneralComponents
){


    @Composable
    fun CloseButton() {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .height(uiScale.dp * 21)
                .padding(horizontal = uiScale.dp * 6)
                .clickable { uiEventHandler.closeAboutAppButtonClicked() }
        ) {
            generalComponents.ImageIcon1(
                R.drawable.icon_cross,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
            )
            Box(modifier = Modifier
                .width(uiScale.dp * 6)
                .height(uiScale.dp * 6))
            generalComponents.TextMain(text = "Exit")
        }
    }


    @Composable
    fun EnableFlashlight() {

        val isFlashlightEnabled = modelData.getCameraOptionStateBoolean(
            "isFlashlightOptionEnabled") == true

        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .height(uiScale.dp * 21)
                .padding(horizontal = uiScale.dp * 6)
                .clickable {
                    if (isFlashlightEnabled) {
                        uiEventHandler.toggleFlashlightOptionButtonClicked() }
                    else { modelData.openEnableFlashlightOption() }
                }
        ) {
            generalComponents.ImageIcon1(
                R.drawable.icon_flashlight,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
            )
            Box(modifier = Modifier
                .width(uiScale.dp * 6)
                .height(uiScale.dp * 6))
            if (isFlashlightEnabled) {
                generalComponents.TextMain(text = "Disable Flashlight")
            } else {
                generalComponents.TextMain(text = "Enable Flashlight")
            }
        }
    }


    @Composable
    fun TextAboutTheApp() {
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(uiScale.dp * 6)
        ) {
            generalComponents.TextMain(textAlign = TextAlign.Center,
                text = "About the App\n")
            generalComponents.TextMain(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                text = "RainbowRayCamera, or simply RRC, is a creation of the KTVINCCO team\n\n" +
                        "We've engineered an innovative project poised to revolutionize mobile photography, granting Android users the ability to capture better photos and videos on devices of any configuration and irrespective of hardware quality\n\n" +
                        "The primary aim of our application is to capture high-quality photos and videos in challenging conditions, such as darkness or intense backlighting\n\n" +
                        "RainbowRayCamera use many media processing algorithms to achieve a beautiful and realistic result")
        }
    }


    @Composable
    fun TextFromTheDevelopers() {
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(uiScale.dp * 6)
        ) {
            generalComponents.TextMain(textAlign = TextAlign.Center,
                text = "From the Developers\n")
            generalComponents.TextMain(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                text = "By Ketaslava Ket:\n" +
                        "I have been using Android smartphones all my life and believe that Android is the best OS for smartphones. Unfortunately, many smartphones have issues with photo and video quality. I always wanted to improve this, so my team KTVINCCO and I, developed this application for you")
        }
    }


    @Composable
    fun TextLicenses() {
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(uiScale.dp * 6)
        ) {
            generalComponents.TextMain(textAlign = TextAlign.Center,
                text = "Licenses\n")

            // About this project
            generalComponents.TextMain(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                text = "This project is available under the GNU General Public License.\n" +
                        "You can review license and source code here:")
            Spacer(modifier = Modifier.height(uiScale.dp * 4))
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                generalComponents.OpenWebLinkButton(
                    "GNU General Public License", "https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")
            }
            Spacer(modifier = Modifier.height(uiScale.dp * 4))
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                generalComponents.OpenWebLinkButton(
                    "Source Code", "https://github.com/ketaslava/rainbowraycamera-project")
            }
            Spacer(modifier = Modifier.height(uiScale.dp * 8))

            // About third party software

            generalComponents.TextMain(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                text = "Notice of licenses used in the project's technologies:\n" +
                        "\n" +
                        "OpenCV:\n" +
                        "NOTICE: This application uses OpenCV, which is licensed under the Apache License, Version 2.0.\n" +
                        "You can review license and source code here:")
            Spacer(modifier = Modifier.height(uiScale.dp * 4))
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                generalComponents.OpenWebLinkButton(
                    "Apache 2.0.", "https://www.apache.org/licenses/LICENSE-2.0")
                Spacer(modifier = Modifier.width(uiScale.dp * 4))
                generalComponents.OpenWebLinkButton(
                    "Source code", "https://github.com/opencv/opencv")
            }
            Spacer(modifier = Modifier.height(uiScale.dp * 4))

            generalComponents.TextMain(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                text = "OpenSans:\n" +
                        "NOTICE: This application uses the OpenSans font, which is licensed under the Apache License, Version 2.0.\n" +
                        "You can review license here:")
            Spacer(modifier = Modifier.height(uiScale.dp * 4))
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                generalComponents.OpenWebLinkButton(
                    "Apache 2.0.", "https://www.apache.org/licenses/LICENSE-2.0")
            }
        }
    }


    @Composable
    fun TextDevelopers() {
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(uiScale.dp * 6)
        ) {
            generalComponents.TextMain(textAlign = TextAlign.Center,
                text = "Developers\n")
            generalComponents.TextMain(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                text = "    * Ketaslava Ket\n" +
                        "    * KTVINCCO STUDIO")
        }
    }


    @Composable
    fun TextFeedback() {
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(uiScale.dp * 6)
        ) {
            generalComponents.TextMain(textAlign = TextAlign.Center,
                text = "Leave Feedback\n")
            generalComponents.TextMain(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                text = "If you encounter any issues with the application, or if you'd like to suggest an improvement, provide technical advice, or join our team, you can use the feedback form (in contacts) to submit your idea. This is the fastest option. We will definitely see your proposal and respond promptly")
        }
    }


    @Composable
    fun OfficialTextContacts() {
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(uiScale.dp * 6)
        ) {
            generalComponents.TextMain(modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center, text = "Official Contacts\n")

            generalComponents.TextMain(modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start, text = "Email: ktvincco@gmail.com\n")

            Spacer(modifier = Modifier.height(uiScale.dp * 5))
        }
    }


    @Composable
    fun TextContacts() {
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(uiScale.dp * 6)
        ) {
            generalComponents.TextMain(modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center, text = "Contacts\n")

            generalComponents.TextMain(modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start, text = "Website: http://ktvincco.com\n")
            generalComponents.OpenWebLinkButton(
                "ktvincco.com", "http://ktvincco.com")
            Spacer(modifier = Modifier.height(uiScale.dp * 5))

            generalComponents.TextMain(modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start, text = "Email: ktvincco@ktvincco.com\n" +
                        "Email2: ktvincco@gmail.com\n\n" +
                        "RainbowRayCamera Feedback Form:\n" )
            generalComponents.OpenWebLinkButton(
                "Leave feedback", "http://ktvincco.com/rainbowraycamera/feedback")
            Spacer(modifier = Modifier.height(uiScale.dp * 5))

            generalComponents.TextMain(modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start, text = "Write a review on the Play Market:\n")
            generalComponents.OpenWebLinkButton(
                "Leave review", "http://ktvincco.com")
            Spacer(modifier = Modifier.height(uiScale.dp * 5))
        }
    }


    @Composable
    fun TextTermsOfUseAndPrivacyPolicy() {
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(uiScale.dp * 6)
        ) {
            generalComponents.TextMain(textAlign = TextAlign.Center,
                text = "TermsOfUse and PrivacyPolicy\n")
            generalComponents.TextMain(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                text = "By using this application, you agree to our terms of use and privacy policy. You can review them here:\n")
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
        }
    }


    @Composable
    fun Bottom() {
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(uiScale.dp * 6)
        ) {
            generalComponents.TextMain(textAlign = TextAlign.Center,
                text = "Copyright KTVINCCO and Ketaslava Ket 2024\n")
            Row (
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                generalComponents.ImageIcon1(
                    R.drawable.logo_ktvincco_full,
                    modifier = Modifier
                        .width(uiScale.dp * 42)
                        .height(uiScale.dp * 26)
                )
            }
        }
    }


    @Composable
    fun AboutAppPage() {
        LazyColumn (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            items(1) {
                Box(modifier = Modifier.height(uiScale.dp * 6))
                CloseButton()
                EnableFlashlight()

                TextAboutTheApp()
                TextFromTheDevelopers()
                TextLicenses()
                TextDevelopers()
                TextFeedback()
                TextContacts()
                OfficialTextContacts()
                TextTermsOfUseAndPrivacyPolicy()
                Bottom()

                Box(modifier = Modifier.height(uiScale.dp * 40))
            }
        }
    }
}
