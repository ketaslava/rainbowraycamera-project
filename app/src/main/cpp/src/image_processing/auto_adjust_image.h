// Created by keta on 2/8/24.

#ifndef RAINBOWRAYCAMERA_AUTO_ADJUST_IMAGE_H
#define RAINBOWRAYCAMERA_AUTO_ADJUST_IMAGE_H


class AutoAdjustImage {

public:

    static void normalizeBrightness(cv::Mat& image);

    static void normalizeShadows(cv::Mat& image);

    static void autoColorCorrection(cv::Mat& image);

};


#endif //RAINBOWRAYCAMERA_AUTO_ADJUST_IMAGE_H
