// Created by keta on 12/18/23.

#ifndef RAINBOWRAYCAMERA_MERGE_IMAGES_H
#define RAINBOWRAYCAMERA_MERGE_IMAGES_H


class MergeImages {

public:

    static void mergeFrameToCompositeImage(cv::Mat& targetImage, const cv::Mat& baseImage,
        const int& imageSerialNumber, cv::Mat& calculateMergeMask,
        [[maybe_unused]] const std::string& workingDirectoryPath);

    static void mergeCompositeToBaseImage(cv::Mat& baseImage, cv::Mat& compositeImage,
        [[maybe_unused]] const std::string& workingDirectoryPath);
};


#endif //RAINBOWRAYCAMERA_MERGE_IMAGES_H
