package com.ktvincco.rainbowraycamera.domain


import com.ktvincco.rainbowraycamera.data.PermissionController
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.data.util.CameraConfiguration
import com.ktvincco.rainbowraycamera.data.CameraController
import com.ktvincco.rainbowraycamera.data.DataSaver
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ktvincco.rainbowraycamera.AppSettings
import com.ktvincco.rainbowraycamera.domain.component.AppUsageAccessControl
import com.ktvincco.rainbowraycamera.domain.component.ContentPostProcessor
import com.ktvincco.rainbowraycamera.domain.component.MonetizationService
import com.ktvincco.rainbowraycamera.domain.component.TelemetryService


class Main (private var mainActivity: Activity, private val modelData: ModelData,
            private val uiEventHandler: UiEventHandler) {


    // Settings


    companion object {
        const val LOG_TAG = "Main"
    }


    // Variables


    // Data
    private var cameraConfigurations: List<CameraConfiguration> = arrayListOf()
    // Components
    private val appUsageAccessControl = AppUsageAccessControl()
    private val permissionController = PermissionController(mainActivity)
    private var cameraController: CameraController? = null
    private val dataController = DataSaver(mainActivity)
    private var dataSaver = DataSaver(mainActivity)
    private val monetizationService = MonetizationService(mainActivity, dataSaver)
    private val contentPostProcessor = ContentPostProcessor(
        mainActivity, dataSaver, monetizationService)
    // Domains
    private var camera: Camera? = null
    private val gallery = Gallery(mainActivity, modelData, uiEventHandler,
        dataController, monetizationService, dataSaver) { domainName ->  openDomain(domainName) }
    private val startup = Startup(mainActivity, modelData, uiEventHandler,
        dataSaver) { checkState() }
    private val telemetryService = TelemetryService(mainActivity)
    // State
    private var isCameraControllerConfigured = false


    // Private


    // Setup camera controller
    private fun waitAndSetupCameraController(setupCallback: (status: Boolean) -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(
            { setupCameraController(setupCallback) }, 250)
    }
    private fun setupCameraController(setupCallback: (status: Boolean) -> Unit) {
        // Create camera controller and setup cameras
        cameraController = CameraController(mainActivity)
        cameraController!!.setupCameraController (dataController) { newCameraCharacteristicsList ->
            cameraConfigurations = newCameraCharacteristicsList
            setupCallback(true)
        }
    }


    // Open camera
    private fun openCameraDomain() {
        // Configure camera controller
        if (!isCameraControllerConfigured) {
            // At first time
            waitAndSetupCameraController {
                // Create camera domain
                camera = Camera(
                    mainActivity,
                    modelData,
                    uiEventHandler,
                    cameraController!!,
                    cameraConfigurations,
                    dataController,
                ) { domainName -> openDomain(domainName) }
                // Send signal to start capture session
                camera!!.clearStart()
                // Set state
                isCameraControllerConfigured = true
                // Open Ui pages
                modelData.openCamera()
            }
        } else {
            // Send signal to start capture session
            camera!!.resume()
            // Open Ui pages
            modelData.openCamera()
        }
    }


    // Open application part
    private fun openDomain(domainName: String) {

        // Close all pages
        modelData.closeAllPages()

        // Open page
        if (domainName == "Startup") {
            // Send signal
            startup.open()
            // Open Ui pages
            modelData.openStartupScreen()
        }
        if (domainName == "Camera") {
            openCameraDomain()
        }
        if (domainName == "Gallery") {
            // Send signal
            gallery.open()
            // Open Ui pages
            modelData.openGalley()
        }
    }


    // Default launch
    private fun runApplicationInMainState() {

        // Log
        Log.i(LOG_TAG, "Run app in main mode")

        // Delete broken files after crash
        dataController.deleteAllBrokenCollections()

        // Setup Monetization service
        monetizationService.setupService()

        // Start background process
        contentPostProcessor.start()

        // Send telemetry
        telemetryService.update()

        // Open camera
        openDomain("Camera")
    }


    // First launch
    private fun checkState() {

        // Log
        Log.i(LOG_TAG, "Check state")

        // Check is setup completed
        if((dataSaver.loadIntByKey("startupScreenCompletedVersion") ==
                    AppSettings().getStartupScreenVersion()) &&
            !AppSettings().getIsAlwaysShowStartupScreen()) {

            // Run in main state
            runApplicationInMainState()

        } else {
            // Open startup domain
            openDomain("Startup")
        }
    }


    private fun checkAccess() {
        // Check user access
        appUsageAccessControl.isUserHaveAccessToUseApplication { isHaveAccess, whyDoNotHaveAccess ->

            // When success
            if (isHaveAccess) {
                // Continue setup
                checkState()
                return@isUserHaveAccessToUseApplication
            }

            // When not success
            if (whyDoNotHaveAccess == "NeedAnUpdate") {
                // Log
                Log.w(LOG_TAG, "WARNING update required")
                // Open access denied screen
                modelData.openUpdateRequiredPage()
            }
        }
    }


    // Request permissions, use permissionController
    private fun requestPermissions() {
        permissionController.requestPermissions { result ->
            // Process permissions request result
            if (result) {
                checkAccess()
            } else {
                // Log
                Log.w(LOG_TAG, "WARNING access denied")
                // Open access denied screen
                modelData.openAccessDeniedPage()
            }
        }
    }


    // Public


    fun start() {
        Log.i(LOG_TAG, "Start")

        // Check access
        requestPermissions()
    }


    // Provide callbacks
    fun onNewActivityState(newState: String) {
        camera?.onNewActivityState(newState)
    }
    fun onNewSystemEvent(newEvent: String) {
        camera?.onNewSystemEvent(newEvent)
        gallery.onNewSystemEvent(newEvent)
    }
    fun requestPermissionsResultCallback(requestCode: Int, permissions: Array<String>,
                                         grantResults: IntArray) {
        // Process in permissionController
        permissionController.requestPermissionsResultCallback(requestCode,
            permissions, grantResults)
    }

}