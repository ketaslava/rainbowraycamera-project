package com.ktvincco.rainbowraycamera


import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import com.ktvincco.rainbowraycamera.domain.Main
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler
import com.ktvincco.rainbowraycamera.ui.UserInterface


class MainActivity : ComponentActivity() {


    // Variables


    // Layer base components
    private var userInterface: UserInterface? = null
    private var modelData: ModelData? = null
    private var uiEventHandler: UiEventHandler? = null
    private var main: Main? = null

    // Start when reach correct configuration
    private var isApplicationInPortraitMode = false
    private var isAppStarted = false

    // Wake lock (disable auto sleep)
    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null


    // Enter point


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create binders components
        modelData = ModelData()
        uiEventHandler = UiEventHandler(modelData!!)

        // Create base components
        userInterface = UserInterface(this, modelData!!, uiEventHandler!!)
        main = Main(this, modelData!!, uiEventHandler!!)

        // Setup interface
        setContent{  // Setup user interface
            userInterface!!.SetupUserInterface()
        }

        // Setup only if UI is vertical
        val orientation = resources.configuration.orientation
        isApplicationInPortraitMode = orientation == Configuration.ORIENTATION_PORTRAIT

        // Wake lock
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLockFlags = PowerManager.SCREEN_DIM_WAKE_LOCK
        wakeLock = powerManager!!.newWakeLock(wakeLockFlags, "App:WakeLockTag")
        wakeLock?.acquire(128*60*1000L /*10 minutes*/)

        // Enable edge-to-edge UI layout
        enableEdgeToEdge()
        setDecorFitsSystemWindows(window, false)

        // Immersive mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+
            window.insetsController?.let { controller ->
                // Hide status bar and navigation
                controller.hide(WindowInsets.Type.systemBars())
                // Set return by swipe behaviour
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Immersive mode for old android
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

        // Process system "back" event
        onBackPressedDispatcher.addCallback(this /* lifecycle owner */) {
            main?.onNewSystemEvent("onGoBackEvent")
        }
    }


    // Private


    private fun onNewActivityState(newState: String) {

        // Check start configuration for start app
        if (newState == "onResume" && isApplicationInPortraitMode && !isAppStarted) {
            isAppStarted = true; main!!.start() }

        // Wake lock
        if (newState == "onResume") { wakeLock?.acquire(128*60*1000L /*10 minutes*/) }
        if (newState == "onPause") { wakeLock?.release() }

        // Provide new state
        main?.onNewActivityState(newState)
        userInterface?.onNewActivityState(newState)
    }


    // System callbacks


    // Provide activity state callbacks
    override fun onResume() { super.onResume(); onNewActivityState("onResume") }
    override fun onPause() { super.onPause(); onNewActivityState("onPause") }


    // Permissions request callback
    @Deprecated("Deprecated in Java (Today don't have a Kotlin solve)")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        @Suppress("DEPRECATION")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Process in domainController
        main?.requestPermissionsResultCallback(requestCode, permissions, grantResults)
    }


    // Buttons event handler
    /*override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                main?.onNewSystemEvent("onKeyVolumeDownPressed"); true }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                main?.onNewSystemEvent("onKeyVolumeUpPressed"); true }
            else -> super.onKeyDown(keyCode, event)
        }
    }*/
}