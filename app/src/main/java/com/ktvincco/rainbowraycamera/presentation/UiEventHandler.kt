package com.ktvincco.rainbowraycamera.presentation


import android.view.SurfaceView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class UiEventHandler (private val modelData: ModelData) {


    // Settings


    companion object {
        const val LOG_TAG = "myLogs"
    }


    // Universal


    // Gallery switch buttons
    private var nextButtonCallback: () -> Unit = {}
    private var backButtonCallback: () -> Unit = {}
    fun assignNextButtonCallback(callback: () -> Unit) {
        nextButtonCallback = callback }
    fun assignBackButtonCallback(callback: () -> Unit) {
        backButtonCallback = callback }
    fun nextButtonClicked() { nextButtonCallback() }
    fun backButtonClicked() { backButtonCallback() }


    // Confirm button
    private var confirmButtonCallback: () -> Unit = {}
    fun assignConfirmButtonCallback(callback: () -> Unit) {
        confirmButtonCallback = callback }
    fun confirmButtonClicked() { confirmButtonCallback() }


    // Close button
    private var closeButtonCallback: () -> Unit = {}
    fun assignCloseButtonCallback(callback: () -> Unit) {
        closeButtonCallback = callback }
    fun closeButtonClicked() { closeButtonCallback() }


    // Close button
    private var checkboxCallback: (name: String, value: Boolean) -> Unit = { _, _ -> }
    fun assignCheckboxCallback(callback: (name: String, value: Boolean) -> Unit) {
        checkboxCallback = callback }
    fun checkboxSwitched(name: String, value: Boolean) { checkboxCallback(name, value) }


    // Save button
    private var saveButtonCallback: () -> Unit = {}
    fun assignSaveButtonCallback(callback: () -> Unit) { saveButtonCallback = callback }
    fun onSaveButtonClicked() { saveButtonCallback() }


    // Camera


    // Main preview SurfaceView
    private val _mainPreviewSurfaceView = MutableStateFlow<SurfaceView?>(null)
    val mainPreviewSurfaceView: StateFlow<SurfaceView?> = _mainPreviewSurfaceView
    fun setMainPreviewSurfaceView(surfaceView: SurfaceView?) {
        _mainPreviewSurfaceView.value = surfaceView
    }


    // Manual Exposure
    private val _manualExposureIso = MutableStateFlow(100)
    val manualExposureIso: StateFlow<Int> = _manualExposureIso
    fun setManualExposureIso(newValue: Int) { _manualExposureIso.value = newValue }
    private val _manualExposureShutterSpeed = MutableStateFlow(16000000L)
    val manualExposureShutterSpeed: StateFlow<Long> = _manualExposureShutterSpeed
    fun setManualExposureShutterSpeed(newValue: Long) {
        _manualExposureShutterSpeed.value = newValue }


    // Focus
    private val _manualFocusValue = MutableStateFlow(0.0F)
    val manualFocusValue: StateFlow<Float> = _manualFocusValue
    fun setManualFocusValue(newValue: Float) { _manualFocusValue.value = newValue }


    // White Balance
    private val _manualWhiteBalance = MutableStateFlow(Pair(0.5F, 0.5F))
    val manualWhiteBalance: StateFlow<Pair<Float, Float>> = _manualWhiteBalance
    fun setManualWhiteBalance(newValue: Pair<Float, Float>) { _manualWhiteBalance.value = newValue }


    // Switch mode button
    private var switchModeButtonCallback: () -> Unit = {}
    fun assignSwitchModeButtonCallback(callback: () -> Unit) {
        switchModeButtonCallback = callback }
    fun switchModeButtonClicked() { switchModeButtonCallback() }


    // Switch camera button
    private var switchCameraButtonCallback: () -> Unit = {}
    fun assignSwitchCameraButtonCallback(callback: () -> Unit) {
        switchCameraButtonCallback = callback }
    fun switchCameraButtonClicked() { switchCameraButtonCallback() }


    // Capture button
    private var captureButtonCallback: () -> Unit = {}
    fun assignCaptureButtonCallback(callback: () -> Unit) {captureButtonCallback = callback}
    fun captureButtonClicked() {captureButtonCallback()}


    // Resolution button
    private var outputSizeButtonCallback: () -> Unit = {}
    fun assignOutputSizeButtonCallback(callback: () -> Unit) {outputSizeButtonCallback = callback}
    fun outputSizeButtonClicked() {outputSizeButtonCallback()}


    // Focus point
    private var onFocusPointUpdate: (newPosition: Pair<Float, Float>) -> Unit = {}
    fun assignFocusPointUpdateCallback(callback: (newPosition: Pair<Float, Float>) -> Unit) {
        onFocusPointUpdate = callback}
    fun focusPointUpdate(newPosition: Pair<Float, Float>) { onFocusPointUpdate(newPosition) }


    // Focus button
    private var onFocusButtonClicked: () -> Unit = {}
    fun assignFocusButtonCallback(callback: () -> Unit) {
        onFocusButtonClicked = callback}
    fun focusButtonClicked() { onFocusButtonClicked() }


    // Flashlight button
    private var onToggleFlashlightButtonClicked: () -> Unit = {}
    fun assignToggleFlashlightButtonCallback(callback: () -> Unit) {
        onToggleFlashlightButtonClicked = callback}
    fun toggleFlashlightButtonClicked() { onToggleFlashlightButtonClicked() }


    // Enable Flashlight option button
    private var onToggleFlashlightOptionButtonClicked: () -> Unit = {}
    fun assignToggleFlashlightOptionButtonCallback(callback: () -> Unit) {
        onToggleFlashlightOptionButtonClicked = callback}
    fun toggleFlashlightOptionButtonClicked() { onToggleFlashlightOptionButtonClicked() }


    // Stabilized capture button
    private var onStabilizedCaptureButtonClicked: () -> Unit = {}
    fun assignStabilizedCaptureButtonCallback(callback: () -> Unit) {
        onStabilizedCaptureButtonClicked = callback }
    fun stabilizedCaptureButtonClicked() { onStabilizedCaptureButtonClicked() }


    // Open and close capture settings button
    private var onCaptureSettingsButton: () -> Unit = {}
    fun assignCaptureSettingsButtonCallback(callback: () -> Unit) {
        onCaptureSettingsButton = callback }
    fun captureSettingsButtonClicked() { onCaptureSettingsButton() }


    // Zoom
    private val _cameraZoom = MutableStateFlow(1F)
    val cameraZoom: StateFlow<Float> = _cameraZoom
    fun setCameraZoom(newValue: Float) {
        _cameraZoom.value = newValue
    }


    // Open and close about app
    private var onOpenAboutAppButtonClicked: () -> Unit = {}
    fun assignOnOpenAboutAppButtonClicked(callback: () -> Unit) {
        onOpenAboutAppButtonClicked = callback }
    fun openAboutAppButtonClicked() { onOpenAboutAppButtonClicked() }
    private var onCloseAboutAppButtonClicked: () -> Unit = {}
    fun assignOnCloseAboutAppButtonClicked(callback: () -> Unit) {
        onCloseAboutAppButtonClicked = callback }
    fun closeAboutAppButtonClicked() { onCloseAboutAppButtonClicked() }


    // Switch night mode button
    private var switchNightModeButton: () -> Unit = {}
    fun assignSwitchNightModeButtonCallback(callback: () -> Unit) {
        switchNightModeButton = callback }
    fun switchNightModeButtonClicked() { switchNightModeButton() }


    // Camera options


    // Capture sound
    private var switchIsEnableCaptureSoundCallback: () -> Unit = {}
    fun assignSwitchIsEnableCaptureSoundCallback(callback: () -> Unit) {
        switchIsEnableCaptureSoundCallback = callback }
    fun switchIsEnableCaptureSound() { switchIsEnableCaptureSoundCallback() }


    // Record audio
    private var switchIsEnableRecordAudioCallback: () -> Unit = {}
    fun assignSwitchIsEnableRecordAudioCallback(callback: () -> Unit) {
        switchIsEnableRecordAudioCallback = callback }
    fun switchIsEnableRecordAudio() { switchIsEnableRecordAudioCallback() }


    // Grid
    private var switchIsEnableGridCallback: () -> Unit = {}
    fun assignSwitchIsEnableGridCallback(callback: () -> Unit) {
        switchIsEnableGridCallback = callback }
    fun switchIsEnableGrid() { switchIsEnableGridCallback() }


    // FocusPeaking
    private var switchIsEnableFocusPeakingCallback: () -> Unit = {}
    fun assignSwitchIsEnableFocusPeakingCallback(callback: () -> Unit) {
        switchIsEnableFocusPeakingCallback = callback }
    fun switchIsEnableFocusPeaking() { switchIsEnableFocusPeakingCallback() }


    // Ois
    private var switchIsEnableOisCallback: () -> Unit = {}
    fun assignSwitchIsEnableOisCallback(callback: () -> Unit) {
        switchIsEnableOisCallback = callback }
    fun switchIsEnableOis() { switchIsEnableOisCallback() }


    // Ois
    private var switchIsUseRawSensorFormatWhenAvailableCallback: () -> Unit = {}
    fun assignSwitchIsUseRawSensorFormatWhenAvailable(callback: () -> Unit) {
        switchIsUseRawSensorFormatWhenAvailableCallback = callback }
    fun switchIsUseRawSensorFormatWhenAvailable() {
        switchIsUseRawSensorFormatWhenAvailableCallback() }


    // Gallery


    // Open gallery button
    private var openGalleryButtonCallback: () -> Unit = {}
    fun assignOpenGalleryButtonCallback(callback: () -> Unit) {
        openGalleryButtonCallback = callback }
    fun openGalleryButtonClicked() { openGalleryButtonCallback() }


    // Gallery delete button
    private var galleryDeleteButtonCallback: () -> Unit = {}
    fun assignGalleryDeleteButtonCallback(callback: () -> Unit) {
        galleryDeleteButtonCallback = callback }
    fun galleryDeleteButtonClicked() { galleryDeleteButtonCallback() }


    // Gallery share button
    private var galleryShareButtonCallback: () -> Unit = {}
    fun assignGalleryShareButtonCallback(callback: () -> Unit) {
        galleryShareButtonCallback = callback }
    fun galleryShareButtonClicked() { galleryShareButtonCallback() }


    // Gallery image rotate button
    private var galleryImageRotateButtonCallback: (degrees: Int) -> Unit = {}
    fun assignGalleryImageRotateButtonCallback(callback: (degrees: Int) -> Unit) {
        galleryImageRotateButtonCallback = callback }
    fun galleryImageRotateButtonClicked(degrees: Int) { galleryImageRotateButtonCallback(degrees) }


    // Saves count bar button
    private var savesCountBarButtonCallback: () -> Unit = {}
    fun assignSavesCountBarButtonCallback(callback: () -> Unit) {
        savesCountBarButtonCallback = callback }
    fun onSavesCountBarButtonClicked() {
        savesCountBarButtonCallback() }


    // Get more saves popup
    private var getMoreSavesPopupButtonCallback: (buttonId: String) -> Unit = {}
    fun assignGetMoreSavesPopupButtonCallback(callback: (buttonId: String) -> Unit) {
        getMoreSavesPopupButtonCallback = callback }
    fun onGetMoreSavesPopupButtonClicked(buttonId: String) {
        getMoreSavesPopupButtonCallback(buttonId) }

}