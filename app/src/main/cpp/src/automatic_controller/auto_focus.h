// Created by keta on 3/10/24.

#ifndef RAINBOWRAYCAMERA_AUTO_FOCUS_H
#define RAINBOWRAYCAMERA_AUTO_FOCUS_H


class AutoFocus {

private:

    static long getCurrentUnixTimeInMillis();

    static void calculateEdgeMap(const cv::Mat& image);

    static void cropImageBySquareAtPoint(
            cv::Mat& image, const int& squareSideSize, const float& pointX, const float& pointY);

public:

    static void calculateFocusDistance(const std::string& workingDirectoryPath, cv::Mat image);

};


#endif //RAINBOWRAYCAMERA_AUTO_FOCUS_H
