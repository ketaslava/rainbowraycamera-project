package com.ktvincco.rainbowraycamera.data


import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter


class DataSaver (private val mainActivity: Activity) {


    // Settings


    companion object {
        const val LOG_TAG = "DataSaver"
    }


    // Variables


    private var currentCollectionName: String? = null
    private external fun rotateImageByPath(imagePath: String, rotationDegrees: Int): String
    private var savedDataCache = mutableMapOf<String, String>()


    // Process directories


    private fun getAppFilesDir(): File { return mainActivity.filesDir }
    private fun getCollectionsDir(): File {
        return File(getAppFilesDir(), "/collections") }
    private fun getCollectionDirectoryByName(collectionName: String): File {
        return File(getCollectionsDir(), "/$collectionName") }

    private fun getPublicStorageDir(): File {
        return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "RainbowRayCamera") }

    private fun getFileInPublicDirByTimeInName(timeInName: String): File {
        val directory = getPublicStorageDir()
        val fileName = "RRC_IMG_${timeInName}.jpg"
        return File(directory, fileName)
    }


    fun createDirInPrivateSpace(directory: String) {
        val filesDir = getAppFilesDir()
        val targetDir = File(filesDir, directory)
        if (!targetDir.exists()) { targetDir.mkdirs() }
    }


    // Base


    // Broadcast signal about new media for operating system
    private fun indexFileForOperatingSystem(context: Context, imagePath: String) {
        try {
            MediaScannerConnection.scanFile(context, arrayOf(imagePath), null) { _, _ -> }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "EXC in notifyGallery")
            e.printStackTrace()
        }
    }


    // Clear all in directory
    private fun clearDirectory(directory: File) {
        if (directory.exists()) { val files = directory.listFiles()
            if (files != null) { for (file in files) { if (file.isDirectory) {
                clearDirectory(file) } else { file.delete() } } } }
    }


    private fun getMediaFilesInDirectory(directory: File): Array<File> {
        if (directory.exists() && directory.isDirectory) {
            return directory.listFiles { file -> file.isFile } ?: emptyArray()
        }
        return emptyArray()
    }


    fun isFileInPublicStorage(filePath: String): Boolean {
        val publicDir = Environment.getExternalStorageDirectory()
        return filePath.startsWith(publicDir.absolutePath)
    }


    fun isFileInPublicOrPrivateStorage(filePath: String): Boolean {
        val file = File(filePath)
        val publicDir = Environment.getExternalStorageDirectory()
        val isInPublicStorage = file.absolutePath.startsWith(publicDir.absolutePath)
        val isInPrivateStorage = file.absolutePath.contains("/private_result_storage/")
        return isInPublicStorage || isInPrivateStorage
    }


    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return try { if (file.exists()) { file.delete(); true }
        else { false } } catch (e: Exception) { e.printStackTrace(); false }
    }


    fun saveTextToFile(directory: File, fileName: String, text: String) {
        val file = File(directory, fileName)
        if (!file.exists()) { file.createNewFile() }

        val fileOutputStream = FileOutputStream(file, false)
        val writer = BufferedWriter(OutputStreamWriter(fileOutputStream))

        writer.write(text)
        writer.close()
        fileOutputStream.close()
    }


    fun readTextFromFile(directory: File, fileName: String): String? {
        val file = File(directory, fileName)
        if (!file.exists()) { return null }

        val fileInputStream = FileInputStream(file)
        val reader = BufferedReader(InputStreamReader(fileInputStream))
        val stringBuilder = StringBuilder()

        var line: String? = reader.readLine()
        while (line != null) { stringBuilder.append(line); line = reader.readLine()
            if (line != null) { stringBuilder.append("\n") } }

        reader.close()
        fileInputStream.close()
        return stringBuilder.toString()
    }


    // Save and load data with cache


    private fun saveDataCached(key: String, data: String) {
        // If current data already saved -> exit
        if (savedDataCache.containsKey(key) && savedDataCache[key] == data) { return }
        // Save data
        val filesDir = File(getAppFilesDir(), "/savedData")
        saveTextToFile(filesDir, "/$key.txt", data)
        savedDataCache[key] = data
    }


    private fun loadDataCached(key: String): String? {
        // If can load data from cache -> load from cache
        if (savedDataCache.containsKey(key)) { return savedDataCache[key] }
        // Load data
        val filesDir = File(getAppFilesDir(), "/savedData")
        return readTextFromFile(filesDir, "/$key.txt")
    }


    // Public


    init {
        createDirInPrivateSpace("/savedData")
    }


    // Base


    fun saveStringByKey(key: String, data: String) {
        saveDataCached("string_$key", data)
    }
    fun loadStringByKey(key: String): String? {
        return loadDataCached("string_$key")
    }


    fun saveIntByKey(key: String, data: Int) {
        saveDataCached("int_$key", data.toString())
    }
    fun loadIntByKey(key: String): Int? {
        return loadDataCached("int_$key")?.toInt()
    }


    fun saveBooleanByKey(key: String, data: Boolean) {
        saveDataCached("boolean_$key", data.toString())
    }
    fun loadBooleanByKey(key: String): Boolean? {
        return loadDataCached("boolean_$key")?.toBoolean()
    }


    fun saveIntArrayByKey(key: String, data: ArrayList<Int>) {
        val dataString = data.joinToString(",")
        saveDataCached("arrayInt_$key", dataString)
    }
    fun loadIntArrayByKey(key: String): ArrayList<Int>? {
        val dataString = loadDataCached("arrayInt_$key") ?: return null
        val dataArray = dataString.split(",").map { it.toInt() }.toMutableList()
        return ArrayList(dataArray)
    }


    fun getProcessingDirectory(): File {
        val processingDirectory = File(mainActivity.filesDir, "/processing")

        // Create if not exists
        if (!processingDirectory.exists()) {
            processingDirectory.mkdirs()
        }

        return processingDirectory
    }


    fun saveByteArray(byteArray: ByteArray, directory: File, fileName: String) {

        // Check if the directory exists, and create it if it doesn't
        if (!directory.exists()) { directory.mkdirs() }

        // Create file as object
        val file = File(directory, fileName)

        // Open data stream
        val fileOutputStream = FileOutputStream(file)

        // Write data and close stream
        try {
            fileOutputStream.write(byteArray)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileOutputStream.close()
        }
    }


    // Capture content to collection


    // Save images as byte array from Camera controller to collection
    // Created collections will be post processed with background content processor
    // Collection named with current unix time
    // For create new photo collection: Start -> Write, Write ... Write, -> Save
    // For create new video collection: Start -> getPath -> *Save video file to path* -> Save


    fun createNewCollection(contentType: String, contentFormat: String, contentRotation: Float,
                            isMirrorRelativeToNormal: Boolean, isMirrorByVertical: Boolean) {

        // Create collection name
        currentCollectionName = System.currentTimeMillis().toString()

        // Create collection directory
        val collectionDirectory = getCollectionDirectoryByName(currentCollectionName!!)
        if (!collectionDirectory.exists()) {
            collectionDirectory.mkdirs()
        } else {
            // Clear if unix time switch error
            clearDirectory(collectionDirectory)
        }

        // Add is temporal collection file
        saveTextToFile(collectionDirectory, "isTemporalCollection.txt", "No")

        // Add content type file
        saveTextToFile(collectionDirectory, "contentType.txt", contentType)
        // Add format type file
        saveTextToFile(collectionDirectory, "contentFormat.txt", contentFormat)
        // Add content rotation file
        saveTextToFile(collectionDirectory,
            "contentRotation.txt", contentRotation.toString())
        // Add content mirror file
        saveTextToFile(collectionDirectory,
            "isMirrorRelativeToNormal.txt", isMirrorRelativeToNormal.toString())
        saveTextToFile(collectionDirectory,
            "isMirrorByVertical.txt", isMirrorByVertical.toString())
        // Add collection state file
        saveTextToFile(collectionDirectory, "collectionState.txt", "Creation")
        // Add collection state file
        saveTextToFile(collectionDirectory, "numberOfFiles.txt", "0")

        // Log
        Log.i(LOG_TAG, "Collection created by path: " +
                collectionDirectory.absolutePath.toString())
    }


    fun createNewTemporalCollection() {

        // Create collection name
        currentCollectionName = System.currentTimeMillis().toString()

        // Create collection directory
        val collectionDirectory = getCollectionDirectoryByName(currentCollectionName!!)
        if (!collectionDirectory.exists()) {
            collectionDirectory.mkdirs()
        } else {
            // Clear if unix time switch error
            clearDirectory(collectionDirectory)
        }

        // Add is temporal collection file
        saveTextToFile(collectionDirectory, "isTemporalCollection.txt", "Yes")

        // Log
        Log.i(LOG_TAG, "Collection created by path: " +
                collectionDirectory.absolutePath.toString())
    }


    fun writeByteArrayToCurrentCollection(byteArray: ByteArray) {

        // Null EXC
        if (currentCollectionName == null) { return; }

        // Get directory
        val collectionDirectory = getCollectionDirectoryByName(currentCollectionName!!)

        // Get number of files
        val numOfFiles = readTextFromFile(collectionDirectory, "numberOfFiles.txt")
        // Get content format
        val contentFormat = readTextFromFile(collectionDirectory, "contentFormat.txt")

        // Write file
        saveByteArray(byteArray, collectionDirectory, "$numOfFiles.$contentFormat")

        // + 1 file
        val newFileNum = (numOfFiles!!.toInt() + 1).toString()
        // Update number of files
        saveTextToFile(collectionDirectory, "numberOfFiles.txt", newFileNum)
    }


    fun getCurrentCollectionPathForVideoFile(): String {

        // Get directory
        val collectionDirectory = getCollectionDirectoryByName(currentCollectionName!!)

        // Update number of files
        saveTextToFile(collectionDirectory, "numberOfFiles.txt", "1")

        return File(collectionDirectory, "recordedVideo.mp4").absolutePath
    }


    fun getImagesCountForCurrentCollection(): Int {
        // Null EXC
        if (currentCollectionName == null) { return 0; }
        // Get directory
        val collectionDirectory = getCollectionDirectoryByName(currentCollectionName!!)
        // Update collection state file to "Created" state
        return readTextFromFile(collectionDirectory, "numberOfFiles.txt")!!.toInt()
    }


    fun saveCurrentCollection() {

        // Null EXC
        if (currentCollectionName == null) { return }

        // Get directory
        val collectionDirectory = getCollectionDirectoryByName(currentCollectionName!!)

        // Update collection state file to "Created" state
        saveTextToFile(collectionDirectory, "collectionState.txt", "Created")

        // Close current collection
        currentCollectionName = null
    }


    fun deleteAllBrokenCollections() {

        // Get all collections
        val directory = File(mainActivity.filesDir, "/collections")
        var collections: List<String> = emptyList();

        if (directory.exists() && directory.isDirectory) {
            collections = directory.listFiles {
                    file -> file.isDirectory }?.map { it.name } ?: emptyList()
        }

        // Remove unavailable no fully created collections

        for (collection in collections) {

            // Delete temporal collection
            val isTemporalCollection = readTextFromFile(
                getCollectionDirectoryByName(collection), "isTemporalCollection.txt")
            if (isTemporalCollection == "Yes") {
                deleteCollection(collection)
                continue
            }

            // Delete by state
            val collectionState = readTextFromFile(
                getCollectionDirectoryByName(collection), "collectionState.txt")
            if (collectionState != "Created") {
                deleteCollection(collection)
                continue
            }

            // Delete by number of files
            val numberOfFiles = readTextFromFile(
                getCollectionDirectoryByName(collection), "numberOfFiles.txt")
            if (numberOfFiles != null && numberOfFiles.toInt() < 2) {
                deleteCollection(collection)
                continue
            }
        }
    }


    // Post process collection


    fun getNextCollectionNameAvailableToProcess(): String? {

        // Get all collections
        val directory = File(mainActivity.filesDir, "/collections")
        var collections: List<String> = emptyList();

        if (directory.exists() && directory.isDirectory) {
            collections = directory.listFiles {
                    file -> file.isDirectory }?.map { it.name } ?: emptyList()
        }

        // Remove unavailable to process collections

        var collectionsToProcess: List<String> = emptyList()

        for (collection in collections) {

            val collectionState = readTextFromFile(
                getCollectionDirectoryByName(collection), "collectionState.txt")

            if (collectionState == "Created") {
                collectionsToProcess = collectionsToProcess.plus(collection)
            }
        }

        // Sort by time (select oldest)

        if (collectionsToProcess.isEmpty()) { return null; }

        var oldestCollection = collectionsToProcess[0].toLong();
        for (collection in collections) {
            if (collection.toLong() < oldestCollection) {
                oldestCollection = collection.toLong()
            }
        }

        return oldestCollection.toString();
    }


    fun getCollectionContentTypeByName(collectionName: String): String {
        // Get collection path
        val collectionPath = getCollectionDirectoryByName(collectionName)
        // Read content type file
        return readTextFromFile(collectionPath, "contentType.txt")!!
    }


    fun copyCollectionToProcessingDirectory(collectionName: String) {

        // Get directory
        val processingDirectory = getProcessingDirectory()
        val collectionDirectory = getCollectionDirectoryByName(collectionName)

        // Clear processing directory
        clearDirectory(processingDirectory)

        collectionDirectory.listFiles()?.forEach { file ->
            val destinationFile = File(processingDirectory, file.name)
            file.copyTo(destinationFile)
        }
    }


    fun useIndex0ImageAsResult() {
        val processingDirectory = getProcessingDirectory()
        val contentFormat = readTextFromFile(processingDirectory, "contentFormat.txt")
        val srcFileName = "0.$contentFormat"
        val dstFileName = "result.$contentFormat"
        val srcFile = File(processingDirectory, srcFileName)
        val dstFile = File(processingDirectory, dstFileName)
        srcFile.copyTo(dstFile, true)
    }


    fun normalizeInSpaceResultInProcessingDirectory() {

        // Get variables
        val processingDirectory = getProcessingDirectory()
        val contentFormat = readTextFromFile(processingDirectory, "contentFormat.txt")
        val srcFileName = "result.$contentFormat"
        val srcFile = File(processingDirectory, srcFileName)

        val isMirrorRelativeToNormal = readTextFromFile(
            processingDirectory, "isMirrorRelativeToNormal.txt")!!.toBoolean()
        val isMirrorByVertical = readTextFromFile(
            processingDirectory, "isMirrorByVertical.txt")!!.toBoolean()
        val rotation = readTextFromFile(
            processingDirectory, "contentRotation.txt")!!.toFloat()

        if (contentFormat == "jpg") {
            val bitmap = BitmapFactory.decodeFile(srcFile.absolutePath)

            val matrix = Matrix()
            matrix.postRotate(rotation)
            if (isMirrorRelativeToNormal) {
                if (isMirrorByVertical) {
                    matrix.preScale(1.0f, -1.0f)
                } else {
                    matrix.preScale(-1.0f, 1.0f)
                }
            }

            val normalizedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width,
                bitmap.height, matrix, true
            )

            val outputStream = FileOutputStream(srcFile)
            normalizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        }
    }


    fun copyResultFromPhotoCollectionToPublicStorage(collectionName: String) {

        val processingDirectory = getProcessingDirectory()
        val srcFileName = "result.jpg"

        val publicDirectory = File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM), "RainbowRayCamera")
        val dstFileName = "RRC_IMG_$collectionName.jpg"

        if (!publicDirectory.exists()) {
            publicDirectory.mkdirs()
        }

        val srcFile = File(processingDirectory, srcFileName)
        val dstFile = File(publicDirectory, dstFileName)

        srcFile.copyTo(dstFile, true)

        // Notify gallery about new media
        indexFileForOperatingSystem(mainActivity, dstFile.absolutePath)
    }


    fun copyResultFromPhotoCollectionToPrivateResultStorage(collectionName: String) {

        val processingDirectory = getProcessingDirectory()
        val contentFormat = readTextFromFile(processingDirectory, "contentFormat.txt")
        val srcFileName = "result.$contentFormat"

        val targetDirectory = File(mainActivity.filesDir, "/private_result_storage")
        val dstFileName = "RRC_IMG_$collectionName.$contentFormat"

        if (!targetDirectory.exists()) { targetDirectory.mkdirs() }

        val srcFile = File(processingDirectory, srcFileName)
        val dstFile = File(targetDirectory, dstFileName)

        srcFile.copyTo(dstFile, true)
    }


    fun copyResultFromVideoCollectionToPublicStorage(collectionName: String) {

        val processingDirectory = getProcessingDirectory()
        val srcFileName = "recordedVideo.mp4"

        val publicDirectory = File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM), "RainbowRayCamera")
        val dstFileName = "RRC_VID_$collectionName.mp4"

        if (!publicDirectory.exists()) {
            publicDirectory.mkdirs()
        }

        val srcFile = File(processingDirectory, srcFileName)
        val dstFile = File(publicDirectory, dstFileName)

        if (srcFile.exists()) {
            srcFile.copyTo(dstFile)
        }

        // Notify gallery about new media
        indexFileForOperatingSystem(mainActivity, dstFile.absolutePath)
    }


    fun copyResultFromVideoCollectionToPrivateResultStorage(collectionName: String) {

        val processingDirectory = getProcessingDirectory()
        val srcFileName = "recordedVideo.mp4"

        val targetDirectory = File(mainActivity.filesDir, "/private_result_storage")
        val dstFileName = "RRC_VID_$collectionName.mp4"

        if (!targetDirectory.exists()) { targetDirectory.mkdirs() }

        val srcFile = File(processingDirectory, srcFileName)
        val dstFile = File(targetDirectory, dstFileName)

        if (srcFile.exists()) { srcFile.copyTo(dstFile) }
    }


    fun copyFileFromPrivateToPublicStorage(filePath: String) {

        val srcFile = File(filePath)

        val dstDirectory = File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM), "RainbowRayCamera")
        val dstFile = File(dstDirectory, srcFile.name)

        if (!dstDirectory.exists()) { dstDirectory.mkdirs() }

        srcFile.copyTo(dstFile, true)
        srcFile.delete()

        // Notify gallery about new media
        indexFileForOperatingSystem(mainActivity, dstFile.absolutePath)
    }


    fun forceStopCollectionProcess(collectionName: String) {
        // Get directory
        val collectionDirectory = getCollectionDirectoryByName(collectionName)
        // Write force stop file
        saveTextToFile(collectionDirectory, "isForceStopNow.txt", "1")
    }


    fun deleteCollection(collectionName: String) {
        // Get directory
        val collectionDirectory = getCollectionDirectoryByName(collectionName)
        // Write force stop file
        collectionDirectory.deleteRecursively()
    }


    // Gallery


    fun getAllMediaPathsInPublicAndPrivateStorage(): List<String> {

        var mediaPaths: List<String> = emptyList()
        val publicDirectory = getPublicStorageDir()
        val privateDirectory = File(mainActivity.filesDir, "/private_result_storage")

        var mediaFiles = getMediaFilesInDirectory(publicDirectory)
        mediaFiles = mediaFiles.plus(getMediaFilesInDirectory(privateDirectory))

        if (mediaFiles.isNotEmpty()) {
            // Sort files by name
            val sortedMediaFiles = mediaFiles.sortedBy { file ->
                // Extracting numbers from file names and converting them to Long
                val numberInFileName = "\\d+".toRegex().find(file.name)?.value?.toLongOrNull()
                numberInFileName ?:
                // If no number found, place it at the end
                Long.MAX_VALUE
            }
            mediaPaths = sortedMediaFiles.map { it.absolutePath }
        }

        return mediaPaths
    }


    fun getAllBaseImagePathsInCollections(): List<String> {
        val directory = getCollectionsDir()
        var imagePaths: List<String> = emptyList()
        if (directory.exists() && directory.isDirectory) {
            imagePaths = directory.listFiles {
                    file -> file.isDirectory }?.map { it.absolutePath + "/0.jpg" } ?: emptyList()
        }
        return imagePaths
    }


    fun readImageAsBitmapByPath(imageAbsolutePath: String): Bitmap? {
        try {
            // Get image file
            val imageFile = File(imageAbsolutePath)

            // open stream
            val fileInputStream = FileInputStream(imageFile)

            // read bytes
            val byteArray = ByteArray(imageFile.length().toInt())
            fileInputStream.read(byteArray)
            fileInputStream.close()

            // bytes to bitmap
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    fun rotateImage(filePath: String, degrees: Int) {
        rotateImageByPath(filePath, degrees)
    }
}