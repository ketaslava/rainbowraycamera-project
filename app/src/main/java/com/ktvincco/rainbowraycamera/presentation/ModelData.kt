package com.ktvincco.rainbowraycamera.presentation


import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Range
import android.util.Size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.ktvincco.rainbowraycamera.domain.util.StandardImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class ModelData {


    // Settings


    companion object {
        const val LOG_TAG = "myLogs"
    }


    // Universal


    // Navigation buttons Next and Back active
    private val _isNextButtonActive = MutableStateFlow<Boolean>(true)
    val isNextButtonActive: StateFlow<Boolean> = _isNextButtonActive
    fun setIsNextButtonActive(newValue: Boolean) {
        _isNextButtonActive.value = newValue }
    private val _isBackButtonActive = MutableStateFlow<Boolean>(true)
    val isBackButtonActive: StateFlow<Boolean> = _isBackButtonActive
    fun setIsBackButtonActive(newValue: Boolean) {
        _isBackButtonActive.value = newValue }


    // Confirm button active
    private val _isConfirmButtonActive = MutableStateFlow<Boolean>(true)
    val isConfirmButtonActive: StateFlow<Boolean> = _isConfirmButtonActive
    fun setIsConfirmButtonActive(newValue: Boolean) {
        _isConfirmButtonActive.value = newValue }


    // Current page index
    private val _currentPageIndex = MutableStateFlow<Int>(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex
    fun setCurrentPageIndex(newValue: Int) {
        _currentPageIndex.value = newValue }


    // Save button
    private val _isEnableSaveButton = MutableStateFlow<Boolean>(false)
    val isEnableSaveButton: StateFlow<Boolean> = _isEnableSaveButton
    fun setSaveButtonState(newValue: Boolean) { _isEnableSaveButton.value = newValue }


    // Slider
    private val _sliderTarget = MutableStateFlow("Disabled")
    val sliderTarget: StateFlow<String> = _sliderTarget
    fun disableSlider() { _sliderTarget.value = "Disabled" }
    fun setSliderTargetIso() { _sliderTarget.value = "Iso" }
    fun setSliderTargetShutterSpeed() { _sliderTarget.value = "ShutterSpeed" }
    fun setSliderTargetFocus() { _sliderTarget.value = "Focus" }
    fun setSliderTargetWhiteBalance() { _sliderTarget.value = "WhiteBalance" }


    // Camera


    // Camera mode
    private val _cameraMode = MutableStateFlow("")
    val cameraMode: StateFlow<String> = _cameraMode
    fun setCameraMode(newValue: String) { _cameraMode.value = newValue }


    // Camera preview image
    private val _cameraPreviewImage = MutableStateFlow<StandardImage?>(null)
    val cameraPreviewImage: StateFlow<StandardImage?> = _cameraPreviewImage
    fun setCameraPreviewImage(newValue: StandardImage?) { _cameraPreviewImage.value = newValue }


    // Camera preview image
    private val _isEnableSurfacePreview = MutableStateFlow<Boolean>(false)
    val isEnableSurfacePreview: StateFlow<Boolean> = _isEnableSurfacePreview
    fun setIsEnableSurfacePreview(newValue: Boolean) { _isEnableSurfacePreview.value = newValue }


    // previewSurface aspect ratio
    private val _previewSurfaceAspectRatio = MutableStateFlow<Float>(1.0F)
    val previewSurfaceAspectRatio: StateFlow<Float> = _previewSurfaceAspectRatio
    fun setPreviewSurfaceAspectRatio(newValue: Float) {
        _previewSurfaceAspectRatio.value = newValue }


    // Capture media state
    private val _isCaptureMedaNow = MutableStateFlow<Boolean>(false)
    val isCaptureMedaNow: StateFlow<Boolean> = _isCaptureMedaNow
    fun setIsCaptureMedaNow(newValue: Boolean) {
        _isCaptureMedaNow.value = newValue }


    // Stabilization button state
    private val _stabilizedCaptureButtonState = MutableStateFlow<Int>(0)
    val stabilizedCaptureButtonState: StateFlow<Int> = _stabilizedCaptureButtonState
    fun setStabilizedCaptureButtonState(newValue: Int) {
        _stabilizedCaptureButtonState.value = newValue }


    // Is enable capture settings menu
    private val _isEnableCaptureSettingsMenu = MutableStateFlow<Boolean>(false)
    val isEnableCaptureSettingsMenu: StateFlow<Boolean> = _isEnableCaptureSettingsMenu
    fun setIsEnableCaptureSettingsMenu(newValue: Boolean) {
        _isEnableCaptureSettingsMenu.value = newValue }


    // Is enable alignment cursor position
    private val _isEnableAlignmentCursor = MutableStateFlow(false)
    val isEnableAlignmentCursor: StateFlow<Boolean> = _isEnableAlignmentCursor
    fun setIsEnableAlignmentCursor(newValue: Boolean) {
        _isEnableAlignmentCursor.value = newValue }


    // Alignment cursor position
    private val _alignmentCursorPosition = MutableStateFlow(Triple(0F, 0F, 0F))
    val alignmentCursorPosition: StateFlow<Triple<Float, Float, Float>> = _alignmentCursorPosition
    fun setAlignmentCursorPosition(newValue: Triple<Float, Float, Float>) {
        _alignmentCursorPosition.value = newValue }


    // Camera UI mode
    private val _nightModeSwitchButtonState = MutableStateFlow("Disabled")
    val nightModeSwitchButtonState: StateFlow<String> = _nightModeSwitchButtonState
    fun disableNightModeSwitchButton() { _nightModeSwitchButtonState.value = "Disabled" }
    fun enableNightModeSwitchButtonAsEnter() { _nightModeSwitchButtonState.value = "Enter" }
    fun enableNightModeSwitchButtonAsExit() { _nightModeSwitchButtonState.value = "Exit" }


    // Blink screen
    private val _blinkScreenWhileCapturePhotoState = MutableStateFlow(false)
    val blinkScreenWhileCapturePhotoState: StateFlow<Boolean> = _blinkScreenWhileCapturePhotoState
    fun blinkScreenWhileCapturePhoto() {
        _blinkScreenWhileCapturePhotoState.value = true
        Handler(Looper.getMainLooper()).postDelayed({
            _blinkScreenWhileCapturePhotoState.value = false
        }, 128)
    }

    // Exposure
    // Is auto exposure enabled
    private val _isAutoExposureEnabled = MutableStateFlow(true)
    val isAutoExposureEnabled: StateFlow<Boolean> = _isAutoExposureEnabled
    fun setAutoExposureState(newValue: Boolean) { _isAutoExposureEnabled.value = newValue }
    // Manual control range
    private val _exposureIsoRange = MutableStateFlow(Range(100, 1000))
    val exposureIsoRange: StateFlow<Range<Int>> = _exposureIsoRange
    fun setExposureIsoRange(newValue: Range<Int>) { _exposureIsoRange.value = newValue }
    private val _exposureShutterSpeedRange =
        MutableStateFlow(Range(16000000L, 64000000L))
    val exposureShutterSpeedRange: StateFlow<Range<Long>> = _exposureShutterSpeedRange
    fun setExposureShutterSpeedRange(
        newValue: Range<Long>) { _exposureShutterSpeedRange.value = newValue }

    // Auto focus
    private val _isEnableAutoFocus = MutableStateFlow(true)
    val isEnableAutoFocus: StateFlow<Boolean> = _isEnableAutoFocus
    fun setAutoFocusState(newValue: Boolean) { _isEnableAutoFocus.value = newValue }

    // Auto white balance
    private val _isAutoWhiteBalanceEnabled = MutableStateFlow(true)
    val isAutoWhiteBalanceEnabled: StateFlow<Boolean> = _isAutoWhiteBalanceEnabled
    fun setAutoWhiteBalanceState(newValue: Boolean) { _isAutoWhiteBalanceEnabled.value = newValue }

    // Camera settings
    private val _cameraOptionStates = MutableStateFlow<MutableMap<String, String>>(mutableMapOf())
    private val cameraOptionStates: StateFlow<MutableMap<String, String>> = _cameraOptionStates
    private val _cameraOptionStatesUpdater = MutableStateFlow(0)
    private val cameraOptionStatesUpdater: StateFlow<Int> = _cameraOptionStatesUpdater
    fun setCameraOptionState(optionName: String, newValue: String) {
        _cameraOptionStates.value[optionName] = newValue
        _cameraOptionStatesUpdater.value += 1 }
    @Composable
    fun getCameraOptionState(optionName: String): String? {
        cameraOptionStatesUpdater.collectAsState().value
        return cameraOptionStates.collectAsState().value[optionName] }
    @Composable
    fun getCameraOptionStateBoolean(optionName: String): Boolean? {
        cameraOptionStatesUpdater.collectAsState().value
        return cameraOptionStates.collectAsState().value[optionName]?.toBoolean() }


    // Gallery


    // Empty gallery
    private val _isGalleryEmpty = MutableStateFlow<Boolean>(false)
    val isGalleryEmpty: StateFlow<Boolean> = _isGalleryEmpty
    fun setIsGalleryEmpty(newValue: Boolean) { _isGalleryEmpty.value = newValue }


    // Gallery view image
    private val _galleryViewImage = MutableStateFlow<Bitmap?>(null)
    val galleryViewImage: StateFlow<Bitmap?> = _galleryViewImage
    fun setGalleryViewImage(newValue: Bitmap?) { _galleryViewImage.value = newValue }


    // Show RAW image screen
    private val _isGalleryShowRawImageScreenAsView = MutableStateFlow(false)
    val isGalleryShowRawImageScreenAsView: StateFlow<Boolean> = _isGalleryShowRawImageScreenAsView
    fun setIsGalleryShowRawImageScreenAsView(newValue: Boolean) {
        _isGalleryShowRawImageScreenAsView.value = newValue }


    // Gallery view video
    private val _galleryViewVideo = MutableStateFlow<String?>(null)
    val galleryViewVideo: StateFlow<String?> = _galleryViewVideo
    fun setGalleryViewVideo(newValue: String?) { _galleryViewVideo.value = newValue }


    // In processing cover
    private val _isEnableGalleryInProcessingCover = MutableStateFlow<Boolean>(false)
    val isEnableGalleryInProcessingCover: StateFlow<Boolean> = _isEnableGalleryInProcessingCover
    fun setIsEnableGalleryInProcessingCover(newValue: Boolean) {
        _isEnableGalleryInProcessingCover.value = newValue }


    // Monetization
    // Saves count bar
    private val _isEnableAvailableSavesCountBar = MutableStateFlow<Boolean>(false)
    val isEnableAvailableSavesCountBar: StateFlow<Boolean> = _isEnableAvailableSavesCountBar
    private val _currentSavesCountForSavesCountBar = MutableStateFlow<Int>(0)
    val currentSavesCountForSavesCountBar: StateFlow<Int> = _currentSavesCountForSavesCountBar
    fun setAvailableSavesCountBarState(isEnable: Boolean, currentCount: Int) {
        _isEnableAvailableSavesCountBar.value = isEnable
        _currentSavesCountForSavesCountBar.value = currentCount
    }

    // "Get more saves" popup
    private val _isEnableGetMoreSavesPopup = MutableStateFlow(false)
    val isEnableGetMoreSavesPopup: StateFlow<Boolean> = _isEnableGetMoreSavesPopup
    private val _getMoreSavesPopupFullVersionPrice = MutableStateFlow("")
    val getMoreSavesPopupFullVersionPrice: StateFlow<String> = _getMoreSavesPopupFullVersionPrice
    fun setGetMoreSavesPopupState(newValue: Boolean, fullVersionPrice: String) {
        _isEnableGetMoreSavesPopup.value = newValue
        if (fullVersionPrice != "") { _getMoreSavesPopupFullVersionPrice.value = fullVersionPrice }
    }


    // Benchmarking


    // Max benchmark timer
    private val _maxBenchmarkTimeSec = MutableStateFlow<Int>(0)
    val maxBenchmarkTimeSec: StateFlow<Int> = _maxBenchmarkTimeSec
    fun setMaxBenchmarkTimeSec(newValue: Int) {
        _maxBenchmarkTimeSec.value = newValue }


    // Progress text
    private val _benchmarkProgressText = MutableStateFlow<String>("Loading...")
    val benchmarkProgressText: StateFlow<String> = _benchmarkProgressText
    fun setBenchmarkProgressText(newValue: String) {
        _benchmarkProgressText.value = newValue }


    // Pages


    // Page system
    private val _activePages = MutableStateFlow<List<String>>(emptyList())
    val activePages: StateFlow<List<String>> = _activePages
    private fun openPage(name: String) {
        if (!_activePages.value.contains(name)) {
            _activePages.value += name
        }
        Log.i(LOG_TAG, "Open page: $name, arr: ${_activePages.value}")
    }
    private fun closePage(name: String) {
        if (_activePages.value.contains(name)) {
            _activePages.value -= name
        }
        Log.i(LOG_TAG, "Close page: $name, arr: ${_activePages.value}")
    }
    fun closeAllPages() {
        _activePages.value = emptyList()
        Log.i(LOG_TAG, "Close all pages")
    }
    fun openCamera() { closeAllPages();
        openPage("CameraControl"); openPage("CameraPreview") }
    fun openGalley() { closeAllPages(); openPage("Gallery") }
    fun openAboutApp() { closeAllPages(); openPage("AboutApp") }
    fun openEnableFlashlightOption() { closeAllPages(); openPage("EnableFlashlightOption") }
    fun openStartupScreen() { closeAllPages(); openPage("StartupScreen") }
    fun openAccessDeniedPage() { closeAllPages(); openPage("AccessDenied") }
    fun openUpdateRequiredPage() { closeAllPages(); openPage("UpdateRequired") }
    fun openBenchmarkScreen() { closeAllPages(); openPage("BenchmarkScreen") }


    // Dialog windows


    // Selector
    private val _selectorLabel = MutableStateFlow<String>("Selector")
    val selectorLabel: StateFlow<String> = _selectorLabel
    private val _selectorOptions = MutableStateFlow<List<String>>(emptyList())
    val selectorOptions: StateFlow<List<String>> = _selectorOptions
    private val _selectorDefaultOption = MutableStateFlow<Int>(0)
    val selectorDefaultOption: StateFlow<Int> = _selectorDefaultOption
    private var _whenSelectorOptionSelected: (optionIndex: Int) -> Unit = {}
    fun selectorOptionSelected(optionIndex: Int) { _whenSelectorOptionSelected(optionIndex)
        closePage("Selector") }
    fun openSelector(newLabel: String, optionsArray: List<String>, defaultOptionIndex: Int,
        whenOptionSelected: (optionIndex: Int) -> Unit
    ) { _selectorLabel.value = newLabel; _selectorOptions.value = optionsArray
        _selectorDefaultOption.value = defaultOptionIndex
        _whenSelectorOptionSelected = whenOptionSelected
        openPage("Selector")
    }
    fun selector2OptionSelected(optionIndex: Int) { _whenSelectorOptionSelected(optionIndex)
        closePage("Selector2") }
    fun openSelector2(newLabel: String, optionsArray: List<String>, defaultOptionIndex: Int,
                     whenOptionSelected: (optionIndex: Int) -> Unit
    ) { _selectorLabel.value = newLabel; _selectorOptions.value = optionsArray
        _selectorDefaultOption.value = defaultOptionIndex
        _whenSelectorOptionSelected = whenOptionSelected
        openPage("Selector2")
    }
    fun cameraModeSelectorOptionSelected(optionIndex: Int) {
        _whenSelectorOptionSelected(optionIndex); closePage("CameraModeSelector") }
    fun openCameraModeSelector(
        defaultOptionIndex: Int, whenOptionSelected: (optionIndex: Int) -> Unit
    ) {
        _selectorDefaultOption.value = defaultOptionIndex
        _whenSelectorOptionSelected = whenOptionSelected
        openPage("CameraModeSelector")
    }


    // Video size selector
    private val _videoSizes = MutableStateFlow<List<Size>>(emptyList())
    val videoSizes: StateFlow<List<Size>> = _videoSizes
    private val _availableVideoFrameRatesBySizeMap =
        MutableStateFlow<Map<Size, ArrayList<Int>>>(mapOf())
    val availableVideoFrameRatesBySizeMap: StateFlow<
            Map<Size, ArrayList<Int>>> = _availableVideoFrameRatesBySizeMap
    private val _defaultVideoSizeIndex = MutableStateFlow<Int>(0)
    val defaultVideoSizeIndex: StateFlow<Int> = _defaultVideoSizeIndex
    private val _defaultVideoFrameRateIndex = MutableStateFlow<Int>(0)
    val defaultVideoFrameRateIndex: StateFlow<Int> = _defaultVideoFrameRateIndex
    private var _whenSelectorVideoSizeSelected: (
        videoSizeIndex: Int, frameRateIndex: Int) -> Unit = { _, _ -> }
    fun openSelectorVideoSize(
        newLabel: String,
        videoSizes: List<Size>,
        defaultVideoSizeIndex: Int,
        availableVideoFrameRatesBySizeMap: Map<Size, ArrayList<Int>>,
        defaultVideoFrameRateIndex: Int,
        whenSelectorVideoSizeSelected: (videoSizeIndex: Int, frameRateIndex: Int) -> Unit
    ) {
        _selectorLabel.value = newLabel
        _videoSizes.value = videoSizes
        _availableVideoFrameRatesBySizeMap.value = availableVideoFrameRatesBySizeMap
        _defaultVideoSizeIndex.value = defaultVideoSizeIndex
        _defaultVideoFrameRateIndex.value = defaultVideoFrameRateIndex
        _whenSelectorVideoSizeSelected = whenSelectorVideoSizeSelected
        openPage("SelectorVideoSize")
    }
    fun selectorVideoSizeSelected(videoSizeIndex: Int, frameRateIndex: Int) {
        _whenSelectorVideoSizeSelected(videoSizeIndex, frameRateIndex)
        closePage("SelectorVideoSize")
    }


    // Popup
    private val _popupLabel = MutableStateFlow<String>("Popup")
    val popupLabel: StateFlow<String> = _popupLabel
    private val _popupLabelType = MutableStateFlow<String>("Info")
    val popupLabelType: StateFlow<String> = _popupLabelType
    private val _popupText = MutableStateFlow<String>("Popup")
    val popupText: StateFlow<String> = _popupText
    private val _popupButtonsType = MutableStateFlow<String>("Ok")
    val popupButtonsType: StateFlow<String> = _popupButtonsType
    private var _whenPopupButtonClicked: (buttonId: String) -> Unit = {}
    fun popupButtonClicked(buttonId: String) { closePage("Popup")
        _whenPopupButtonClicked(buttonId) }
    fun openPopup(newLabel: String, newPopupLabelType: String, newPopupText: String,
            newPopupButtonsType: String, newWhenPopupButtonClicked: (buttonId: String) -> Unit
    ) { _popupLabel.value = newLabel; _popupLabelType.value = newPopupLabelType
        _popupText.value = newPopupText; _popupButtonsType.value = newPopupButtonsType
        _whenPopupButtonClicked = newWhenPopupButtonClicked
        openPage("Popup")
    }
}