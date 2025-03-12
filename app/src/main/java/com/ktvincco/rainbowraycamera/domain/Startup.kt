package com.ktvincco.rainbowraycamera.domain


import android.app.Activity
import com.ktvincco.rainbowraycamera.AppSettings
import com.ktvincco.rainbowraycamera.data.DataSaver
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler


class Startup (
    private var mainActivity: Activity,
    private val modelData: ModelData,
    private val uiEventHandler: UiEventHandler,
    private val dataSaver: DataSaver,
    private val onStartupComplete: () -> Unit
) {


    // Settings


    private val lastPageIndex = 3


    // Variables


    private var currentPageIndex = 0
    private var checkboxStates: HashMap<String, Boolean> = HashMap()


    // Private


    private fun getCheckboxState(name: String): Boolean {
        return if (checkboxStates.containsKey(name)) {
            checkboxStates[name] ?: false
        } else { false }
    }
    private fun resetCheckboxState() { checkboxStates = HashMap() }


    private fun specialProcessCurrentPageForContinue(): Boolean {

        if (currentPageIndex == 1) {
            return getCheckboxState("OlderThan18")
        }

        if (currentPageIndex == 2) {
            if (!getCheckboxState("TermsOfUse")) { return false }
            return getCheckboxState("PrivacyPolicy")
        }

        return true
    }


    private fun updateButtons() {

        // Next and Back
        if (currentPageIndex < lastPageIndex && specialProcessCurrentPageForContinue()) {
            modelData.setIsNextButtonActive(true) } else { modelData.setIsNextButtonActive(false) }
        if (currentPageIndex > 0) { modelData.setIsBackButtonActive(true) }
        else { modelData.setIsBackButtonActive(false) }

        // Confirm
        if (currentPageIndex == lastPageIndex) { modelData.setIsConfirmButtonActive(true) }
        else { modelData.setIsConfirmButtonActive(false) }
    }


    private fun showPage() {
        modelData.setCurrentPageIndex(currentPageIndex)
    }


    private fun assignCallbacks() {

        // Next and Back button
        uiEventHandler.assignNextButtonCallback {
            // Check special requirements for continue
            if (!specialProcessCurrentPageForContinue()) { return@assignNextButtonCallback }
            // New index
            currentPageIndex += 1
            if (currentPageIndex > lastPageIndex) { currentPageIndex = lastPageIndex }
            // Update
            resetCheckboxState()
            updateButtons()
            showPage()
        }
        uiEventHandler.assignBackButtonCallback {
            // New index
            currentPageIndex -= 1
            if (currentPageIndex < 0) { currentPageIndex = 0 }
            // Update
            updateButtons()
            showPage()
        }

        // Confirm and close button callback
        uiEventHandler.assignConfirmButtonCallback {
            // Check is on last page
            if (currentPageIndex == lastPageIndex) { /* Close startup screen */ close() }
        }

        // Checkboxes
        uiEventHandler.assignCheckboxCallback { name, value ->
            checkboxStates[name] = value
            updateButtons()
        }
    }


    private fun close() {
        // Set is setup complete
        dataSaver.saveIntByKey("startupScreenCompletedVersion",
            AppSettings().getStartupScreenVersion())
        // Callback
        onStartupComplete()
    }


    // Public


    fun open() {
        currentPageIndex = 0
        assignCallbacks()
        updateButtons()
    }
}