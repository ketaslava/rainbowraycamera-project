// Created by keta on 2/7/24.

#ifndef RAINBOWRAYCAMERA_MAKE_COMPOSITE_IMAGE_H
#define RAINBOWRAYCAMERA_MAKE_COMPOSITE_IMAGE_H


class MakeCompositeImage {

public:

    static bool getCompositeImage(cv::Mat& compositeImage, const cv::Mat& baseImage,
                                 const std::string& workingDirectoryPath,const int& imagesCount);

};


#endif //RAINBOWRAYCAMERA_MAKE_COMPOSITE_IMAGE_H
