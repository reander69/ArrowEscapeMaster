package com.arrowescape.master.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class HapticType {
    LIGHT_TAP,
    LAUNCH_WHOOSH,
    ROTATION_CLICK,
    SHIELD_CRACK,
    BLOCKED_SHAKE,
    WRONG_PRESS,
    VICTORY_PULSE
}

class HapticManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun performHaptic(type: HapticType, enabled: Boolean = true) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.LIGHT_TAP -> VibrationEffect.createOneShot(14, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.LAUNCH_WHOOSH -> VibrationEffect.createOneShot(32, 220)
                    HapticType.ROTATION_CLICK -> VibrationEffect.createOneShot(24, 250)
                    HapticType.SHIELD_CRACK -> VibrationEffect.createWaveform(
                        longArrayOf(0, 30, 30, 40),
                        intArrayOf(0, 255, 0, 255),
                        -1
                    )
                    HapticType.BLOCKED_SHAKE, HapticType.WRONG_PRESS -> VibrationEffect.createWaveform(
                        // Powerful triple heavy rumble waveform for wrong attempts
                        longArrayOf(0, 80, 40, 80, 40, 110),
                        intArrayOf(0, 255, 0, 255, 0, 255),
                        -1
                    )
                    HapticType.VICTORY_PULSE -> VibrationEffect.createWaveform(
                        longArrayOf(0, 50, 60, 70, 60, 120),
                        intArrayOf(0, 180, 0, 220, 0, 255),
                        -1
                    )
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val duration = when (type) {
                    HapticType.LIGHT_TAP -> 14L
                    HapticType.LAUNCH_WHOOSH -> 32L
                    HapticType.ROTATION_CLICK -> 24L
                    HapticType.SHIELD_CRACK -> 70L
                    HapticType.BLOCKED_SHAKE, HapticType.WRONG_PRESS -> 200L
                    HapticType.VICTORY_PULSE -> 250L
                }
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            // Ignore if device fails or lacks permission
        }
    }
}
