// Created by keta on 3/8/24.

#include <iostream>
#include <string>
#include <opencv2/opencv.hpp>

#include "rotateImage.h"

#include "util/image_base.h"


std::string RotateImage::rotateImage(std::string imagePath, int rotationDegrees) {

    // Check rotationDegrees
    if (rotationDegrees != 90 && rotationDegrees != -90 && rotationDegrees != 180
    &&rotationDegrees != 270 && rotationDegrees != -270) {
        return "Exception: Incorrect rotationDegrees";
    }

    try {

        // Load image
        cv::Mat image = imread(imagePath, cv::IMREAD_COLOR);

        // Rotate
        ImageBase::rotateImage(image, rotationDegrees);

        // Rewrite
        cv::imwrite(imagePath, image);

    } catch (...) {
        return "Error";
    }

    return "Success";
}