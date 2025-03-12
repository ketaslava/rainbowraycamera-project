package com.ktvincco.rainbowraycamera.data

import android.app.Activity
import android.media.MediaPlayer


class SoundPlayer (
    private val mainActivity: Activity
) {

    // Plays sound from application resources (R) by id
    fun playSound(soundResourceId: Int) {

        val mediaPlayer = MediaPlayer.create(mainActivity, soundResourceId)
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }
        mediaPlayer.seekTo(0)
        mediaPlayer.start()
    }

}