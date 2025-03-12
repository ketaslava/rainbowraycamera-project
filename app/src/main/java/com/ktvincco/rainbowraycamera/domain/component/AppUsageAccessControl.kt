package com.ktvincco.rainbowraycamera.domain.component

import com.ktvincco.rainbowraycamera.AppSettings

class AppUsageAccessControl {

    // Check application activation
    // Return true + "" or false + "" / "NeedAnUpdate" / "InstallationSourceError"
    fun isUserHaveAccessToUseApplication(
        callback: (isHaveAccess: Boolean, whyDoNotHaveAccess: String) -> Unit) {

        // Check end of build lifetime
        val endOfBuildLifetime =
            (AppSettings().getEndOfBuildLifetime().toString() + "000").toLong()
        if (endOfBuildLifetime < System.currentTimeMillis()) {
            callback(false, "NeedAnUpdate")
            return
        }

        // Success exit
        callback(true, "")
    }
}