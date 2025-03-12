// Created by keta on 2/7/24.

#include <iostream>
#include <fstream>
#include <string>
#include <opencv2/opencv.hpp>

#include "make_composite_image.h"

#include "merge_images.h"
#include "../settings.h"
#include "../util/image_base.h"
#include "../util/data_file.h"


void downscaleImageToPositioningProcess(cv::Mat& image) {

    // Calculate new size
    cv::Size newSize;
    float origImgXF = static_cast<float>(image.cols);
    float origImgYF = static_cast<float>(image.rows);
    const int bigSideSize = Settings::imagePositioningDownscaledBigSideSize;
    if (image.cols >= image.rows) {
        newSize = cv::Size(bigSideSize,  bigSideSize * (origImgYF / origImgXF));
    } else {
        newSize = cv::Size(bigSideSize * (origImgXF /origImgYF), bigSideSize);
    }

    // Resize
    ImageBase::resizeFitMatImage(image, newSize);
}


bool isImagePositionValid(const cv::Size& imageSize, cv::Mat& perspectiveMatrix) {

    // Get image corners position
    auto corners = std::vector<cv::Point2f>(4);
    corners[0] = cv::Point2f(0.0f, 0.0f);
    corners[1] = cv::Point2f(static_cast<float>(imageSize.width - 1), 0.0f);
    corners[2] = cv::Point2f(0.0f, static_cast<float>(imageSize.height - 1));
    corners[3] = cv::Point2f(static_cast<float>(imageSize.width - 1),
                             static_cast<float>(imageSize.height - 1));

    // Check data
    if (corners.size() != 4) { return false; }
    if (perspectiveMatrix.empty()) { return false; }
    if (!(perspectiveMatrix.rows == 3 && perspectiveMatrix.cols == 3 &&
        (perspectiveMatrix.type() == CV_32F || perspectiveMatrix.type() == CV_64F))) {
        return false; }

    // Apply perspective transform to the points
    try {
        cv::perspectiveTransform(corners, corners, perspectiveMatrix);
    } catch (...) {
        return false;
    }

    // Calculate and check if image position have a big deviation

    const auto imsX = static_cast<float>(imageSize.width);
    const auto imsY = static_cast<float>(imageSize.height);
    const float& mdp = Settings::imagePositioningMaxValidPercentOfPositionDeviation / 100.0;
    bool ihbd = false; // Is have big deviation

    // Top left
    if ((std::abs(corners[0].x) / imsX) > mdp) { ihbd = true; }
    if ((std::abs(corners[0].y) / imsY) > mdp) { ihbd = true; }
    // Top right
    if ((std::abs(corners[1].x - imsX) / imsX) > mdp) { ihbd = true; }
    if ((std::abs(corners[1].y) / imsY) > mdp) { ihbd = true; }
    // Bottom left
    if ((std::abs(corners[2].x) / imsX) > mdp) { ihbd = true; }
    if ((std::abs(corners[2].y - imsY) / imsY) > mdp) { ihbd = true; }
    // Bottom right
    if ((std::abs(corners[3].x - imsX) / imsX) > mdp) { ihbd = true; }
    if ((std::abs(corners[3].y - imsY) / imsY) > mdp) { ihbd = true; }

    // Return result ( Is have bad good position (not bad) )
    std::cout << "Is position valid: " << !ihbd << std::endl;
    return !ihbd;
}


bool perspectiveTransform(cv::Mat& image, const cv::Mat& perspectiveMatrix) {

    // Check input data <- that the matrix is a 3x3 matrix with type CV_32F or CV_64F
    if (!(perspectiveMatrix.rows == 3 && perspectiveMatrix.cols == 3 &&
        (perspectiveMatrix.type() == CV_32F || perspectiveMatrix.type() == CV_64F))) {
        std::cout << "Error: Invalid perspective matrix" << std::endl; return false; }

    // Apply the perspective transformation to the image and process exceptions
    try {
        cv::warpPerspective(image, image,perspectiveMatrix, image.size());
        return true;

    } catch (...) {
        std::cout << "Error in warpPerspective" << std::endl; return false;
    }
}


bool getNewImageCorners(const cv::Mat& matrix, const cv::Size& imageSize,
                          std::vector<cv::Point2f>& corners) {

    // Corners
    corners = std::vector<cv::Point2f>(4);
    corners[0] = cv::Point2f(0.0f, 0.0f);
    corners[1] = cv::Point2f(static_cast<float>(imageSize.width - 1), 0.0f);
    corners[2] = cv::Point2f(0.0f, static_cast<float>(imageSize.height - 1));
    corners[3] = cv::Point2f(static_cast<float>(imageSize.width - 1),
                             static_cast<float>(imageSize.height - 1));

    // Apply perspective transform to the points
    try {
        cv::perspectiveTransform(corners, corners, matrix);
        return true;
    } catch (...) {
        std::cerr << "Error in getNewCornersPosition" << std::endl;
        return false;
    }
}


bool calculatePerspectiveMatrix(const cv::Mat& downscaledBaseImg, const cv::Mat& targetImg,
                                cv::Mat& perspectiveMatrix) {

    // Downscale input images

    cv::Mat downscaledTargetImg = targetImg;
    downscaleImageToPositioningProcess(downscaledTargetImg);

    // Position image

    // Initialize SIFT detector
    cv::Ptr<cv::Feature2D> sift = cv::SIFT::create();

    // Find the keypoints and descriptors with SIFT
    std::vector<cv::KeyPoint> kp1, kp2;
    cv::Mat des1, des2;
    sift->detectAndCompute(downscaledBaseImg, cv::noArray(), kp1, des1);
    sift->detectAndCompute(downscaledTargetImg, cv::noArray(), kp2, des2);

    // BFMatcher with default params
    cv::BFMatcher bf;
    std::vector<std::vector<cv::DMatch>> matches;
    bf.knnMatch(des1, des2, matches, 2);

    // Filter keypoints by match distance
    std::vector<cv::DMatch> goodMatches;
    int goodMatchesCount = 0;
    for (const auto& match : matches) {
        if (match[0].distance < 0.75 * match[1].distance) {
            goodMatches.push_back(match[0]);
            goodMatchesCount ++;
        }
    }

    // Extract matched keypoints
    std::vector<cv::Point2f> srcPts, dstPts;
    for (const auto& match : goodMatches) {

        int queryIdx = match.queryIdx; int trainIdx = match.trainIdx;

        // Check is point valid
        if (queryIdx < 0 || queryIdx >= kp1.size() || trainIdx < 0 || trainIdx >= kp2.size()) {
            // if index not valid, skip iteration
            continue;
        }

        srcPts.push_back(kp1[queryIdx].pt); dstPts.push_back(kp2[trainIdx].pt);
    }

    // No matches EXC (impossible to positioning) -> Exit with false state
    if (srcPts.size() < 4 || dstPts.size() < 4) {
        std::cout << "Insufficient key points for positioning" << std::endl;
        return false;
    }

    // Calculate perspective matrix

    // Scale keypoints for the original size
    float scale_x = static_cast<float>(targetImg.cols) /
            static_cast<float>(downscaledTargetImg.cols);
    float scale_y = static_cast<float>(targetImg.rows) /
            static_cast<float>(downscaledTargetImg.rows);
    for (auto& point : srcPts) { point.x *= scale_x; point.y *= scale_y; }
    for (auto& point : dstPts) { point.x *= scale_x; point.y *= scale_y; }

    // Find perspective transformation matrix
    perspectiveMatrix = cv::findHomography(dstPts, srcPts, cv::RANSAC,
                                           5.0);

    // Success exit -> exit with true state
    return true;
}


bool MakeCompositeImage::getCompositeImage(cv::Mat& compositeImage, const cv::Mat& baseImage,
        const std::string& workingDirectoryPath,const int& imagesCount) {

    std::cout << "Merge collection process" << std::endl;

    // Setup

    // Process base image
    cv::Size baseImageSize(baseImage.cols, baseImage.rows);
    cv::Mat downscaledBaseImg = baseImage;
    downscaleImageToPositioningProcess(downscaledBaseImg);

    // Create positioned image indexes list
    std::list<int> positionedImageIndexesList;

    // Create positioned images corners vector
    std::vector<std::vector<cv::Point2f>> positionedImagesCorners;

    // Image positioning and filter by position

    // For all images in collection
    int currentImageIndex = 1; // start from image by index 2 (1 + 1)
    while (currentImageIndex + 1 < imagesCount) {

        // Next image
        currentImageIndex ++;
        std::string currentImageIndexStr = std::to_string(currentImageIndex);

        // Log
        std::cout << "currentImageIndex: " << currentImageIndex << std::endl;

        // Read image
        cv::Mat targetImage = cv::imread(workingDirectoryPath + "/" +
                currentImageIndexStr + ".jpg", cv::IMREAD_COLOR);

        // Calculate position
        cv::Mat perspectiveMatrix;
        bool result = calculatePerspectiveMatrix(downscaledBaseImg,targetImage,
                                                 perspectiveMatrix);
        if (!result) { continue; }

        // Check is position valid
        result = isImagePositionValid(baseImageSize, perspectiveMatrix);
        if (!result) { continue; }

        // Transform image to new position
        result = perspectiveTransform(targetImage, perspectiveMatrix);
        if (!result) { continue; }

        // Get new image corners
        std::vector<cv::Point2f> imageCorners;
        result = getNewImageCorners(
                perspectiveMatrix, baseImageSize, imageCorners);
        positionedImagesCorners.push_back(imageCorners);
        if (!result) { continue; }

        // Save image and write index to list
        cv::imwrite(workingDirectoryPath + "/positionedImage" +
                    currentImageIndexStr + ".jpg", targetImage);
        positionedImageIndexesList.push_back(currentImageIndex);
    }

    // Merge all images to composite image

    // Log
    std::cout << "Merge collection to composite image" << std::endl;

    // Assign composite image as base
    compositeImage = baseImage;

    // Merge all good positioned images to base
    int imageSerialNumber = 0;
    for (const auto& imageIndex : positionedImageIndexesList) {

        // Get string index
        std::string imageIndexStr = std::to_string(imageIndex);

        // Log
        std::cout << "currentImageIndex: " << imageIndexStr << std::endl;

        // Read image to merge
        cv::Mat targetImage = imread(workingDirectoryPath + "/positionedImage" +
            imageIndexStr + ".jpg", cv::IMREAD_COLOR);

        // Merge process
        MergeImages::mergeFrameToCompositeImage(targetImage, baseImage, imageSerialNumber,
                                           compositeImage, workingDirectoryPath);

        // Next image
        imageSerialNumber ++;
    }

    // Final process

    // Save images corners image
    DataFile::writeVectorVectorPoints2d(
        workingDirectoryPath + "/positionedImagesCorners.txt",
        positionedImagesCorners);

    // Log
    std::cout << "Merge collection finished" << std::endl;

    return true;
}