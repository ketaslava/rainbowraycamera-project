// Created by keta on 12/29/23.

#ifndef RAINBOWRAYCAMERA_SETTINGS_H
#define RAINBOWRAYCAMERA_SETTINGS_H


class Settings {

public:

    // Custom logger

    static const bool isEnableLogger = false; // false
    static const std::string logFilePath;

    // Image processing

    // Positioning
    static const int imagePositioningDownscaledBigSideSize = 512; // 512
    static const int imagePositioningMaxValidPercentOfPositionDeviation = 4; // 4

};


#endif //RAINBOWRAYCAMERA_SETTINGS_H
