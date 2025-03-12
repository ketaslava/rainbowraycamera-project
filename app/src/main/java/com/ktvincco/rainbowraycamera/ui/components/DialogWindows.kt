package com.ktvincco.rainbowraycamera.ui.components


import com.ktvincco.rainbowraycamera.R
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


class DialogWindows (private var mainActivity: Activity, private var modelData: ModelData,
                     private var uiEventHandler: UiEventHandler, private val uiScale: Float,
                     private var generalComponents: GeneralComponents
) {


    @Composable
    fun PopupTop() {
        Row (
            modifier = Modifier
                .fillMaxSize()
                .padding(all = uiScale.dp * 2)
        ) {
            // Label icon
            val popupLabelType = modelData.popupLabelType.collectAsState().value
            var popupIcon = R.drawable.icon_info
            if (popupLabelType == "Warning") { popupIcon = R.drawable.icon_warning }
            if (popupLabelType == "Error") { popupIcon = R.drawable.icon_error }
            generalComponents.ImageIcon1(
                popupIcon,
                modifier = Modifier
                    .width(uiScale.dp * 12)
                    .height(uiScale.dp * 12))
            Box(
                modifier = Modifier
                    .width(uiScale.dp * 2)
            )
            // Label
            val popupLabel = modelData.popupLabel.collectAsState().value
            generalComponents.TextMain(text = popupLabel,
                containerModifier = Modifier.fillMaxSize())
        }
    }


    @Composable
    fun PopupBottom() {
        Column (
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(all = uiScale.dp * 2)
        ) {

            // Text

            val popupText = modelData.popupText.collectAsState().value
            generalComponents.TextMain(text = popupText,
                containerModifier = Modifier
                    .fillMaxWidth())

            // Buttons

            val popupButtonsType = modelData.popupButtonsType.collectAsState().value

            if (popupButtonsType == "Ok") {
                Box(
                    modifier = Modifier
                        .height(uiScale.dp * 9)
                        .width(uiScale.dp * 25)
                        .background(Color(0xCC1A1A1A))
                        .clickable { modelData.popupButtonClicked("Ok") }
                ) {
                    generalComponents.TextMain(
                        text = "OK",
                        containerModifier = Modifier
                            .matchParentSize()
                    )}
            }

            if (popupButtonsType == "YesOrNo") {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .height(uiScale.dp * 9)
                            .width(uiScale.dp * 25)
                            .background(Color(0xCC1A1A1A))
                            .clickable { modelData.popupButtonClicked("Yes") }
                    ) {
                        generalComponents.TextMain(
                            text = "Yes",
                            containerModifier = Modifier
                                .matchParentSize()
                        )}
                    Box(
                        modifier = Modifier
                            .height(uiScale.dp * 9)
                            .width(uiScale.dp * 25)
                            .background(Color(0xCC1A1A1A))
                            .clickable { modelData.popupButtonClicked("No") }
                    ) {
                        generalComponents.TextMain(
                            text = "No",
                            containerModifier = Modifier
                                .matchParentSize()
                        )}
                }
            }
        }
    }


    @Composable
    fun PopupPage() {
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .clickable { modelData.popupButtonClicked("Cancel") }
        ) {
            Box(
                modifier = Modifier
                    .height(uiScale.dp * 15)
                    .width(uiScale.dp * 65)
                    .background(Color(0xCC2C2C2C))
            ) {
                PopupTop()
            }
            Box(
                modifier = Modifier
                    .height(uiScale.dp * 50)
                    .width(uiScale.dp * 65)
                    .background(Color(0xCC1A1A1A))
            ) {
                PopupBottom()
            }
        }
    }


    @Composable
    fun SelectorOptionBlock(blockIndex: Int) {
        Box(
            modifier = Modifier
                .height(uiScale.dp * 10)
                .width(uiScale.dp * 65)
                .background(Color(0x801A1A1A))
                .clickable {
                    modelData.selectorOptionSelected(blockIndex)
                }
        ) {
            Row(
                modifier = Modifier
                    .matchParentSize()
            ){

                // Calculate selector point color
                var selectorPointColor = Color(0x80FFFFFF)
                if (modelData.selectorDefaultOption.collectAsState().value == blockIndex) {
                    selectorPointColor = Color(0xFFFFFFFF) }

                Box(
                    modifier = Modifier
                        .height(uiScale.dp * 10)
                        .width(uiScale.dp * 10)
                ) {
                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .height(uiScale.dp * 4)
                                .width(uiScale.dp * 4)
                                .background(selectorPointColor)
                        )
                    }
                }
                generalComponents.TextMain(
                    text = modelData.selectorOptions.collectAsState().value[blockIndex],
                    containerModifier = Modifier.fillMaxSize()
                )
            }
        }
    }


    @Composable
    fun Selector() {

        // Get selector options
        val selectorOptions = modelData.selectorOptions.collectAsState().value
        val selectorDefaultOption = modelData.selectorDefaultOption.collectAsState().value

        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x58000000))
                .clickable { modelData.selectorOptionSelected(selectorDefaultOption) }
        ) {
            Box(
                modifier = Modifier
                    .height(uiScale.dp * 12.5F)
                    .width(uiScale.dp * 65)
                    .background(Color(0x99333333))
            ) {
                Row (
                    modifier = Modifier
                        .matchParentSize()
                        .padding(all = uiScale.dp * 2)
                ) {
                    generalComponents.TextMain(
                        text = modelData.selectorLabel.collectAsState().value,
                        containerModifier = Modifier
                            .fillMaxSize())
                }
            }
            Box(
                modifier = Modifier
                    .height(uiScale.dp * 55)
                    .width(uiScale.dp * 65)
                    .background(Color(0x991A1A1A))
            ) {
                LazyColumn (
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .matchParentSize()
                ) {
                    items(selectorOptions.size) {blockIndex ->
                        SelectorOptionBlock(blockIndex)
                    }
                }
            }
        }
    }


    @Composable
    fun Selector2OptionBlock(blockIndex: Int) {
        Box(
            modifier = Modifier
                .height(uiScale.dp * 11)
                .width(uiScale.dp * 65)
                .background(Color(0x991A1A1A))
                .clickable {
                    modelData.selector2OptionSelected(blockIndex)
                }
        ) {
            Row(
                modifier = Modifier
                    .matchParentSize()
            ){

                // Calculate selector point color
                var selectorPointColor = Color(0x80FFFFFF)
                if (modelData.selectorDefaultOption.collectAsState().value == blockIndex) {
                    selectorPointColor = Color(0xFFFFFFFF) }

                Box(
                    modifier = Modifier
                        .height(uiScale.dp * 10)
                        .width(uiScale.dp * 10)
                ) {
                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .height(uiScale.dp * 4)
                                .width(uiScale.dp * 4)
                                .background(selectorPointColor)
                        )
                    }
                }
                generalComponents.TextMain(
                    text = modelData.selectorOptions.collectAsState().value[blockIndex],
                    containerModifier = Modifier.fillMaxSize()
                )
            }
        }
    }


    @Composable
    fun Selector2() {

        // Get selector options
        val selectorOptions = modelData.selectorOptions.collectAsState().value
        val selectorDefaultOption = modelData.selectorDefaultOption.collectAsState().value

        Column (
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x7F000000))
                .clickable { modelData.selector2OptionSelected(selectorDefaultOption) }
        ) {
            Box(
                modifier = Modifier
                    .height(uiScale.dp * 11)
                    .width(uiScale.dp * 65)
                    .background(Color(0xDB333333))
            ) {
                Row (
                    modifier = Modifier
                        .matchParentSize()
                        .padding(all = uiScale.dp * 2)
                ) {
                    generalComponents.TextMain(
                        text = modelData.selectorLabel.collectAsState().value,
                        containerModifier = Modifier
                            .fillMaxSize())
                }
            }
            Box(
                modifier = Modifier
                    .height(uiScale.dp * 70)
                    .width(uiScale.dp * 65)
                    .background(Color(0xDB1A1A1A))
            ) {
                LazyColumn (
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .matchParentSize()
                ) {
                    items(selectorOptions.size) {blockIndex ->
                        Selector2OptionBlock(blockIndex)
                    }
                }
            }
        }
    }


    @Composable
    fun CameraModeSelector() {

        // Get selector options
        val selectorDefaultOption = modelData.selectorDefaultOption.collectAsState().value

        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
                .padding(all = uiScale.dp * 6)
                .clickable {
                    modelData.cameraModeSelectorOptionSelected(selectorDefaultOption)
                }
        ) {

            // Center point
            generalComponents.ImageIcon1(
                R.drawable.icon_cross,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable {
                        modelData.cameraModeSelectorOptionSelected(selectorDefaultOption)
                    }
            )

            // Items to place
            val items = listOf(
                R.drawable.icon_photo_camera,
                R.drawable.icon_sun,
                R.drawable.icon_night,
                R.drawable.icon_photo_camera_manual,
                R.drawable.icon_video_camera,
                R.drawable.icon_video_camera_manual
            )
            val radius = uiScale.dp * 128
            val startAngle = 270f
            val endAngle = 180f

            // Place items
            items.forEachIndexed { index, item ->
                val angle = startAngle + (endAngle - startAngle) / (items.size - 1) * index
                val radians = Math.toRadians(angle.toDouble())
                val x = radius.value * cos(radians).toFloat()
                val y = radius.value * sin(radians).toFloat()

                generalComponents.ImageIcon1(
                    item,
                    modifier = Modifier
                        .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                        .width(uiScale.dp * 9)
                        .height(uiScale.dp * 9)
                        .clickable {
                            modelData.cameraModeSelectorOptionSelected(index)
                        }
                )
            }
        }
    }


    @Composable
    fun SelectorVideoSizeBlock(
        blockIndex: Int, optionsArray: List<String>,
        defaultIndex: Int, callback: (selectedIndex: Int) -> Unit) {
        Box(
            modifier = Modifier
                .height(uiScale.dp * 10)
                .width(uiScale.dp * 65)
                .background(Color(0x991A1A1A))
                .clickable {
                    callback(blockIndex)
                }
        ) {
            Row(
                modifier = Modifier
                    .matchParentSize()
            ){

                // Calculate selector point color
                var selectorPointColor = Color(0x80FFFFFF)
                if (blockIndex == defaultIndex) { selectorPointColor = Color(0xFFFFFFFF) }

                Box(
                    modifier = Modifier
                        .height(uiScale.dp * 10)
                        .width(uiScale.dp * 10)
                ) {
                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .height(uiScale.dp * 4)
                                .width(uiScale.dp * 4)
                                .background(selectorPointColor)
                        )
                    }
                }
                generalComponents.TextMain(
                    text = optionsArray[blockIndex],
                    containerModifier = Modifier.fillMaxSize()
                )
            }
        }
    }


    @Composable
    fun SelectorVideoSize() {

        // Get selector options
        val videoSizes = modelData.videoSizes.collectAsState().value
        val defaultSizeIndex = modelData.defaultVideoSizeIndex.collectAsState().value
        val frameRates = modelData.availableVideoFrameRatesBySizeMap.collectAsState().value
        val defaultFrameRateIndex = modelData.defaultVideoFrameRateIndex.collectAsState().value

        // Selection process
        var currentSizeIndex by remember { mutableStateOf(defaultSizeIndex) }
        var currentFrameRateIndex by remember { mutableStateOf(defaultFrameRateIndex) }

        // Process selection
        // Get available frame rates for selected size
        val currentSizeFrameRates = frameRates[videoSizes[currentSizeIndex]] ?: arrayListOf(30)
        // When size changed select 60 fps if available or 30 fps or index 0
        var lastSizeIndex by remember { mutableStateOf(defaultSizeIndex) }
        if (currentSizeIndex != lastSizeIndex) {
            currentFrameRateIndex = if (currentSizeFrameRates.contains(60)) {
                currentSizeFrameRates.indexOf(60) } else if (currentSizeFrameRates.contains(30)) {
                currentSizeFrameRates.indexOf(30) } else { 0 }
            // Assign new last index
            lastSizeIndex = currentSizeIndex
        }

        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x58000000))
                .clickable { modelData.selectorVideoSizeSelected(
                    defaultSizeIndex, defaultFrameRateIndex) }
        ) {
            Box(
                modifier = Modifier
                    .height(uiScale.dp * 12.5F)
                    .width(uiScale.dp * 100)
                    .background(Color(0x99333333))
            ) {
                Row (
                    modifier = Modifier
                        .matchParentSize()
                        .padding(all = uiScale.dp * 2)
                ) {
                    generalComponents.TextMain(
                        text = modelData.selectorLabel.collectAsState().value,
                        containerModifier = Modifier
                            .fillMaxSize())
                }
            }
            Box(
                modifier = Modifier
                    .height(uiScale.dp * 55)
                    .width(uiScale.dp * 100)
                    .background(Color(0x991A1A1A))
            ) {
                Row (
                    modifier = Modifier.matchParentSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    LazyColumn (
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight().width(uiScale.dp * 50)
                    ) {
                        items(videoSizes.size) {blockIndex ->
                            // Transform data to target format
                            val stringList: List<String> = videoSizes.map { "$it px" }
                            // Show column
                            SelectorVideoSizeBlock(blockIndex, stringList, currentSizeIndex) {
                                currentSizeIndex = it
                            }
                        }
                    }
                    LazyColumn (
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight().width(uiScale.dp * 50)
                    ) {
                        // Transform data to target format
                        val stringList: List<String> = currentSizeFrameRates.map { "$it FPS" }
                        // Show column
                        items(currentSizeFrameRates.size) {blockIndex ->
                            SelectorVideoSizeBlock(blockIndex, stringList, currentFrameRateIndex) {
                                currentFrameRateIndex = it
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .height(uiScale.dp * 12.5F)
                    .width(uiScale.dp * 100)
                    .background(Color(0x99333333))
                    .clickable { modelData.selectorVideoSizeSelected(
                        currentSizeIndex, currentFrameRateIndex) }
            ) {
                Row (
                    modifier = Modifier
                        .matchParentSize()
                        .padding(all = uiScale.dp * 2)
                        .background(Color(0x99333333))
                ) {
                    generalComponents.TextMain(
                        text = "Ok",
                        containerModifier = Modifier
                            .fillMaxSize())
                }
            }
        }
    }
}


