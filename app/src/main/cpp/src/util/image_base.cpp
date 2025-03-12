// Created by keta on 12/17/23.

#include <string>
#include "opencv2/opencv.hpp"

#include "image_base.h"


// Resize cv::Mat image
void ImageBase::resizeFitMatImage(cv::Mat& inputImage, const cv::Size& newSize) {

    // Get aspect ratio
    double aspectRatio = static_cast<double>(inputImage.cols) / inputImage.rows;

    // crop if new aspect ratio
    if (aspectRatio != static_cast<double>(newSize.width) / newSize.height) {

        // Select minimal aspect ratio
        double targetAspectRatio = std::min(static_cast<double>(newSize.width) / newSize.height,
                                            static_cast<double>(inputImage.cols) / inputImage.rows);

        // Calc crop
        int targetWidth = static_cast<int>(targetAspectRatio * inputImage.rows);
        int targetHeight = static_cast<int>(targetWidth / targetAspectRatio);
        int startX = (inputImage.cols - targetWidth) / 2;
        int startY = (inputImage.rows - targetHeight) / 2;

        // Crop
        cv::Rect roi(startX, startY, targetWidth, targetHeight);
        inputImage = inputImage(roi);
    }

    // Resize
    cv::resize(inputImage, inputImage, cv::Size(newSize.width, newSize.height));
}


// Merge 2 images with alpha
void ImageBase::overlayImages(const cv::Mat& background, const cv::Mat& overlay,
                              double alpha, cv::Mat& output)
{
    // Checks
    if (background.size() != overlay.size()) {
        std::cerr << "Error: Images must have the same dimensions." << std::endl; return; }
    if (background.empty() || overlay.empty()) {
        std::cerr << "Error: Images cannot be empty." << std::endl; return; }
    if (alpha < 0.0 || alpha > 1.0) {
        std::cerr << "Error: Alpha value must be in the range [0.0, 1.0]." << std::endl; return; }

    // Make overlay
    output = background.clone();
    cv::addWeighted(background, 1.0 - alpha, overlay,
                    alpha, 0,output);
}


void ImageBase::cropImageBy2Corners(cv::Mat& targetImage, int topLeftX, int topLeftY,
                         int bottomRightX, int bottomRightY) {

    if (topLeftX < 0 || topLeftY < 0 || bottomRightX >= targetImage.cols ||
            bottomRightY >= targetImage.rows) {
        // Log
        std::cerr << "Invalid coordinates for cropping." << std::endl;
        // error return
        return;
    }

    // Calculate W and H
    int width = bottomRightX - topLeftX + 1;
    int height = bottomRightY - topLeftY + 1;

    // Create rect
    cv::Rect roi(topLeftX, topLeftY, width, height);

    // Crop
    targetImage = targetImage(roi);
}


void ImageBase::rotateImage(cv::Mat& targetImage, int& rotationDegrees) {
    if (rotationDegrees == 90 || rotationDegrees == -270) {
        cv::rotate(targetImage, targetImage, cv::ROTATE_90_CLOCKWISE);
    } else if (rotationDegrees == -90 || rotationDegrees == 270) {
        cv::rotate(targetImage, targetImage, cv::ROTATE_90_COUNTERCLOCKWISE);
    } else if (rotationDegrees == 180 || rotationDegrees == -180) {
        cv::rotate(targetImage, targetImage, cv::ROTATE_180);
    }
}