package com.ktvincco.rainbowraycamera.domain.component


import android.app.Activity
import android.util.Log
import com.ktvincco.rainbowraycamera.data.DataSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ContentPostProcessor (
    private val mainActivity: Activity,
    private val dataSaver: DataSaver,
    private val monetizationService: MonetizationService
) {


    // Variables


    private var currentState = "Stopped"
    private var currentCollectionName = ""
    companion object {
        // Log
        const val LOG_TAG = "ContentPostProcessor"
        // Used to load the 'rainbowraycamera' library on application startup.
        init {
            System.loadLibrary("rainbowraycamera")
        }
    }
    private var currentStateUpdateLoopJob: Job? = null


    // External


    private external fun processImageCollection(inputString: String): String


    // Private


    private fun processPhotoCollection(collectionName: String) {

        // Log
        Log.i(LOG_TAG, "Start process collection (photo)")

        // Copy all files to processing directory
        dataSaver.copyCollectionToProcessingDirectory(collectionName)

        // Assign index 0 image as result
        dataSaver.useIndex0ImageAsResult()

        // Rotate and copy result to public storage
        dataSaver.normalizeInSpaceResultInProcessingDirectory()

        // Check activation and move result
        if (monetizationService.getIsFullVersionActive()) {
            dataSaver.copyResultFromPhotoCollectionToPublicStorage(collectionName)
        } else {
            dataSaver.copyResultFromPhotoCollectionToPrivateResultStorage(collectionName)
        }

        // Delete collection
        dataSaver.deleteCollection(collectionName)

        // Set idle state
        currentState = "Idle"
    }


    private fun processMultiImagePhotoCollection(collectionName: String) {

        // Log
        Log.i(LOG_TAG, "Start process collection (multi image photo)")

        // Copy all files to processing directory
        dataSaver.copyCollectionToProcessingDirectory(collectionName)

        // Get processing directory
        val processingDirectory = dataSaver.getProcessingDirectory()

        // Launch image processing
        CoroutineScope(Dispatchers.Default).launch {
            try {

                // Async call to external C++ content processor
                val result = processImageCollection(processingDirectory.absolutePath)

                // Process result
                if (result == "Success") {

                    // Rotate and copy result to public storage
                    dataSaver.normalizeInSpaceResultInProcessingDirectory()

                    // Check activation and move result
                    if (monetizationService.getIsFullVersionActive()) {
                        dataSaver.copyResultFromPhotoCollectionToPublicStorage(collectionName)
                    } else {
                        dataSaver.copyResultFromPhotoCollectionToPrivateResultStorage(
                            collectionName)
                    }

                    // Delete collection
                    dataSaver.deleteCollection(collectionName)

                    // Log
                    Log.i(LOG_TAG, "Process collection success finished")
                }

                if (result == "Stopped") {
                    // Log
                    Log.i(LOG_TAG, "Process collection force stopped")
                }

                if (result == "Error") {
                    // Log
                    Log.i(LOG_TAG, "Process collection error")

                    // Delete problem collection
                    dataSaver.deleteCollection(collectionName)
                }

            } catch (e: Exception) {
                // Process exceptions while processing
                e.printStackTrace()

                // Log
                Log.i(LOG_TAG, "Process collection exception")

                // Delete problem collection
                dataSaver.deleteCollection(collectionName)
            }

            // Set idle state
            currentState = "Idle"
        }
    }


    private fun processVideoCollection(collectionName: String) {

        // Log
        Log.i(LOG_TAG, "Start process collection (photo)")

        // Copy all files to processing directory
        dataSaver.copyCollectionToProcessingDirectory(collectionName)

        // Check activation and move result
        if (monetizationService.getIsFullVersionActive()) {
            dataSaver.copyResultFromVideoCollectionToPublicStorage(collectionName)
        } else {
            dataSaver.copyResultFromVideoCollectionToPrivateResultStorage(collectionName)
        }

        // Delete collection
        dataSaver.deleteCollection(collectionName)

        // Set idle state
        currentState = "Idle"
    }


    private fun updateCurrentState() {

        // Log
        // Log.i(LOG_TAG, "Update current state")

        // Check is state not idle
        if (currentState != "Idle") { return; }

        // Get available collections to process
        val nextCollectionName = dataSaver.getNextCollectionNameAvailableToProcess()

        // Start process collection
        if (nextCollectionName != null) {

            // Set state
            currentState = "Processing"

            // Get collection content type
            val collectionContentType =
                dataSaver.getCollectionContentTypeByName(nextCollectionName)

            // Process collection by type
            if (collectionContentType == "Photo") {
                processPhotoCollection(nextCollectionName)
            }
            if (collectionContentType == "MultiImagePhoto" ||
                collectionContentType == "MultiImageNightPhoto") {
                processMultiImagePhotoCollection(nextCollectionName)
            }
            if (collectionContentType == "Video") {
                processVideoCollection(nextCollectionName)
            }
        }
    }


    private suspend fun currentStateUpdateLoop() {
        while (true) {
            // Update current state
            updateCurrentState()
            // Delay
            delay(800)
        }
    }


    // Public


    fun start() {

        // Set idle state
        currentState = "Idle"

        // Enable state update loop
        if (currentStateUpdateLoopJob?.isActive == false || currentStateUpdateLoopJob == null) {
            currentStateUpdateLoopJob = CoroutineScope(Dispatchers.Default).launch {
                currentStateUpdateLoop()
            }
        }
    }

    fun stop() {

        // Stop processing
        if (currentState == "Processing") {
            dataSaver.forceStopCollectionProcess(currentCollectionName)
        }

        // Disable state update loop
        currentStateUpdateLoopJob?.cancel()

        // Set idle state
        currentState = "Stopped"
    }
}