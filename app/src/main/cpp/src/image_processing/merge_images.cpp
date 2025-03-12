// Created by keta on 12/18/23.


#include <iostream>
#include <string>
#include "opencv2/opencv.hpp"

#include "merge_images.h"

#include "../util/image_base.h"


// Private


cv::Mat calculateEdgeMap(const cv::Mat& inputImage) {

    // Assign variable
    cv::Mat image = inputImage.clone();

    // Sobel transform
    cv::Mat gradientX, gradientY;
    cv::Sobel(image, gradientX, CV_16S, 1, 0);
    cv::Sobel(image, gradientY, CV_16S, 0, 1);

    // Calculate gradients
    cv::convertScaleAbs(gradientX, gradientX);
    cv::convertScaleAbs(gradientY, gradientY);

    // Append gradient maps
    cv::add(gradientX, gradientY, image);

    // Remove low gradients
    image.forEach<uchar>([&](uchar& pixel, const int* position) {
        if (pixel < 64.0) { pixel = 0.0; }
    });

    return image;
}


const void calculateMergeMask(
        const cv::Mat& baseImage, const cv::Mat& targetImage, cv::Mat& outputMask) {

    // Get grayscale image
    cv::Mat grayscaleTargetImage;
    cv::Mat grayscaleBaseImage;
    cv::cvtColor(targetImage, grayscaleTargetImage, cv::COLOR_BGR2GRAY);
    cv::cvtColor(baseImage, grayscaleBaseImage, cv::COLOR_BGR2GRAY);

    // Calculate edge mask
    cv::Mat edgeMask = calculateEdgeMap(grayscaleTargetImage);

    // Calculate difference mask
    cv::Mat differenceMask;
    cv::absdiff(grayscaleTargetImage, grayscaleBaseImage, differenceMask);
    differenceMask.forEach<uchar>([&](uchar &pixel, const int *position) {
        if (pixel < 32.0) { pixel = 0.0; } else { pixel = 255.0; }
    });

    // Combine masks
    cv::add(edgeMask, differenceMask, outputMask);

    // Apply smooth
    cv::GaussianBlur(outputMask, outputMask,
                     cv::Size(9, 9), 2.0);
}


// Public


void MergeImages::mergeFrameToCompositeImage(cv::Mat& targetImage, const cv::Mat& baseImage,
                                        const int& imageSerialNumber, cv::Mat& compositeImage,
                                        [[maybe_unused]] const std::string& workingDirectoryPath) {

    // Log
    std::cout << "Merge Frame to Composite image" << imageSerialNumber << std::endl;

    // Overlay images
    float overlayAlpha = 1.0F / (static_cast<float>(imageSerialNumber) + 1);
    ImageBase::overlayImages(compositeImage, targetImage,
                             overlayAlpha, compositeImage);
}


void MergeImages::mergeCompositeToBaseImage(cv::Mat& baseImage, cv::Mat& compositeImage,
        [[maybe_unused]] const std::string& workingDirectoryPath) {

    // Log
    std::cout << "Merge Composite to Base image" << std::endl;

    // Calculate merge mask
    cv::Mat mergeMask;
    calculateMergeMask(baseImage, compositeImage, mergeMask);

    // DEV // Log
    // cv::imwrite(workingDirectoryPath + "/mergeMaskBC.jpg", mergeMask);

    // Convert data type
    baseImage.convertTo(baseImage, CV_64FC3);
    compositeImage.convertTo(compositeImage, CV_64FC3);

    // Normalize the alpha mask to keep intensity between 0 and 1
    cv::cvtColor(mergeMask, mergeMask, cv::COLOR_GRAY2BGR);
    mergeMask.convertTo(mergeMask, CV_64FC3, 1.0/255.0);

    // Merge by mask
    cv::multiply(mergeMask, baseImage, baseImage);
    cv::multiply(cv::Scalar::all(1.0)-mergeMask, compositeImage, compositeImage);
    cv::add(baseImage, compositeImage, baseImage);

    // Convert to standard format
    baseImage.convertTo(baseImage, CV_8U);
}

