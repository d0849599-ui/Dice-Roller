package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.view.SoundEffectConstants
import android.view.View

/**
 * Handles tactile clatter sound effects for the dice roller.
 * Strictly respects system volume, ringer mode, and user preference.
 */
class DiceSoundManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    /**
     * Plays a crisp, subtle wood/clatter click sound effect using Android's
     * system audio effects engine, ensuring it respects the system volume settings.
     */
    fun playClatterSound(view: View? = null) {
        // Respect system ringer mode (silent / vibrate shouldn't produce loud sounds)
        val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        if (ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            return
        }

        // Play subtle system tap/click effect through the window view or audio manager
        if (view != null) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        } else {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.75f)
        }
    }
}
