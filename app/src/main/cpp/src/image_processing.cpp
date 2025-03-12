// Created by keta on 12/23/23.

#include <iostream>
#include <string>
#include <opencv2/opencv.hpp>

#include "image_processing.h"

#include "custom_logger.h"
#include "image_processing/make_composite_image.h"
#include "image_processing/auto_adjust_image.h"
#include "util/data_file.h"
#include "image_processing/crop_result_image.h"
#include "image_processing/merge_images.h"


ImageProcessing::ImageProcessing(std::string workingDirectoryPath) :
    workingDirectoryPath(std::move(workingDirectoryPath)) {}


std::string ImageProcessing::processImage() {

    // Log
    CustomLogger::logToFile("=============================================");
    CustomLogger::logToFile("Start process image collection !");
    CustomLogger::logToFile("Working directory path: " + workingDirectoryPath);

    // Initialize variables
    cv::Mat image;
    cv::Mat compositeImage;
    bool isCompoundImageCreated = false;

    // Get content type
    std::string contentType = DataFile::loadString(
            workingDirectoryPath + "/contentType.txt", "Photo");

    // Get images count
    int imagesCount = DataFile::loadInt(
            workingDirectoryPath + "/numberOfFiles.txt", 0);

    // Check empty collection EXC
    if (imagesCount < 2) { return "Error"; }


    // Read first (index 1) image in collection as base image
    image = cv::imread(workingDirectoryPath + "/1.jpg", cv::IMREAD_COLOR);

    // Create compound image use other images in collection
    if (imagesCount > 2) {
        bool result = MakeCompositeImage::getCompositeImage(
                compositeImage, image, workingDirectoryPath, imagesCount);
        if (result) { isCompoundImageCreated = true; }
    }

    // Normalize brightness
    AutoAdjustImage::normalizeBrightness(image);
    if (isCompoundImageCreated) { AutoAdjustImage::normalizeBrightness(compositeImage); }

    // Normalize shadows when night mode is disabled (not "NightPhoto")
    if (contentType == "Photo") {
        AutoAdjustImage::normalizeShadows(image);
        if (isCompoundImageCreated) { AutoAdjustImage::normalizeShadows(compositeImage); }
    }

    // Merge base and compound images
    if (isCompoundImageCreated) {
        // Log
        cv::imwrite(workingDirectoryPath + "/baseImage.jpg", image);
        cv::imwrite(workingDirectoryPath + "/compositeImage.jpg", compositeImage);
        // Merge
        MergeImages::mergeCompositeToBaseImage(image, compositeImage, workingDirectoryPath);
        // Crop result
        CropResultImage::cropResultByPositionedImagesCorners(image, workingDirectoryPath);
    }

    // Apply filters
    // Disabled // AutoAdjustImage::autoColorCorrection(image);

    // Save result
    cv::imwrite(workingDirectoryPath + "/result.jpg", image);

    return "Success";
}