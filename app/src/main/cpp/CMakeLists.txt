

# Base

cmake_minimum_required(VERSION 3.22.1)

project("rainbowraycamera")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "-std=c++17")


# Configure state

set(IS_TEST_BUILD OFF CACHE STRING "Build project to execute to run code tests")


# Set sources

set(SOURCES
		# Base
		run_tests.cpp
		src/custom_logger.cpp
		src/settings.cpp
		# Components
		src/image_processing.cpp
		src/rotateImage.cpp
		src/automatic_controller.cpp
		# Image processing - base
		src/util/image_base.cpp
		src/util/data_file.cpp
		# Image processing - processing
		src/image_processing/make_composite_image.cpp
		src/image_processing/crop_result_image.cpp
        src/image_processing/merge_images.cpp
		src/image_processing/auto_adjust_image.cpp
		# Automatic controller
		src/automatic_controller/auto_focus.cpp
		src/automatic_controller/auto_exposition.cpp
		)


# Add target

if(NOT ${IS_TEST_BUILD})

	# Info
	message("BUILD FOR RELEASE")

	# Target for release
	add_library(${CMAKE_PROJECT_NAME} SHARED
			native-lib.cpp ${SOURCES})
else()

	# Info
	message("BUILD FOR TEST")

	# Target for test mode
	add_executable(${CMAKE_PROJECT_NAME} ${SOURCES})
endif()


# Resolve dependencies

if(NOT ${IS_TEST_BUILD})

	# OpenCV from .so lib files for release
	set(pathToOpenCV "${CMAKE_SOURCE_DIR}/../jniLibs")
	include_directories(${pathToOpenCV}/include)
	add_library(lib_opencv SHARED IMPORTED)
	set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION
			${pathToOpenCV}/${ANDROID_ABI}/libopencv_java4.so)

	# Link libraries for release
	target_link_libraries(${CMAKE_PROJECT_NAME}
			# Base
			android
			log
			# Custom
			lib_opencv
			)

else()

	# OpenCV from system for test mode
	find_package(OpenCV REQUIRED)

	# Link libraries for test mode
	target_link_libraries(${CMAKE_PROJECT_NAME} ${OpenCV_LIBS})

endif()


# State reset

set(IS_TEST_BUILD OFF CACHE STRING "Build project to execute to run code tests" FORCE)

