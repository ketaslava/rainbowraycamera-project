// Created by keta on 3/26/24.

#ifndef RAINBOWRAYCAMERA_CROP_RESULT_IMAGE_H
#define RAINBOWRAYCAMERA_CROP_RESULT_IMAGE_H


class CropResultImage {

public:

    static void cropResultByPositionedImagesCorners(
            cv::Mat& targetImage, std::string workingDirectoryPath);

};


#endif //RAINBOWRAYCAMERA_CROP_RESULT_IMAGE_H
