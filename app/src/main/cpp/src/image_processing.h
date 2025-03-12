// Created by keta on 12/23/23.

#ifndef RAINBOWRAYCAMERA_IMAGE_PROCESSING_H
#define RAINBOWRAYCAMERA_IMAGE_PROCESSING_H


class ImageProcessing {

private:

    std::string workingDirectoryPath;

public:

    ImageProcessing(std::string  workingDirectoryPath);

    std::string processImage();

};


#endif //RAINBOWRAYCAMERA_IMAGE_PROCESSING_H
