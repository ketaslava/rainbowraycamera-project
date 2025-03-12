package com.ktvincco.rainbowraycamera.domain.component

import android.app.Activity
import android.util.Log
import com.ktvincco.rainbowraycamera.data.DataSaver


class TelemetryService (
    private var mainActivity: Activity
) {


    // Settings


    companion object {
        const val LOG_TAG = "TelemetryService"
    }


    // Variables


    private val dataSaver = DataSaver(mainActivity)


    // Private


    /*private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }


    private fun apiPostRequest() {

        // Check is connection available
        if (!isInternetAvailable()) {
            Log.i(LOG_TAG, "Network disabled -> exit")
            return
        }

        val url = "https://ktvincco.com/rainbowraycamera/telemetry/new_user_report/api.php"

        // Combine request
        val postData = "param1=value1&param2=value2".toByteArray(Charsets.UTF_8)

        // Async request
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Make connection
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                conn.setRequestProperty("Content-Length", postData.size.toString())
                conn.doOutput = true

                // Send data
                val outputStream = conn.outputStream
                outputStream.write(postData)
                outputStream.flush()
                outputStream.close()

                // Get response
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = conn.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?
                    val response = StringBuilder()
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    println("Response: ${response.toString()}")
                } else {
                    Log.i(LOG_TAG, "EXC in response code: ${responseCode}")
                }
            } catch (e: Exception) {
                Log.i(LOG_TAG, "EXC in request: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    private fun sendNewUserReport() {
        apiPostRequest()
    }


    private fun sendDailyLaunchReport() {

    }*/


    // Public


    fun update() {

        // Log
        Log.i(LOG_TAG, "Update")
        Log.i(LOG_TAG, "TELEMETRY SERVICE DISABLED")

        /*dataSaver.loadBooleanByKey("isNoFirstCaptureSettingsLoad")
        dataSaver.saveBooleanByKey("isNoFirstCaptureSettingsLoad", true)*/
        //sendNewUserReport()
    }

}