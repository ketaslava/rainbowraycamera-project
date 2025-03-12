// Created by keta on 3/11/24.


#include <iostream>
#include <string>
#include <opencv2/opencv.hpp>
#include <ctime>

#include "auto_exposition.h"

#include "../util/image_base.h"
#include "../util/data_file.h"


void AutoExposition::calculateExposition(
        const std::string& workingDirectoryPath, cv::Mat image) {

    // Load data
    int nextStepAfterFrames = DataFile::loadInt(
            workingDirectoryPath + "/nextStepAfterFrames.txt", 0);
    float iso = DataFile::loadInt(
            workingDirectoryPath + "/iso.txt", 100);
    float isoMin = DataFile::loadInt(
            workingDirectoryPath + "/isoMin.txt", 100);
    float isoMax = DataFile::loadInt(
            workingDirectoryPath + "/isoMax.txt", 1000);
    long shutterSpeed = DataFile::loadLong(
            workingDirectoryPath + "/shutterSpeed.txt", 16000000);
    long shutterSpeedMin = DataFile::loadLong(
            workingDirectoryPath + "/shutterSpeedMin.txt", 16000000);


    // Configuration
    int exposureStepFrames = 2;
    long shutterSpeedMax = 80000000; // 0.08 Sec
    double exposureCompensation = -64.0;
    double minimalBrightnessForScale = 16.0;

    // Check step frame
    if (nextStepAfterFrames > 0) { nextStepAfterFrames--;
        /* Save counter */ DataFile::saveInt(nextStepAfterFrames,
            workingDirectoryPath + "/nextStepAfterFrames.txt"); /* Exit */ return; }
    // Assign frames before next step
    nextStepAfterFrames = exposureStepFrames;

    // Get brightness
    cv::Scalar meanIntensity = mean(image);
    double brightness = (meanIntensity[0] + meanIntensity[1] + meanIntensity[2]) / 3.0;

    // Exit if image is absolute black
    if (brightness == 0.0) { return; }

    // Calculate exposure scale
    brightness -= exposureCompensation;
    if (brightness < minimalBrightnessForScale) { brightness = minimalBrightnessForScale; }
    //double brightnessDeltaScale = ((128.0 / brightness) / 2.0) + 0.5;
    double brightnessDeltaScale = 128.0 / brightness;

    // Apply exposure scale
    if (shutterSpeed != shutterSpeedMax) {
        // Correct shutterSpeed
        shutterSpeed = shutterSpeed * brightnessDeltaScale;
        iso = isoMin;
    } else {
        if (iso == isoMin && brightnessDeltaScale < 1.0) {
            shutterSpeed = shutterSpeedMax - 1;
        } else {
            // Correct iso
            iso = iso * brightnessDeltaScale;
            shutterSpeed = shutterSpeedMax;
        }
    }

    // Clamp iso
    if (iso < isoMin) { iso = isoMin; }
    if (iso > isoMax) { iso = isoMax; }
    // Clamp shutter speed
    if (shutterSpeed < shutterSpeedMin) { shutterSpeed = shutterSpeedMin; }
    if (shutterSpeed > shutterSpeedMax) { shutterSpeed = shutterSpeedMax; }

    // Calculate and save recommended night mode state
    DataFile::saveBool(shutterSpeed == shutterSpeedMax,
                      workingDirectoryPath + "/recommendedNightModeState.txt");


    // Save log
    /*DataFile::saveString("Iso: " + std::to_string(iso) +
        "  Shutter: " + std::to_string(shutterSpeed / 1000000000.0) +
        "  Delta: " + std::to_string(brightnessDeltaScale), workingDirectoryPath + "/Log.txt");*/

    // Save current data
    DataFile::saveInt(
            nextStepAfterFrames, workingDirectoryPath + "/nextStepAfterFrames.txt");
    DataFile::saveInt(
            iso, workingDirectoryPath + "/iso.txt");
    DataFile::saveLong(
            shutterSpeed, workingDirectoryPath + "/shutterSpeed.txt");
}