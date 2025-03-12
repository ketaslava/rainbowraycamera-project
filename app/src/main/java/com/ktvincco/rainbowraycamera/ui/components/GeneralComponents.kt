package com.ktvincco.rainbowraycamera.ui.components

import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.google.android.exoplayer2.text.webvtt.WebvttCssStyle.FontSizeUnit


class GeneralComponents (
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private val screenResolutionInDp: Pair<Float, Float>,
) {


    @Composable
    fun TextMain(
        text: String,
        modifier: Modifier = Modifier,
        containerModifier: Modifier = Modifier,
        color: Color = Color.White,
        textSize: TextUnit = TextUnit.Unspecified,
        textAlign: TextAlign = TextAlign.Center
    ) {
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = containerModifier
        ) {
            Text(
                text = text,
                modifier = modifier,
                color = color,
                fontSize = textSize,
                textAlign = textAlign
            )
        }
    }


    @Composable
    fun ImageIcon1(
        imageRes: Int,
        modifier: Modifier
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    }


    @Composable
    fun OpenWebLinkButton(text: String, link: String) {
        Box (
            modifier = Modifier
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    mainActivity.startActivity(intent)
                }
                .background(Color.White)
        ){
            TextMain(text = text, color = Color.Blue,
                modifier = Modifier.padding(uiScale.dp * 2))
        }
    }


    @Composable
    fun Slider1(onStartValue: Float, onChangeSliderValue: (newValue: Float) -> Unit,
                isShowValue: Boolean = true) {

        // Settings
        val workZoneStart = 10
        val workZoneEnd = 90

        // Slider value
        var sliderValue by remember { mutableStateOf(onStartValue) }

        // New start value
        var lastStartValue by remember { mutableStateOf(onStartValue) }
        if (onStartValue != lastStartValue) {
            sliderValue = onStartValue
            lastStartValue = onStartValue
        }

        // Callback
        onChangeSliderValue(sliderValue)

        Box(
            modifier = Modifier
                .height(uiScale.dp * 12)
                .fillMaxWidth()
                .background(Color(0x991A1A1A))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val touchPosition = Pair(change.position.x, change.position.y)
                        val touchPositionX = touchPosition.first
                        val screenResolutionX = screenResolution.first / 100.0F
                        sliderValue = mapFloatToNormalizedRange(touchPositionX,
                            workZoneStart * (screenResolutionX),
                            workZoneEnd * (screenResolutionX))
                    }
                }
        ) {
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .height(uiScale.dp * 4)
                            .width(uiScale.dp * (100 - (workZoneStart + (100 - workZoneEnd))))
                            .background(Color(0x80FFFFFF))
                    ){
                        if (isShowValue) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(
                                        uiScale.dp * sliderValue *
                                                (100 - (workZoneStart + (100 - workZoneEnd)))
                                    )
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }
        }
    }
    fun mapFloatToNormalizedRange(value: Float, startRange: Float, endRange: Float): Float {
        var newValue = value
        if (newValue < startRange) {
            newValue = startRange
        }
        if (newValue > endRange) {
            newValue = endRange
        }

        val range = endRange - startRange
        return (newValue - startRange) / range
    }
}