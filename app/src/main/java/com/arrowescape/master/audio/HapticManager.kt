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
                    HapticType.LIGHT_TAP -> VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.LAUNCH_WHOOSH -> VibrationEffect.createOneShot(28, 180)
                    HapticType.ROTATION_CLICK -> VibrationEffect.createOneShot(20, 220)
                    HapticType.SHIELD_CRACK -> VibrationEffect.createWaveform(
                        longArrayOf(0, 25, 30, 35),
                        intArrayOf(0, 255, 0, 180),
                        -1
                    )
                    HapticType.BLOCKED_SHAKE, HapticType.WRONG_PRESS -> VibrationEffect.createWaveform(
                        longArrayOf(0, 45, 30, 45),
                        intArrayOf(0, 255, 0, 220),
                        -1
                    )
                    HapticType.VICTORY_PULSE -> VibrationEffect.createWaveform(
                        longArrayOf(0, 40, 50, 60, 50, 90),
                        intArrayOf(0, 150, 0, 200, 0, 255),
                        -1
                    )
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val duration = when (type) {
                    HapticType.LIGHT_TAP -> 12L
                    HapticType.LAUNCH_WHOOSH -> 28L
                    HapticType.ROTATION_CLICK -> 20L
                    HapticType.SHIELD_CRACK -> 60L
                    HapticType.BLOCKED_SHAKE, HapticType.WRONG_PRESS -> 90L
                    HapticType.VICTORY_PULSE -> 200L
                }
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            // Ignore if device fails or permission missing
        }
    }
}
