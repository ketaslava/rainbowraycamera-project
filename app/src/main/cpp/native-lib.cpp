#include <jni.h>
#include <cstdio>
#include <string>

#include "src/image_processing.h"
#include "opencv2/opencv.hpp"
#include "src/rotateImage.h"
#include "src/automatic_controller.h"


extern "C" JNIEXPORT jstring JNICALL
Java_com_ktvincco_rainbowraycamera_domain_component_ContentPostProcessor_processImageCollection(
        JNIEnv* env,jobject /* this */, jstring workingDirectoryJ) {

    // Get base string from jstring
    const char *workingDirectory = env->GetStringUTFChars(workingDirectoryJ, nullptr);
    std::string inputStdString(workingDirectory);
    env->ReleaseStringUTFChars(workingDirectoryJ, workingDirectory);

    // Create image processor
    ImageProcessing imageProcessing(workingDirectory);

    // Process image collection
    std::string imageProcessingResult;
    try {
        imageProcessingResult = imageProcessing.processImage();
    } catch (cv::Exception) {
        imageProcessingResult = "Error";
    } catch (...) {
        imageProcessingResult = "Error";
    }

    // Return result
    return env->NewStringUTF(imageProcessingResult.c_str());
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_ktvincco_rainbowraycamera_data_DataSaver_rotateImageByPath(
        JNIEnv* env,jobject /* this */,
        jstring imagePathJ, jint rotationDegreesJ) {

    // Get std::string from jstring
    const char *imagePath = env->GetStringUTFChars(imagePathJ, nullptr);
    std::string inputStdString(imagePath);
    env->ReleaseStringUTFChars(imagePathJ, imagePath);

    // Get int from jint
    int rotationDegrees = static_cast<int>(rotationDegreesJ);

    // Rotate image
    std::string result = RotateImage::rotateImage(imagePath, rotationDegrees);

    // Return result
    return env->NewStringUTF(result.c_str());
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_ktvincco_rainbowraycamera_data_AutomaticController_calculateAutomaticControlFromCaptureResult(
        JNIEnv* env,jobject /* this */, jstring workingDirectoryPathJ) {

    // Get std::string from jstring
    const char *workingDirectoryPath =
            env->GetStringUTFChars(workingDirectoryPathJ, nullptr);
    std::string inputStdString(workingDirectoryPath);
    env->ReleaseStringUTFChars(workingDirectoryPathJ, workingDirectoryPath);

    // Create image processor
    std::string result = AutomaticController::processCaptureResult(workingDirectoryPath);

    // Return result
    return env->NewStringUTF(result.c_str());
}