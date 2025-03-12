// Created by keta on 3/10/24.


#include <iostream>
#include <string>
#include <opencv2/opencv.hpp>
#include <ctime>

#include "auto_focus.h"

#include "../util/image_base.h"
#include "../util/data_file.h"


long AutoFocus::getCurrentUnixTimeInMillis() {
    using namespace std::chrono;
    auto now = system_clock::now();
    auto epoch = system_clock::from_time_t(0);  // Fixed line
    return duration_cast<milliseconds>(now - epoch).count();
}


void AutoFocus::calculateEdgeMap(const cv::Mat& image) {

    // Sobel transform
    cv::Mat gradientX, gradientY;
    cv::Sobel(image, gradientX, CV_16S, 1, 0);
    cv::Sobel(image, gradientY, CV_16S, 0, 1);
    cv::convertScaleAbs(gradientX, gradientX);
    cv::convertScaleAbs(gradientY, gradientY);

    // Append gradient maps
    cv::add(gradientX, gradientY, image);
}


void AutoFocus::cropImageBySquareAtPoint(
        cv::Mat& image, const int& squareSideSize, const float& pointX, const float& pointY) {

    // Get variables
    int imageWidth = image.cols;
    int imageHeight = image.rows;

    // Check image size, skip if incorrect
    if (imageWidth < squareSideSize + 2 || imageHeight < squareSideSize + 2) { return; }

    // Calculate square
    int squareX = pointX * imageWidth - squareSideSize / 2;
    int squareY = pointY * imageHeight - squareSideSize / 2;

    // Clamp square inside area
    if (squareX < 0) squareX = 0;
    if (squareY < 0) squareY = 0;
    if (squareX + squareSideSize > imageWidth) squareX = imageWidth - squareSideSize;
    if (squareY + squareSideSize > imageHeight) squareY = imageHeight - squareSideSize;

    // Crop
    cv::Rect roi(squareX, squareY, squareSideSize, squareSideSize);
    image = image(roi).clone();
}


void AutoFocus::calculateFocusDistance(
        const std::string& workingDirectoryPath, cv::Mat image) {

    // Load data
    float focusValue = DataFile::loadFloat(
            workingDirectoryPath + "/focusValue.txt", 0.0F);
    std::string focusingStage = DataFile::loadString(
            workingDirectoryPath + "/focusingStage.txt", "Idle");
    float bestFocusPoint = DataFile::loadFloat(
            workingDirectoryPath + "/bestFocusPoint.txt", 0.0F);
    float bestFocusPointValue = DataFile::loadFloat(
            workingDirectoryPath + "/bestFocusPointValue.txt", 0.0F);
    float firstFocusSectionGoodness = DataFile::loadFloat(
            workingDirectoryPath + "/firstFocusSectionGoodness.txt", 0.0F);
    float lastFocusSectionGoodness = DataFile::loadFloat(
            workingDirectoryPath + "/lastFocusSectionGoodness.txt", 0.0F);
    float worstFocusPointValue = DataFile::loadFloat(
            workingDirectoryPath + "/worstFocusPointValue.txt", 0.0F);
    long nextStepTime = DataFile::loadLong(
            workingDirectoryPath + "/nextStepTime.txt", 0L);
    int nextStepAfterFrames = DataFile::loadInt(
            workingDirectoryPath + "/nextStepAfterFrames.txt", 0);

    // Configuration
    int focusSquareSideSize = 384; // 384
    int focusingPrepareFrames = 4; // 8
    long focusStepFrames = 2; // 2
    float focusStep = 0.125; // 0.125
    float minimumBestWorstThreshold = 2.0;


    // Skip state
    bool isSkipIteration = false;

    // Start focusing

    if (focusingStage == "Start" && !isSkipIteration) {
        focusValue = 1.0F;
        bestFocusPoint = 0.0F;
        bestFocusPointValue = 0.0F;
        nextStepAfterFrames = focusingPrepareFrames;
        focusingStage = "Prepare";
        isSkipIteration = true;
    }

    // Prepare

    if (focusingStage == "Prepare" && !isSkipIteration) {

        // Check step frames
        if (nextStepAfterFrames > 0) { nextStepAfterFrames--;
            isSkipIteration = true; }

        // Start focusing
        if (!isSkipIteration) {
            focusingStage = "InProcess";
        }
    }

    // Focusing

    if (focusingStage == "InProcess" && !isSkipIteration) {

        // Check step frame
        if (nextStepAfterFrames > 0) { nextStepAfterFrames--;
            // Save counter
            DataFile::saveInt(nextStepAfterFrames,
                              workingDirectoryPath + "/nextStepAfterFrames.txt");
            // Exit
            return; }
        // Assign frames before next step
        nextStepAfterFrames = focusStepFrames;

        // Get grayscale image
        cv::cvtColor(image, image, cv::COLOR_BGR2GRAY);

        // Crop image by focus point
        float focusPointX = DataFile::loadFloat(
                workingDirectoryPath + "/focusPointX.txt", 0.5F);
        float focusPointY = DataFile::loadFloat(
                workingDirectoryPath + "/focusPointY.txt", 0.5F);
        cropImageBySquareAtPoint(image, focusSquareSideSize,
                                 focusPointX, focusPointY);

        // Calculate edge map
        calculateEdgeMap(image);

        // Calculate focus goodness
        int whitePixelsCount = 0;
        image.forEach<uchar>([&](uchar &pixel, const int *position) {
            if (pixel > 240.0) { whitePixelsCount++; }
        });

        // When new best focus point detected
        if (whitePixelsCount > bestFocusPointValue) {
            // Save point and value + revert 1 step (second same photo enter + change delay fix)
            bestFocusPoint = focusValue + focusStep;
            if ( bestFocusPoint > 1.0 ) { bestFocusPoint = 1.0; }
            bestFocusPointValue = whitePixelsCount;
        }

        // When new worst focus point detected
        if (whitePixelsCount < worstFocusPointValue || worstFocusPointValue == 0) {
            // Save worst point value
            worstFocusPointValue = whitePixelsCount;
        }

        // Move focus
        focusValue -= focusStep;

        // Assign value for first sections
        if (focusValue > 0.5) {
            firstFocusSectionGoodness = whitePixelsCount;
        }
        lastFocusSectionGoodness = whitePixelsCount;

        // Check end of focusing
        if (focusValue < 0.0) {
            focusingStage = "End";
        }

        // Clamp focus
        if (focusValue < 0.0F) { focusValue = 0.0F; }
        if (focusValue > 1.0F) { focusValue = 1.0F; }

        // Save log
        /*DataFile::saveString("Wellness: " + std::to_string(whitePixelsCount),
            workingDirectoryPath + "/Log.txt");*/
    }

    // End focusing

    if (focusingStage == "End" && !isSkipIteration) {
        // Assign focus point with check
        if (bestFocusPointValue != 0 &&
            bestFocusPoint > 0.08 &&
            (static_cast<float>(bestFocusPointValue + 1)
            / static_cast<float>(worstFocusPointValue + 1)) >= minimumBestWorstThreshold &&
            (bestFocusPoint <= 0.5 || firstFocusSectionGoodness > lastFocusSectionGoodness)) {
            // Assign best focus point as focus if autofocus is good
            focusValue = bestFocusPoint;
            //DataFile::saveString("YES", workingDirectoryPath + "/Log.txt");
        } else {
            // Infinity point if autofocus is not good
            focusValue = 0.0F;
            /*DataFile::saveString("NO,  BV: " + std::to_string(bestFocusPointValue) +
                "  WV: " + std::to_string(worstFocusPointValue) +
                "  P: " + std::to_string(bestFocusPoint) +
                "  FP: " + std::to_string(firstFocusSectionGoodness) +
                "  LP: " + std::to_string(lastFocusSectionGoodness),
                workingDirectoryPath + "/Log.txt");*/
        }
        focusingStage = "Idle";
    }


    // Save log
    /*DataFile::saveString("FStage " + focusingStage +
        "  F " + std::to_string(focusValue),
        workingDirectoryPath + "/Log.txt");*/

    // Save current data
    DataFile::saveFloat(
            focusValue, workingDirectoryPath + "/focusValue.txt");
    DataFile::saveString(
            focusingStage, workingDirectoryPath + "/focusingStage.txt");
    DataFile::saveFloat(
            bestFocusPoint, workingDirectoryPath + "/bestFocusPoint.txt");
    DataFile::saveFloat(
            bestFocusPointValue, workingDirectoryPath + "/bestFocusPointValue.txt");
    DataFile::saveFloat(firstFocusSectionGoodness,
                        workingDirectoryPath + "/firstFocusSectionGoodness.txt");
    DataFile::saveFloat(lastFocusSectionGoodness,
                        workingDirectoryPath + "/lastFocusSectionGoodness.txt");
    DataFile::saveFloat(
            worstFocusPointValue, workingDirectoryPath + "/worstFocusPointValue.txt");
    DataFile::saveLong(
            nextStepTime, workingDirectoryPath + "/nextStepTime.txt");
    DataFile::saveInt(
            nextStepAfterFrames, workingDirectoryPath + "/nextStepAfterFrames.txt");
}