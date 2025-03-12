// Created by keta on 12/30/23.

#include <string>
#include <fstream>
#include <iostream>

#include "settings.h"

#include "custom_logger.h"


void CustomLogger::logToFile(const std::string& text) {

    if (!Settings::isEnableLogger) { return; }

    const std::string& logFilePath = Settings::logFilePath;

    std::ofstream outputFile(logFilePath, std::ios::app);

    if (!outputFile.is_open()) {
        outputFile.open(logFilePath, std::ios::out);
    }

    if (outputFile.is_open()) {
        outputFile << text << std::endl;
        outputFile.close();
    }
}