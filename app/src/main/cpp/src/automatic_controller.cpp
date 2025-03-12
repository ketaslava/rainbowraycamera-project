// Created by keta on 3/8/24.

#include <iostream>
#include <string>
#include <opencv2/opencv.hpp>
#include <ctime>

#include "automatic_controller.h"

#include "util/image_base.h"
#include "util/data_file.h"
#include "automatic_controller/auto_focus.h"
#include "automatic_controller/auto_exposition.h"


std::string AutomaticController::processCaptureResult(const std::string& workingDirectoryPath) {

    // Load image
    cv::Mat image = imread(workingDirectoryPath + "/input.jpg", cv::IMREAD_COLOR);

    // Check image
    if (image.empty()) { return "EmptyImageException"; }

    // Use to calculate auto focus
    AutoFocus::calculateFocusDistance(workingDirectoryPath, image);

    // Use to calculate auto exposition
    AutoExposition::calculateExposition(workingDirectoryPath, image);

    return "Success";
}