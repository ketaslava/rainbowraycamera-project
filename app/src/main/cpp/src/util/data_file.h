// Created by keta on 12/18/23.

#ifndef RAINBOWRAYCAMERA_DATA_FILE_H
#define RAINBOWRAYCAMERA_DATA_FILE_H


class DataFile {

public:

    static void writePoints2dVector(const std::string& frameDataFilePath,
                             std::vector<cv::Point2f>& cornersPosition);
    static void readPoints2dVector(const std::string& frameDataFilePath,
                           std::vector<cv::Point2f>& cornersPosition);

    static void writeVectorVectorPoints2d(const std::string& filePath,
        const std::vector<std::vector<cv::Point2f>>& points2dVector);
    static void loadVectorVectorPoints2d(const std::string& filePath,
        std::vector<std::vector<cv::Point2f>>& vectorVectorPoints2d);

    static void saveDictionary(const std::unordered_map<std::string, std::string>& targetDictionary,
                               const std::string& filePath);
    static void loadDictionary(const std::string& filePath,
                               std::unordered_map<std::string, std::string>& targetDictionary);

    static void saveString(const std::string& value, const std::string& filePath);
    static std::string loadString(const std::string& filePath, const std::string defaultValue);

    static void saveBool(const bool& value, const std::string& filePath);
    static bool loadBool(const std::string& filePath, const bool defaultValue);

    static void saveInt(const int& value, const std::string& filePath);
    static int loadInt(const std::string& filePath, const int defaultValue);

    static void saveLong(const long& value, const std::string& filePath);
    static long loadLong(const std::string& filePath, const long defaultValue);

    static void saveFloat(const float& value, const std::string& filePath);
    static float loadFloat(const std::string& filePath, const float defaultValue);
};


#endif //RAINBOWRAYCAMERA_DATA_FILE_H
