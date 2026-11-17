package com.nowlsantos.stockfishandme.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.nowlsantos.stockfishandme.R

class SoundManager(context: Context) {
    private val appContext = context.applicationContext
    private val activePlayers = mutableListOf<MediaPlayer>()

    private fun play(resId: Int, volume: Float = 5.0f) {
        try {
            val mediaPlayer = MediaPlayer.create(appContext, resId) ?: return
            mediaPlayer.setVolume(volume, volume)

            synchronized(activePlayers) {
                activePlayers.add(mediaPlayer)
            }

            mediaPlayer.setOnCompletionListener { mp ->
                synchronized(activePlayers) {
                    activePlayers.remove(mp)
                }
                mp.release()
            }

            mediaPlayer.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play sound resource: $resId", e)
        }
    }

    fun playMoveSound() = play(R.raw.move_self, 5.0f)
    fun playCaptureSound() = play(R.raw.capture, 5.0f)
    fun playCheckSound() = play(R.raw.check, 10.0f)

    companion object {
        private const val TAG = "SoundManager"
    }
}
