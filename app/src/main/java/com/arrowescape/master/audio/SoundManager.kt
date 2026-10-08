package com.arrowescape.master.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

enum class SoundEffect {
    TAP,
    SLIDE_LAUNCH,
    SHIELD_CRACK,
    ROTATE,
    BLOCKED_SHAKE,
    WRONG_PRESS,
    VICTORY_CHIME,
    UNDO
}

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<SoundEffect, Int>()
    var isMuted: Boolean = false

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build()

            generateAndLoadSounds()
        } catch (e: Exception) {
            Log.e("SoundManager", "Error initializing SoundPool: ${e.message}")
        }
    }

    private fun generateAndLoadSounds() {
        val soundDir = File(context.cacheDir, "sounds").apply { if (!exists()) mkdirs() }

        soundMap[SoundEffect.TAP] = loadWavSound(soundDir, "tap.wav", SoundEffect.TAP)
        soundMap[SoundEffect.SLIDE_LAUNCH] = loadWavSound(soundDir, "slide.wav", SoundEffect.SLIDE_LAUNCH)
        soundMap[SoundEffect.SHIELD_CRACK] = loadWavSound(soundDir, "crack.wav", SoundEffect.SHIELD_CRACK)
        soundMap[SoundEffect.ROTATE] = loadWavSound(soundDir, "rotate.wav", SoundEffect.ROTATE)
        soundMap[SoundEffect.BLOCKED_SHAKE] = loadWavSound(soundDir, "shake.wav", SoundEffect.BLOCKED_SHAKE)
        soundMap[SoundEffect.WRONG_PRESS] = loadWavSound(soundDir, "wrong.wav", SoundEffect.WRONG_PRESS)
        soundMap[SoundEffect.VICTORY_CHIME] = loadWavSound(soundDir, "victory.wav", SoundEffect.VICTORY_CHIME)
        soundMap[SoundEffect.UNDO] = loadWavSound(soundDir, "undo.wav", SoundEffect.UNDO)
    }

    private fun loadWavSound(dir: File, filename: String, effect: SoundEffect): Int {
        val file = File(dir, filename)
        if (!file.exists()) {
            val pcmData = generatePcmForEffect(effect)
            val wavData = createWavHeader(pcmData.size, 22050, 1) + pcmData
            FileOutputStream(file).use { it.write(wavData) }
        }
        return soundPool?.load(file.absolutePath, 1) ?: 0
    }

    fun play(effect: SoundEffect) {
        if (isMuted) return
        val soundId = soundMap[effect] ?: return
        if (soundId != 0) {
            // Boost volume to maximum (1.0f, 1.0f)
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    private fun generatePcmForEffect(effect: SoundEffect): ByteArray {
        val sampleRate = 22050
        val durationMs = when (effect) {
            SoundEffect.TAP -> 50
            SoundEffect.SLIDE_LAUNCH -> 250
            SoundEffect.SHIELD_CRACK -> 150
            SoundEffect.ROTATE -> 100
            SoundEffect.BLOCKED_SHAKE -> 140
            SoundEffect.WRONG_PRESS -> 160
            SoundEffect.VICTORY_CHIME -> 600
            SoundEffect.UNDO -> 100
        }

        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val envelope = (1.0 - progress)

            val sampleVal = when (effect) {
                SoundEffect.TAP -> {
                    sin(2.0 * Math.PI * 1200.0 * t) * envelope
                }
                SoundEffect.SLIDE_LAUNCH -> {
                    val freq = 300.0 + (1200.0 * progress)
                    sin(2.0 * Math.PI * freq * t) * envelope
                }
                SoundEffect.SHIELD_CRACK -> {
                    val noise = (Math.random() * 2.0 - 1.0)
                    val tone = sin(2.0 * Math.PI * 800.0 * t)
                    (tone * 0.4 + noise * 0.6) * envelope
                }
                SoundEffect.ROTATE -> {
                    val freq = 600.0 + (200.0 * sin(progress * Math.PI))
                    sin(2.0 * Math.PI * freq * t) * envelope
                }
                SoundEffect.BLOCKED_SHAKE, SoundEffect.WRONG_PRESS -> {
                    val subFreq = if (progress < 0.5) 120.0 else 90.0
                    val buzz = sin(2.0 * Math.PI * subFreq * t) + sin(2.0 * Math.PI * (subFreq * 1.5) * t) * 0.5
                    buzz * envelope
                }
                SoundEffect.VICTORY_CHIME -> {
                    val noteIdx = (progress * 4).toInt().coerceIn(0, 3)
                    val freq = listOf(523.25, 659.25, 783.99, 1046.50)[noteIdx]
                    sin(2.0 * Math.PI * freq * t) * (1.0 - (progress * 0.6))
                }
                SoundEffect.UNDO -> {
                    val freq = 800.0 - (400.0 * progress)
                    sin(2.0 * Math.PI * freq * t) * envelope
                }
            }

            // Max amplitude amplification (1.0f multiplier)
            buffer[i] = (sampleVal * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
        }

        val bytes = ByteArray(numSamples * 2)
        val byteBuf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        for (s in buffer) {
            byteBuf.putShort(s)
        }
        return bytes
    }

    private fun createWavHeader(dataSize: Int, sampleRate: Int, channels: Int): ByteArray {
        val totalDataLen = dataSize + 36
        val byteRate = sampleRate * channels * 2
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()

        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0

        header[20] = 1
        header[21] = 0

        header[22] = channels.toByte()
        header[23] = 0

        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()

        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()

        header[32] = (channels * 2).toByte()
        header[33] = 0

        header[34] = 16
        header[35] = 0

        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        header[40] = (dataSize and 0xff).toByte()
        header[41] = ((dataSize shr 8) and 0xff).toByte()
        header[42] = ((dataSize shr 16) and 0xff).toByte()
        header[43] = ((dataSize shr 24) and 0xff).toByte()

        return header
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
