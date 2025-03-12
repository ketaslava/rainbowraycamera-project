package com.ktvincco.rainbowraycamera.ui.components


import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ktvincco.rainbowraycamera.R
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler


class WhiteBalanceSelector (
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private var generalComponents: GeneralComponents
) {


    private fun calcPointValue(touchPositionX: Float, touchPositionY: Float,
                               controlAreaSizeScreenPart: Int): Pair<Float, Float> {
        val pointOne = screenResolution.first.toFloat() / 100 * controlAreaSizeScreenPart
        val newSizeX = generalComponents.mapFloatToNormalizedRange(
            touchPositionX, 0.0F, pointOne)
        val newSizeY = generalComponents.mapFloatToNormalizedRange(
            touchPositionY, 0.0F, pointOne)
        return Pair(newSizeX, newSizeY)
    }


    private fun calcPointerBoxSize(touchPosition: Pair<Float, Float>,
        controlAreaSizeScreenPart: Int, pointerSizeScreenPart: Int): Pair<Dp, Dp> {
        val cassp = controlAreaSizeScreenPart; val ppsp = pointerSizeScreenPart
        var newWidth = (cassp.dp * uiScale * touchPosition.first) + (ppsp.dp / 2 * uiScale)
        var newHeight = (cassp.dp * uiScale * touchPosition.second) + (ppsp.dp / 2 * uiScale)
        if (newWidth < ppsp.dp * uiScale) { newWidth = ppsp.dp * uiScale}
        if (newHeight < ppsp.dp * uiScale) { newHeight = ppsp.dp * uiScale}
        return Pair(newWidth, newHeight)
    }


    private fun normalizePair(input: Pair<Float, Float>): Pair<Float, Float> {
        var f = input.first
        var s = input.second
        if (f > 1F) { f = 1F }
        if (f < 0F) { f = 0F }
        if (s > 1F) { s = 1F }
        if (s < 0F) { s = 0F }
        return Pair(f, s)
    }


    @SuppressLint("NotConstructor")
    @Composable
    fun WhiteBalanceSelector(startValue: Pair<Float, Float>,
                             callback: (Pair<Float, Float>) -> Unit) {
        Row (
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(bottom = uiScale.dp * 4)
        ) {

            // Settings

            val controlAreaSizeScreenPart = 33
            val pointerSizeScreenPart = 4
            val preciseSelectorSpeedScale = 0.04F
            val posToValScale = screenResolution.first.toFloat() / 100 * controlAreaSizeScreenPart

            // Selector

            // Input raw
            var isWasInput by remember { mutableStateOf(false) }
            var touchPositionX by remember {
                mutableStateOf(posToValScale * startValue.first) }
            var touchPositionY by remember {
                mutableStateOf(posToValScale * startValue.second) }

            // Calc pointer position as value
            var pointerValue = calcPointValue(
                touchPositionX, touchPositionY,
                controlAreaSizeScreenPart
            )

            // Precise selector

            // Input raw for precise selector
            var isUsePSCorrection by remember { mutableStateOf(false) }
            var psCorrectionX by remember { mutableStateOf(0.0F) }
            var psCorrectionY by remember { mutableStateOf(0.0F) }
            var psCurrentCorrectionX by remember { mutableStateOf(0.0F) }
            var psCurrentCorrectionY by remember { mutableStateOf(0.0F) }
            var psTouchPositionX by remember { mutableStateOf(0.0F) }
            var psTouchPositionY by remember { mutableStateOf(0.0F) }
            var psLastTouchPositionX by remember { mutableStateOf(0.0F) }
            var psLastTouchPositionY by remember { mutableStateOf(0.0F) }

            // Calculate current precise selector correction
            psCurrentCorrectionX = (psTouchPositionX - psLastTouchPositionX) *
                    preciseSelectorSpeedScale / posToValScale
            psCurrentCorrectionY = (psTouchPositionY - psLastTouchPositionY) *
                    preciseSelectorSpeedScale / posToValScale

            // Apply correction from precise selector to pointer position
            if (isUsePSCorrection) {
                pointerValue = Pair(
                    pointerValue.first + psCorrectionX + psCurrentCorrectionX,
                    pointerValue.second + psCorrectionY + psCurrentCorrectionY
                )
            } else {
                // Reset precise selector
                psCorrectionX = 0F
                psCorrectionY = 0F
                psCurrentCorrectionX = 0F
                psCurrentCorrectionY = 0F
            }

            // Process data

            // Apply limit
            pointerValue = normalizePair(pointerValue)

            // Callback
            if (isWasInput) { callback(pointerValue) }

            // Calculate pointer position
            val pointerBoxSize = calcPointerBoxSize(pointerValue,
                controlAreaSizeScreenPart, pointerSizeScreenPart)


            Box(
                modifier = Modifier
                    .width(uiScale.dp * controlAreaSizeScreenPart)
                    .height(uiScale.dp * controlAreaSizeScreenPart)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // Change state
                            isWasInput = true
                            isUsePSCorrection = false
                            // Process touch position
                            touchPositionX = offset.x
                            touchPositionY = offset.y
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            // Change state
                            isWasInput = true
                            isUsePSCorrection = false
                            // Process touch position
                            touchPositionX = change.position.x
                            touchPositionY = change.position.y
                        }
                    }
            ) {
                generalComponents.ImageIcon1(
                    R.drawable.color_correction_gradients_rb_rg,
                    modifier = Modifier
                        .width(uiScale.dp * controlAreaSizeScreenPart)
                        .height(uiScale.dp * controlAreaSizeScreenPart)
                        .rotate(270F)
                )
                Box(modifier = Modifier
                    .width(pointerBoxSize.first)
                    .height(pointerBoxSize.second)
                ) {
                    Column (
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End,
                    ) {
                        generalComponents.ImageIcon1(
                            imageRes = R.drawable.pointer,
                            modifier = Modifier
                                .width(pointerSizeScreenPart.dp * uiScale)
                                .height(pointerSizeScreenPart.dp * uiScale)
                        )
                    }
                }
            }


            Box(
                modifier = Modifier
                    .width(uiScale.dp * controlAreaSizeScreenPart)
                    .height(uiScale.dp * controlAreaSizeScreenPart)
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->

                            // Change state
                            isWasInput = true
                            isUsePSCorrection = true

                            // Assign position
                            psTouchPositionX = change.position.x
                            psTouchPositionY = change.position.y

                            // Process consumed action
                            if (change.isConsumed ||
                                psLastTouchPositionX == 0F || psLastTouchPositionY == 0F) {

                                // Add current precise selector correction to pool
                                psCorrectionX += psCurrentCorrectionX
                                psCorrectionY += psCurrentCorrectionY

                                // Assign new last position
                                psLastTouchPositionX = psTouchPositionX
                                psLastTouchPositionY = psTouchPositionY
                            }
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .width(uiScale.dp * controlAreaSizeScreenPart)
                        .height(uiScale.dp * controlAreaSizeScreenPart)
                ) {
                    generalComponents.ImageIcon1(
                        R.drawable.icon_color_picker_drag_area,
                        modifier = Modifier
                            .width(uiScale.dp * controlAreaSizeScreenPart)
                            .height(uiScale.dp * controlAreaSizeScreenPart)
                            .alpha(0.75F)
                    )
                    generalComponents.ImageIcon1(
                        R.drawable.icon_color_picker_frame,
                        modifier = Modifier
                            .width(uiScale.dp * controlAreaSizeScreenPart)
                            .height(uiScale.dp * controlAreaSizeScreenPart)
                    )
                }
            }
        }
    }
}