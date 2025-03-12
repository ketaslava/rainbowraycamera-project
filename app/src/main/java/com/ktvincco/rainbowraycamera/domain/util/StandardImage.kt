package com.ktvincco.rainbowraycamera.domain.util

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Size
import java.io.ByteArrayOutputStream
import kotlin.math.abs


class StandardImage (
    private val imageBitmap: Bitmap,
    private val rotationRelativeToNormal: Float,
    private val isMirrorRelativeToNormal: Boolean,
    private val isMirrorByVertical: Boolean
) {


    // Public


    fun getImageAsBitmap(): Bitmap { return imageBitmap }
    fun getImageSize(): Size { return Size(imageBitmap.width, imageBitmap.height) }
    fun getRotationRelativeToNormal(): Float { return rotationRelativeToNormal }


    fun getImageAsByteArray(): ByteArray {

        // Translate settings
        val format = Bitmap.CompressFormat.JPEG
        val quality = 100

        // translate Bitmap to ByteArray and return
        val outputStream = ByteArrayOutputStream()
        imageBitmap.compress(format, quality, outputStream)
        return outputStream.toByteArray()
    }


    fun cropImageByNewAspectRatio(newAspectRatio: Float): StandardImage {

        // Calculate new image size
        val originalAspectRatio = imageBitmap.width.toFloat() / imageBitmap.height.toFloat()

        val newWidth: Int
        val newHeight: Int

        if (originalAspectRatio > newAspectRatio) {
            newWidth = (imageBitmap.height * newAspectRatio).toInt()
            newHeight = imageBitmap.height
        } else {
            newWidth = imageBitmap.width
            newHeight = (imageBitmap.width / newAspectRatio).toInt()
        }

        // Calculate center crop
        val startX = (imageBitmap.width - newWidth) / 2
        val startY = (imageBitmap.height - newHeight) / 2

        // Crop
        val croppedBitmap = Bitmap.createBitmap(
            imageBitmap, startX, startY, newWidth, newHeight)

        return StandardImage(croppedBitmap, rotationRelativeToNormal,
            isMirrorRelativeToNormal, isMirrorByVertical)
    }


    fun getInSpaceNormalizedImage(): StandardImage {

        // Rotate image by rotationRelativeToNormal

        val matrix = Matrix()
        if (isMirrorRelativeToNormal) {
            if (isMirrorByVertical) { matrix.preScale(1.0f, -1.0f) }
            else { matrix.preScale(-1.0f, 1.0f) }
        }
        matrix.postRotate(rotationRelativeToNormal)

        return StandardImage(
            Bitmap.createBitmap(imageBitmap, 0, 0,
                imageBitmap.width, imageBitmap.height, matrix, true),
            0F, isMirrorRelativeToNormal = false, isMirrorByVertical = false)
    }


    fun applyFocusPeakEffect(): StandardImage {

        // Settings
        val scaleReductionFactor = 6
        val brightnessDeltaThreshold = 3330000

        // Create bitmap
        val width = imageBitmap.width / scaleReductionFactor
        val height = imageBitmap.height / scaleReductionFactor
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Calculate focus peaking image
        var flipFlopPixel = 0
        var isFlipPixel = false

        for (x in 0 until width - 2) {
            for (y in 0 until height - 2) {

                val position = Pair(x * scaleReductionFactor , y * scaleReductionFactor)
                val pixel = imageBitmap.getPixel(position.first, position.second)

                isFlipPixel = !isFlipPixel
                if (isFlipPixel) {
                    flipFlopPixel = imageBitmap.getPixel(position.first + 2, position.second)
                }

                val brightnessDelta = abs(pixel - flipFlopPixel)
                if (brightnessDelta > brightnessDeltaThreshold) {
                    resultBitmap.setPixel(x, y, Color.GREEN)
                }
            }
        }

        // Return result
        return StandardImage(resultBitmap, rotationRelativeToNormal,
            isMirrorRelativeToNormal, isMirrorByVertical)
    }
}