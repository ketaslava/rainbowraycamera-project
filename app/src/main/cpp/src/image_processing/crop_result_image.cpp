// Created by keta on 3/26/24.

#include <iostream>
#include <string>
#include <opencv2/opencv.hpp>

#include "crop_result_image.h"
#include "../util/image_base.h"
#include "../util/data_file.h"


void assignNewMax(float& currentValue, const float& inputValue) {
    if (inputValue > currentValue) { currentValue = inputValue; } }
void assignNewMin(float& currentValue, const float& inputValue) {
    if (inputValue < currentValue) {currentValue = inputValue; } }


void CropResultImage::cropResultByPositionedImagesCorners(
        cv::Mat& targetImage, std::string workingDirectoryPath) {

    // Read image corners
    std::vector<std::vector<cv::Point2f>> positionedImagesCorners;
    DataFile::loadVectorVectorPoints2d(
            workingDirectoryPath + "/positionedImagesCorners.txt", positionedImagesCorners);

    // Calculate new image corners position

    // Create corners (X 0 Y 0 and X max Y max)
    float cornerZeroX = 0;
    float cornerZeroY = 0;
    auto cornerMaxX = static_cast<float>(targetImage.size[1]);
    auto cornerMaxY = static_cast<float>(targetImage.size[0]);

    // Process corners
    for (auto& imageCorners : positionedImagesCorners) {

        // Zero corner
        assignNewMax(cornerZeroX, imageCorners[0].x);
        assignNewMax(cornerZeroX, imageCorners[2].x);
        assignNewMax(cornerZeroY, imageCorners[0].y);
        assignNewMax(cornerZeroY, imageCorners[1].y);

        // Max corner
        assignNewMin(cornerMaxX, imageCorners[3].x);
        assignNewMin(cornerMaxX, imageCorners[1].x);
        assignNewMin(cornerMaxY, imageCorners[3].y);
        assignNewMin(cornerMaxY, imageCorners[2].y);
    }

    // Crop image by new corners position

    std::cout << "Crop image by new corners:" << std::endl;
    std::cout << "Min " << cornerZeroX << " , " << cornerZeroY << std::endl;
    std::cout << "Max " << cornerMaxX << " , " << cornerMaxY << std::endl;

    ImageBase::cropImageBy2Corners(
            targetImage,
            static_cast<int>(cornerZeroX),
            static_cast<int>(cornerZeroY),
            static_cast<int>(cornerMaxX) - 1,
            static_cast<int>(cornerMaxY) - 1
    );
}