package com.ktvincco.rainbowraycamera


class AppSettings {


    // Settings


    // Build configuration and important settings
    private val endOfBuildLifetime: Long = 1900569600 // Unix // 24 Mar of 2030 1:00:00 AM
    private val startupScreenVersion: Int = 6 // Value > 0 // !!! MAY CHANGE BEFORE RELEASE !!!
    private val termsOfUseLink: String =
        "https://ktvincco.com/rainbowraycamera/termsofuse/"
    private val privacyPolicyLink: String =
        "https://ktvincco.com/rainbowraycamera/privacypolicy/"
    private val linkToTheApplicationInGooglePlayMarket: String =
        "https://play.google.com/store/apps/details?id=com.ktvincco.rainbowraycamera"

    // Startup
    private val isAlwaysShowStartupScreen: Boolean = false // false

    // Camera
    private val cameraPreviewImageSizeFactor = 12 // 12
    private val targetImagesCountForPhotoCollection = 10 // 1 + 1 + 8
    private val timeoutBetweenCaptureOptionUpdatesMillis: Long = 16 // 16
    private val stabilizedCaptureTargetStabRate: Float = 0.001F // 0.001F

    // Content processing
    private val isEnableBackgroundImageProcessing = true // true

    // Gallery
    private val isDisableInProcessingCover: Boolean = false // false

    // Monetization
    private val countOfMediaSavesOnStart = 16
    private val freeSavesRewardByWatchAd = 64
    private val isRewardUserAfterAdFailed = true

    // Monetization (AD)
    private val isEnableAdTestMode = false // !!! CHANGE BEFORE RELEASE (3/3) !!!
    private val rewardedAdUnit = "AdmobRewarded" // AdmobRewarded, AdmobRewardedInterstitial
    private val admobRewardedAdUnitId = "ca-app-pub-3343103877905532/9984044412"
    private val testAdmobRewardedAdUnitId = "ca-app-pub-3940256099942544/5224354917"
    private val admobRewardedInterstitialAdUnitId = "ca-app-pub-3343103877905532/3023151871"
    private val testAdmobRewardedInterstitialAdUnitId = "ca-app-pub-3940256099942544/5354046379"


    // Get settings


    // Build configuration and important settings
    fun getEndOfBuildLifetime(): Long { return endOfBuildLifetime }
    fun getStartupScreenVersion(): Int { return startupScreenVersion }
    fun getTermsOfUseLink(): String { return termsOfUseLink }
    fun getPrivacyPolicyLink(): String { return privacyPolicyLink }
    fun getLinkToTheApplicationInGooglePlayMarket(): String {
        return linkToTheApplicationInGooglePlayMarket }

    // Startup
    fun getIsAlwaysShowStartupScreen(): Boolean { return isAlwaysShowStartupScreen }

    // Camera
    fun getCameraPreviewImageSizeFactor(): Int { return cameraPreviewImageSizeFactor }
    fun getTargetImagesCountForPhotoCollection(): Int {
        return targetImagesCountForPhotoCollection }
    fun getTimeoutBetweenCaptureOptionUpdatesMillis(): Long {
        return timeoutBetweenCaptureOptionUpdatesMillis }
    fun getStabilizedCaptureTargetStabRate(): Float { return stabilizedCaptureTargetStabRate }

    // Content processing
    fun getIsEnableBackgroundImageProcessing(): Boolean {
        return isEnableBackgroundImageProcessing }

    // Gallery
    fun getIsDisableInProcessingCover(): Boolean { return isDisableInProcessingCover }

    // Monetization
    fun getCountOfMediaSavesOnStart(): Int { return countOfMediaSavesOnStart }
    fun getFreeSavesRewardByWatchAd(): Int { return freeSavesRewardByWatchAd }
    fun getIsRewardUserAfterAdFailed(): Boolean { return isRewardUserAfterAdFailed }

    // Monetization (AD)
    fun getIsEnableAdTestMode(): Boolean { return isEnableAdTestMode }
    fun getRewardedAdUnit(): String { return rewardedAdUnit }
    fun getAdmobRewardedAdUnitId(): String { return admobRewardedAdUnitId }
    fun getTestAdmobRewardedAdUnitId(): String { return testAdmobRewardedAdUnitId }
    fun getAdmobRewardedInterstitialAdUnitId(): String {
        return admobRewardedInterstitialAdUnitId }
    fun getTestAdmobRewardedInterstitialAdUnitId(): String {
        return testAdmobRewardedInterstitialAdUnitId }

}