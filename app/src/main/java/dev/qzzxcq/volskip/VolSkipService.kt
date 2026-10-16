package dev.qzzxcq.volskip

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * Hold Vol+ -> next track, hold Vol- -> previous track.
 * Short press -> normal volume change.
 * Only active while music is playing.
 */
class VolSkipService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var audio: AudioManager

    private var pressedKey = 0
    private var longFired = false
    private var intercepting = false

    private val longPressRunnable = Runnable {
        longFired = true
        val media = if (pressedKey == KeyEvent.KEYCODE_VOLUME_UP)
            KeyEvent.KEYCODE_MEDIA_NEXT else KeyEvent.KEYCODE_MEDIA_PREVIOUS
        sendMediaKey(media)
        vibrate()
    }

    override fun onServiceConnected() {
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val code = event.keyCode
        if (code != KeyEvent.KEYCODE_VOLUME_UP && code != KeyEvent.KEYCODE_VOLUME_DOWN) return false

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount > 0) return intercepting
                // Leave ringer/notification volume alone when nothing is playing
                if (!audio.isMusicActive) {
                    intercepting = false
                    return false
                }
                intercepting = true
                pressedKey = code
                longFired = false
                handler.postDelayed(longPressRunnable, Prefs.longPressMs(this))
                return true
            }
            KeyEvent.ACTION_UP -> {
                if (!intercepting || code != pressedKey) return false
                handler.removeCallbacks(longPressRunnable)
                if (!longFired) {
                    val dir = if (code == KeyEvent.KEYCODE_VOLUME_UP)
                        AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
                    audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, dir, AudioManager.FLAG_SHOW_UI)
                }
                intercepting = false
                pressedKey = 0
                return true
            }
        }
        return false
    }

    private fun sendMediaKey(code: Int) {
        val t = SystemClock.uptimeMillis()
        audio.dispatchMediaKeyEvent(KeyEvent(t, t, KeyEvent.ACTION_DOWN, code, 0))
        audio.dispatchMediaKeyEvent(KeyEvent(t, t, KeyEvent.ACTION_UP, code, 0))
    }

    private fun vibrate() {
        if (!Prefs.vibrate(this)) return
        val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vm.defaultVibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {
        handler.removeCallbacks(longPressRunnable)
    }
}
