// Created by keta on 12/17/23.

#ifndef RAINBOWRAYCAMERA_IMAGE_BASE_H
#define RAINBOWRAYCAMERA_IMAGE_BASE_H


class ImageBase {

public:

    static void resizeFitMatImage(cv::Mat& inputImage, const cv::Size& newSize);

    static void overlayImages(const cv::Mat& background, const cv::Mat& overlay, double alpha,
                       cv::Mat& output);

    static void cropImageBy2Corners(cv::Mat& targetImage, int topLeftX, int topLeftY,
                                    int bottomRightX, int bottomRightY);

    static void rotateImage(cv::Mat& targetImage, int& rotationDegrees);

};


#endif //RAINBOWRAYCAMERA_IMAGE_BASE_H
