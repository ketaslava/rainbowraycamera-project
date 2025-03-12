// Created by ketaslava on 12/3/23.


#include <iostream>
#include <filesystem>
#include <opencv2/opencv.hpp>
#include "src/image_processing.h"


void copyCollectionToNewDirectory(const std::string& sourceDir,
                   const std::string& destinationDir) {

    for (const auto& file : std::filesystem::directory_iterator(sourceDir)) {
        if (std::filesystem::is_regular_file(file)) {
            std::filesystem::path destinationPath =
                    std::filesystem::path(destinationDir) / file.path().filename();
            std::filesystem::copy(file.path(), destinationPath);
        }
    }
}


void deleteFiles(const std::string& directoryPath) {

    for (const auto& file : std::filesystem::directory_iterator(directoryPath)) {
        if (std::filesystem::is_regular_file(file)) {
            std::filesystem::remove(file);
        }
    }
}


int main(int argc, char **argv) {

	// Settings

	const std::string imageCollectionsRoot = "/home/keta/KTVINCCO/RainbowRayCamera/rainbowraycamera-large-files/image_collections";
    const std::string debugDirectoryPath = "/home/keta/KTVINCCO/RainbowRayCamera/rainbowraycamera-project/app/src/main/cpp/debug";
    // 1707107571547 -- std old
    // 1711515265625 -- std night
    // 1711515279626 -
    // 1711515300225 -
    // 1711515324273 -
    // 1711515337373 -
    // 1711515355754 -
    // 1711515430903 - night grass
    // 1711515452743 -
    // 1711515467946 -
    // 1711515479370 - big bright object
    // 1711515526034 - movement
    // 1711515538363 - big sky
    // 1711515555696 - hard
    // 1711515568358 - hard
    // 1711515601330 - darkness
    // 1711515621953 - hard
    // 1711571451786 -- std day
    // 1711571474846 -
    // 1711571479230 -
    const std::string imageCollection = "/1711515265625";


	// Setup variables

	const std::string collectionPath = imageCollectionsRoot + imageCollection;

	// Run tests

	std::cout << "Run tests !" << std::endl;

	// Move image collection to debug directory
	std::cout << "Move image collection to debug directory" << std::endl;
    // Remove old files and copy new
    deleteFiles(debugDirectoryPath);
    copyCollectionToNewDirectory(collectionPath, debugDirectoryPath);

    // Run image processing
    std::cout << "Run image processing" << std::endl;
    ImageProcessing imageProcessing(debugDirectoryPath);
    imageProcessing.processImage();

	return 0;
}
