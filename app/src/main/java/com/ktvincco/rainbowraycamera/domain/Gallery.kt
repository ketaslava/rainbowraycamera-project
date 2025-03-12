package com.ktvincco.rainbowraycamera.domain


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.FileProvider
import com.ktvincco.rainbowraycamera.AppSettings
import com.ktvincco.rainbowraycamera.data.DataSaver
import com.ktvincco.rainbowraycamera.domain.component.MonetizationService
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


class Gallery (
    private var mainActivity: Activity,
    private val modelData: ModelData,
    private val uiEventHandler: UiEventHandler,
    private val dataController: DataSaver,
    private val monetizationService: MonetizationService,
    private val dataSaver: DataSaver,
    private val openDomainCallback: (domainName: String) -> Unit ) {


    // Settings


    companion object {
        const val LOG_TAG = "Gallery"
    }
    private val appSettings = AppSettings()


    // Variables


    // Gallery
    private var isGalleryOpen = false
    private var currentFileIndex = 0
    private var filesToViewPaths = emptyList<String>()
    private var inProcessingFilesCount = 0
    private var viewUpdateLoopJob: Job? = null
    private var currentFilePath: String? = null
    private var isImageInRotationProcess = false
    // Monetization
    private var isFullAppVersionActive = false
    private var currentMediaSavesCount = 0


    // Private


    private suspend fun viewUpdateLoop() {
        while (viewUpdateLoopJob?.isActive == true) {
            // Update view
            showFile(currentFileIndex, true)
            // Delay
            delay(1500)
        }
    }


    private fun getAllAvailableFilesToView() {

        // Get all base files
        val localFilesPaths = dataController.getAllBaseImagePathsInCollections().reversed()
        inProcessingFilesCount = localFilesPaths.size

        // Get all files in public directory
        val storageFilesPaths =
            dataController.getAllMediaPathsInPublicAndPrivateStorage().reversed()

        // Combine
        filesToViewPaths = localFilesPaths + storageFilesPaths
    }


    private fun updateNextAndBackButtons() {
        if (currentFileIndex > 0) { modelData.setIsNextButtonActive(true) }
        else { modelData.setIsNextButtonActive(false) }
        if (currentFileIndex < filesToViewPaths.size - 1) { modelData.setIsBackButtonActive(true) }
        else { modelData.setIsBackButtonActive(false) }
    }


    private fun showFile(index: Int, isSoftUpdate: Boolean = false) {

        // Assign variable
        var fileToShowIndex = index

        // Reset view
        if (!isSoftUpdate) {
            currentFilePath = null
            modelData.setGalleryViewImage(null)
            modelData.setGalleryViewVideo(null)
            modelData.setSaveButtonState(false)
            modelData.setIsGalleryShowRawImageScreenAsView(false)
        }

        // Get all files to view
        getAllAvailableFilesToView()

        // Is gallery empty
        if (filesToViewPaths.isEmpty()) { modelData.setIsEnableGalleryInProcessingCover(false)
            modelData.setIsGalleryEmpty(true); return }
        else { modelData.setIsGalleryEmpty(false) }

        // When file removed and index not exists
        if (fileToShowIndex > filesToViewPaths.size - 1) {
            fileToShowIndex = filesToViewPaths.size - 1 }

        // Image or video -> set to view

        currentFileIndex = fileToShowIndex
        currentFilePath = filesToViewPaths[currentFileIndex]
        val fileType = currentFilePath!!.substring(currentFilePath!!.length - 4)

        if (fileType == ".jpg") {
            // Delay for enable loading screen
            Handler(Looper.getMainLooper()).postDelayed({
                if (currentFilePath != null) {
                    // Read image
                    val imageBitmap = dataController.readImageAsBitmapByPath(currentFilePath!!)
                    // Set as bitmap to view in gallery
                    modelData.setGalleryViewImage(imageBitmap)
                    // May show save media to public storage button
                    modelData.setSaveButtonState(currentFilePath != null &&
                            currentFilePath!!.contains("/private_result_storage/"))
                }
            }, 64)
        }

        if (fileType == ".raw") {
            // Delay for enable loading screen
            Handler(Looper.getMainLooper()).postDelayed({
                if (currentFilePath != null) {
                    // Show image preview
                    modelData.setGalleryViewImage(null)
                    modelData.setIsGalleryShowRawImageScreenAsView(true)
                    // May show save media to public storage button
                    modelData.setSaveButtonState(currentFilePath != null &&
                            currentFilePath!!.contains("/private_result_storage/"))
                }
            }, 64)
        }

        if (fileType == ".mp4") {
            // Delay for enable loading screen
            Handler(Looper.getMainLooper()).postDelayed({
                if (currentFilePath != null) {
                    modelData.setGalleryViewVideo(currentFilePath)
                    // May show save media to public storage button
                    modelData.setSaveButtonState(currentFilePath != null &&
                            currentFilePath!!.contains("/private_result_storage/"))
                }
            }, 64)
        }

        // Get is file in processing
        val isFileInProcessing = currentFileIndex < inProcessingFilesCount

        // In processing cover
        if (!appSettings.getIsDisableInProcessingCover()) {
            modelData.setIsEnableGalleryInProcessingCover(isFileInProcessing)
        }

        // Start update loop, if file in processing. Or stop, when file is not in processing
        if (isFileInProcessing &&
            (viewUpdateLoopJob == null || viewUpdateLoopJob?.isActive == false)) {
            viewUpdateLoopJob = CoroutineScope(Dispatchers.Default).launch {
                viewUpdateLoop()
            }
        }
        if (!isFileInProcessing &&
            !(viewUpdateLoopJob == null || viewUpdateLoopJob?.isActive == false)) {
            viewUpdateLoopJob!!.cancel()
        }

        // Update buttons
        updateNextAndBackButtons()
    }


    private fun assignButtonCallbacks() {

        // Assign switch buttons callbacks
        uiEventHandler.assignNextButtonCallback {
            // Go to newer file
            if (currentFileIndex > 0) { showFile(currentFileIndex - 1) }
        }
        uiEventHandler.assignBackButtonCallback {
            // Go to older file
            if (currentFileIndex < filesToViewPaths.size - 1) {
                showFile(currentFileIndex + 1) }
        }

        // Assign close button callback
        uiEventHandler.assignCloseButtonCallback {
            close()
        }

        // Assign delete button callback
        uiEventHandler.assignGalleryDeleteButtonCallback {
            // Empty gallery EXC
            if (filesToViewPaths.isEmpty()) { return@assignGalleryDeleteButtonCallback }
            // Collection in processing EXC
            if (!dataController.isFileInPublicOrPrivateStorage(
                    filesToViewPaths[currentFileIndex])) {
                // Open popup
                modelData.openPopup("Share error", "Error",
                    "Photo in being processed", "Ok"
                ) {}
                // Exit
                return@assignGalleryDeleteButtonCallback
            }
            // Open popup Yes or No
            modelData.openPopup("Delete image", "Warning",
                "Do you want to delete this image?", "YesOrNo"
            ) { buttonId ->
                if (buttonId == "Yes") {
                    // Delete file by index
                    dataController.deleteFile(filesToViewPaths[currentFileIndex])
                    // Show new file by same index
                    showFile(currentFileIndex)
                }
            }
        }

        // Assign share button callback
        uiEventHandler.assignGalleryShareButtonCallback {
            // Empty gallery EXC
            if (filesToViewPaths.isEmpty()) { return@assignGalleryShareButtonCallback }

            // Get file path
            val filePath = filesToViewPaths[currentFileIndex]

            // Collection in processing EXC
            if (!dataController.isFileInPublicStorage(filePath)) {
                // Popup text: When photo being processed, When photo not saved to device
                val popupText = if (dataController.isFileInPublicOrPrivateStorage(filePath)) {
                    "First, save the file to the device" } else { "Photo in being processed" }
                // Open popup
                modelData.openPopup("Share error", "Error",
                    popupText, "Ok") {}
                // Exit
                return@assignGalleryShareButtonCallback
            }

            // Share file by path
            shareFile(mainActivity, filePath)
        }

        // Rotate image button
        uiEventHandler.assignGalleryImageRotateButtonCallback { degrees ->
            // Invalid state EXC
            if (isImageInRotationProcess || filesToViewPaths.isEmpty() ||
                currentFileIndex < inProcessingFilesCount || currentFilePath == null ||
                !(degrees == 90 || degrees == -90 || degrees == 180)) {
                return@assignGalleryImageRotateButtonCallback }
            // Set state
            isImageInRotationProcess = true
            // Remove image from view
            modelData.setGalleryViewImage(null)
            modelData.setIsEnableGalleryInProcessingCover(true)
            // Async call
            CoroutineScope(Dispatchers.Default).launch {
                // Rotate image
                dataController.rotateImage(currentFilePath!!, degrees)
                // Show result
                showFile(currentFileIndex)
                // Set state
                isImageInRotationProcess = false
            }
        }

        // Save media button
        uiEventHandler.assignSaveButtonCallback {
            // Illegal state EXC
            if (isFullAppVersionActive) { return@assignSaveButtonCallback }

            // If saves is not available
            if (currentMediaSavesCount <= 0 && !monetizationService.getIsFullVersionActive()) {
                // Open popup
                modelData.setGetMoreSavesPopupState(
                    true, monetizationService.getFullVersionPrice())
                return@assignSaveButtonCallback
            }

            // Use one save
            currentMediaSavesCount -= 1
            dataSaver.saveIntByKey("currentMediaSavesCount", currentMediaSavesCount)
            // Save current media file to public storage
            if (currentFilePath != null) {
                dataController.copyFileFromPrivateToPublicStorage(currentFilePath!!) }
            // Redraw Ui
            modelData.setAvailableSavesCountBarState(true, currentMediaSavesCount)
            showFile(currentFileIndex, true)
        }

        // Saves count bar button
        uiEventHandler.assignSavesCountBarButtonCallback {
            // Illegal state EXC
            if (isFullAppVersionActive) { return@assignSavesCountBarButtonCallback }
            // Open popup
            modelData.setGetMoreSavesPopupState(
                true, monetizationService.getFullVersionPrice())
        }

        // Get more saves popup buttons
        uiEventHandler.assignGetMoreSavesPopupButtonCallback { buttonId ->
            // Illegal state EXC
            if (isFullAppVersionActive) { return@assignGetMoreSavesPopupButtonCallback }
            // Process popup buttons
            if (buttonId == "Cancel") {
                modelData.setGetMoreSavesPopupState(false, "")
            }
            if (buttonId == "WatchAd") {
                modelData.setGetMoreSavesPopupState(false, "")
                monetizationService.showRewardedAd { isSuccess, message ->
                    if (isSuccess) {
                        rewardUserForRewardedAd()
                    } else {
                        modelData.openPopup(
                            "AD Error", "Error",
                            "Please, check Network connection and Try again later",
                            "Ok") {}
                        // Reward user if forced
                        if (appSettings.getIsRewardUserAfterAdFailed()) {
                            rewardUserForRewardedAd()
                        }
                    }
                }
            }
            if (buttonId == "BuyFullVersion") {
                modelData.setGetMoreSavesPopupState(false, "")
                monetizationService.buyFullVersion() {
                    setupMonetizationFeatures()
                }
            }
        }
    }


    private fun shareFile(context: Context, filePath: String) {
        val file = File(filePath)

        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)

            context.startActivity(Intent.createChooser(shareIntent, "Поделиться с помощью"))
        }
    }


    private fun close() {
        // Log
        Log.i(LOG_TAG, "Close")
        // Set state
        isGalleryOpen = false
        // Stop view update loop
        viewUpdateLoopJob?.cancel()
        // Open camera
        openDomainCallback("Camera")
    }


    private fun rewardUserForRewardedAd() {
        // Add media saves
        currentMediaSavesCount += appSettings.getFreeSavesRewardByWatchAd()
        dataSaver.saveIntByKey(
            "currentMediaSavesCount", currentMediaSavesCount)
        // Reshow saves count bar
        modelData.setAvailableSavesCountBarState(
            true, currentMediaSavesCount)
    }


    private fun setupMonetizationFeatures() {

        // Check is full version not active
        isFullAppVersionActive = monetizationService.getIsFullVersionActive()
        if (!isFullAppVersionActive) {

            // Load currentMediaSavesCount
            currentMediaSavesCount = dataSaver.loadIntByKey("currentMediaSavesCount") ?:
            appSettings.getCountOfMediaSavesOnStart()

            // Activate ui elements
            modelData.setAvailableSavesCountBarState(true, currentMediaSavesCount)
        } else {
            modelData.setAvailableSavesCountBarState(false, currentMediaSavesCount)
        }
    }


    // Public


    fun open() {

        // Log
        Log.i(LOG_TAG, "Open")

        // Reset variables
        currentFileIndex = 0

        // Show file by 0 index
        showFile(0)

        // Assign callbacks
        assignButtonCallbacks()

        // Update buttons
        updateNextAndBackButtons()

        // Monetization
        setupMonetizationFeatures()

        // Set state. Wait until gallery open, before allow to close it
        Handler(Looper.getMainLooper()).postDelayed({ isGalleryOpen = true }, 64)
    }

    // Provide callbacks
    fun onNewSystemEvent(newEvent: String) {
        if (newEvent == "onGoBackEvent" && isGalleryOpen) { close() }
    }
}