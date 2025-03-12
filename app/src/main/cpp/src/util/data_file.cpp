// Created by keta on 12/18/23.

#include <iostream>
#include <fstream>
#include <string>
#include "opencv2/opencv.hpp"

#include "data_file.h"


void DataFile::writePoints2dVector(const std::string& filePath,
                         std::vector<cv::Point2f>& points2dVector) {

    // Open file
    std::ofstream file(filePath);

    // Write cornersPosition to file
    for (const auto& corner : points2dVector) {
        file << corner.x << " " << corner.y << std::endl;
    }

    // Close file
    file.close();
}
void DataFile::readPoints2dVector(const std::string& filePath,
                       std::vector<cv::Point2f>& points2dVector) {

    // Open file
    std::ifstream file(filePath);

    // Read cornersPosition from file
    float x, y;
    while (file >> x >> y) {
        points2dVector.emplace_back(x, y);
    }

    // Close file
    file.close();
}


void DataFile::writeVectorVectorPoints2d(const std::string& filePath,
                                   const std::vector<std::vector<cv::Point2f>>& points2dVector) {
    // Open file
    std::ofstream file(filePath);

    // Write points to file
    for (const auto& innerVector : points2dVector) {
        for (const auto& point : innerVector) {
            file << point.x << " " << point.y << std::endl;
        }
        file << std::endl; // Separate inner vectors with a blank line
    }

    // Close file
    file.close();
}
void DataFile::loadVectorVectorPoints2d(const std::string& filePath,
                                  std::vector<std::vector<cv::Point2f>>& points2dVector) {
    // Open file
    std::ifstream file(filePath);

    // Read points from file
    std::string line;
    std::vector<cv::Point2f> innerVector;
    while (std::getline(file, line)) {
        if (line.empty()) {
            if (!innerVector.empty()) {
                points2dVector.push_back(innerVector);
                innerVector.clear();
            }
        } else {
            std::istringstream iss(line);
            float x, y;
            if (iss >> x >> y) {
                innerVector.emplace_back(x, y);
            }
        }
    }
    if (!innerVector.empty()) {
        points2dVector.push_back(innerVector);
    }

    // Close file
    file.close();
}


void DataFile::saveDictionary(const std::unordered_map<std::string, std::string>& targetDictionary,
                    const std::string& filePath) {
    std::ofstream file(filePath);
    for (const auto& pair : targetDictionary) {
        file << pair.first << ":" << pair.second << "\n";
    }
    file.close();
}


void DataFile::loadDictionary(const std::string& filePath,
                    std::unordered_map<std::string, std::string>& targetDictionary) {

    std::ifstream file(filePath);

    std::string line;
    while (std::getline(file, line)) {
        size_t pos = line.find(':');
        if (pos != std::string::npos) {
            std::string key = line.substr(0, pos);
            std::string value = line.substr(pos + 1);
            targetDictionary[key] = value;
        }
    }

    file.close();
}


void DataFile::saveString(const std::string& value, const std::string& filePath) {
    std::ofstream file(filePath);
    // Write the string directly to the file
    file << value << std::endl;
    file.close();
}
std::string DataFile::loadString(const std::string& filePath, const std::string defaultValue) {
    std::ifstream file(filePath);

    if (file.is_open()) {
        std::string value;
        // Read the entire line into the string
        std::getline(file, value);
        file.close();
        return value;
    } else {
        return defaultValue;
    }
}


void DataFile::saveBool(const bool& value, const std::string& filePath) {
    std::ofstream file(filePath);
    file << (value ? "true" : "false") << "\n";
    file.close();
}
bool DataFile::loadBool(const std::string& filePath, const bool defaultValue) {
    std::ifstream file(filePath);
    if (file.is_open()) {
        std::string valueStr;
        file >> valueStr;
        file.close();
        return (valueStr == "true");
    } else {
        return defaultValue;
    }
}


void DataFile::saveInt(const int& value, const std::string& filePath) {

    std::ofstream file(filePath);
    file << value << "\n";
    file.close();
}
int DataFile::loadInt(const std::string& filePath, const int defaultValue) {

    std::ifstream file(filePath);

    if (file.is_open()) {
        int value;
        file >> value;
        file.close();
        return value;
    } else {
        return defaultValue;
    }
}


void DataFile::saveLong(const long& value, const std::string& filePath) {

    std::ofstream file(filePath);
    file << value << "\n";
    file.close();
}
long DataFile::loadLong(const std::string& filePath, const long defaultValue) {

    std::ifstream file(filePath);

    if (file.is_open()) {
        long value;
        file >> value;
        file.close();
        return value;
    } else {
        return defaultValue;
    }
}


void DataFile::saveFloat(const float& value, const std::string& filePath) {
    std::ofstream file(filePath);
    file << value << "\n";
    file.close();
}
float DataFile::loadFloat(const std::string& filePath, const float defaultValue) {

    std::ifstream file(filePath);

    if (file.is_open()) {
        float value;
        file >> value;
        file.close();
        return value;
    } else {
        return defaultValue;
    }
}