// Created by keta on 2/8/24.


#include <iostream>
#include <string>
#include <opencv2/opencv.hpp>

#include "auto_adjust_image.h"


void AutoAdjustImage::normalizeBrightness(cv::Mat& image) {

    std::cout << "normalizeBrightness" << std::endl;

    // Scale brightness until top brightness level reach 256

    // Get grayscale image
    cv::Mat imageGrayscale;
    if (image.channels() > 1) {
        cv::cvtColor(image, imageGrayscale, cv::COLOR_BGR2GRAY);
    } else { imageGrayscale = image; }

    // Calculate histogram
    cv::Mat hist;
    int histSize = 256;
    float range[] = {0, 256};
    const float* histRange = {range};
    cv::calcHist(&imageGrayscale, 1, 0,
                 cv::Mat(), hist, 1, &histSize, &histRange);

    // Calculate brightness scale factor
    int brightPoint = 256;
    double brightSum = 0.0;
    double currentBrightSum = 0.0;
    for (int i = 0; i < hist.rows; ++i) {brightSum += hist.at<float>(i);}
    while (brightPoint > 0) {
        currentBrightSum += hist.at<float>(brightPoint);
        if (currentBrightSum > (brightSum / 128.0)) { break; }
        brightPoint -= 1;
    }
    double brightnessScaleFactor = 256.0 / brightPoint;

    // Scale image brightness
    if (brightPoint != 0 && brightPoint < 248) {
        cv::multiply(image, brightnessScaleFactor, image);
    }
}


void AutoAdjustImage::normalizeShadows(cv::Mat& image) {

    std::cout << "autoProcessShadows" << std::endl;

    // Scale image dark zones until average image brightness reach 128

    // Calculate brightness and brightness delta scale
    cv::Scalar meanIntensity = mean(image);
    double brightness = (meanIntensity[0] + meanIntensity[1] + meanIntensity[2]) / 3.0;
    double brightnessDeltaScale = 128.0 / brightness;

    // Only for dark images
    if (brightnessDeltaScale > 1.0) {
        const double brightnessDeltaSqrt = sqrt(brightnessDeltaScale - 1.0);
        // For each pixel
        image.forEach<cv::Vec3b>([&](cv::Vec3b& pixel, const int* position) {
            // Calculate brightness + threshold
            const double pixelBrightness = ((pixel[0] + pixel[1] + pixel[2]) / 765.0) + 0.5;
            // Only for dark pixels
            if (pixelBrightness < 1.0) {
                // Calculate correction
                const double scale = (1.0 - pow(pixelBrightness, 2));
                const double correction = 1.0 + (brightnessDeltaSqrt * scale);
                // Apply correction
                for (int i = 0; i < 3; ++i) {
                    const double pixelColor = pixel[i] * correction;
                    // Write or Clamped write (for remove artefacts)
                    if (pixelColor < 254.0) { pixel[i] = pixelColor; }
                    else { pixel[i] = 255.0; }
                }
            }
        });
    }
}


void AutoAdjustImage::autoColorCorrection(cv::Mat& image) {

    std::cout << "autoColorCorrection" << std::endl;

    // Transform to lab
    cv::Mat lab_image;
    cvtColor(image, lab_image, cv::COLOR_BGR2Lab);

    // Split by channels
    std::vector<cv::Mat> lab_planes;
    split(lab_image, lab_planes);

    // Process shadows with CLAHE
    cv::Ptr<cv::CLAHE> clahe = cv::createCLAHE();
    clahe->setClipLimit(0.5);
    clahe->apply(lab_planes[0], lab_planes[0]);

    // Merge, transform, write
    merge(lab_planes, lab_image);
    cv::Mat result;
    cvtColor(lab_image, image, cv::COLOR_Lab2BGR);

}

