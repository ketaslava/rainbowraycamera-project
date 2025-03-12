// Created by keta on 3/11/24.

#ifndef RAINBOWRAYCAMERA_AUTO_EXPOSITION_H
#define RAINBOWRAYCAMERA_AUTO_EXPOSITION_H


class AutoExposition {

private:

    /*static long getCurrentUnixTimeInMillis();

    static void calculateEdgeMap(const cv::Mat& image);

    static void cropImageBySquareAtPoint(
            cv::Mat& image, const int& squareSideSize, const float& pointX, const float& pointY);*/

public:

    static void calculateExposition(const std::string& workingDirectoryPath, cv::Mat image);

};


#endif //RAINBOWRAYCAMERA_AUTO_EXPOSITION_H
