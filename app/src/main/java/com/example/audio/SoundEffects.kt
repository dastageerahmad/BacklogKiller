package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

object SoundEffects {
    private const val SAMPLE_RATE = 22050
    private var ambientTrack: AudioTrack? = null
    private var ambientJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playCompletionPop(style: String, pitchMultiplier: Float, volume: Float) {
        scope.launch {
            try {
                val clampedVol = volume.coerceIn(0f, 1f)
                val pitch = pitchMultiplier.coerceIn(0.5f, 2.0f)
                val samples = when (style) {
                    "Retro" -> generateRetroBlip(pitch)
                    "Soft" -> generateSoftChime(pitch)
                    "Bell" -> generateBellChime(pitch)
                    else -> generateClassicChime(pitch)
                }

                // Apply volume
                val pcm = ShortArray(samples.size) { i ->
                    (samples[i] * clampedVol * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val bufferSize = pcm.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                Thread.sleep((pcm.size * 1000L / SAMPLE_RATE) + 100)
                track.release()
            } catch (_: Exception) {}
        }
    }

    private fun generateClassicChime(pitch: Float): FloatArray {
        // C5, E5, G5 upbeat arpeggio
        val notes = doubleArrayOf(523.25 * pitch, 659.25 * pitch, 783.99 * pitch)
        val noteDuration = 0.08
        val totalSamples = (SAMPLE_RATE * noteDuration * notes.size).toInt()
        val buffer = FloatArray(totalSamples)

        var idx = 0
        for (f in notes) {
            val count = (SAMPLE_RATE * noteDuration).toInt()
            for (i in 0 until count) {
                if (idx >= totalSamples) break
                val t = i.toDouble() / SAMPLE_RATE
                val env = (1.0 - (i.toDouble() / count)) // linear decay
                val wave = sin(2.0 * Math.PI * f * t)
                buffer[idx++] = (wave * env * 0.8).toFloat()
            }
        }
        return buffer
    }

    private fun generateSoftChime(pitch: Float): FloatArray {
        val freq = 587.33 * pitch // D5
        val duration = 0.28
        val total = (SAMPLE_RATE * duration).toInt()
        val buffer = FloatArray(total)
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = Math.exp(-t * 9.0)
            val wave = sin(2.0 * Math.PI * freq * t)
            buffer[i] = (wave * env * 0.7).toFloat()
        }
        return buffer
    }

    private fun generateRetroBlip(pitch: Float): FloatArray {
        val freq1 = 440.0 * pitch
        val freq2 = 880.0 * pitch
        val duration = 0.18
        val total = (SAMPLE_RATE * duration).toInt()
        val buffer = FloatArray(total)
        val half = total / 2
        for (i in 0 until total) {
            val freq = if (i < half) freq1 else freq2
            val t = i.toDouble() / SAMPLE_RATE
            val wave = if (sin(2.0 * Math.PI * freq * t) >= 0.0) 0.6f else -0.6f
            val env = 1.0 - (i.toDouble() / total)
            buffer[i] = (wave * env).toFloat()
        }
        return buffer
    }

    private fun generateBellChime(pitch: Float): FloatArray {
        val f0 = 659.25 * pitch // E5
        val duration = 0.45
        val total = (SAMPLE_RATE * duration).toInt()
        val buffer = FloatArray(total)
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = Math.exp(-t * 6.0)
            // Fundamental + 2nd + 3rd harmonic
            val wave = sin(2.0 * Math.PI * f0 * t) * 0.6 +
                    sin(2.0 * Math.PI * f0 * 2.76 * t) * 0.3 +
                    sin(2.0 * Math.PI * f0 * 4.07 * t) * 0.1
            buffer[i] = (wave * env).toFloat()
        }
        return buffer
    }

    fun startAmbient(type: String, volume: Float = 0.5f) {
        stopAmbient()
        if (type == "none" || type.isBlank()) return

        ambientJob = scope.launch {
            try {
                val chunkSize = 4096
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(chunkSize * 2)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                ambientTrack = track
                track.play()

                val random = Random()
                val shortBuffer = ShortArray(chunkSize)
                var lastBrown = 0.0

                while (isActive) {
                    val vol = volume.coerceIn(0.1f, 1.0f) * 0.35f
                    for (i in 0 until chunkSize) {
                        val sample: Double = when (type) {
                            "white" -> (random.nextDouble() * 2.0 - 1.0) * vol
                            "brown" -> {
                                val white = random.nextDouble() * 2.0 - 1.0
                                lastBrown = (lastBrown + (0.02 * white)) / 1.02
                                lastBrown.coerceIn(-1.0, 1.0) * vol * 3.5
                            }
                            "rain" -> {
                                val white = random.nextDouble() * 2.0 - 1.0
                                lastBrown = (lastBrown + (0.05 * white)) / 1.05
                                val drop = if (random.nextInt(250) == 0) random.nextDouble() * 0.4 else 0.0
                                ((lastBrown * 2.0) + drop).coerceIn(-1.0, 1.0) * vol * 2.0
                            }
                            else -> 0.0
                        }
                        shortBuffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    track.write(shortBuffer, 0, chunkSize)
                }
            } catch (_: Exception) {}
        }
    }

    fun stopAmbient() {
        ambientJob?.cancel()
        ambientJob = null
        try {
            ambientTrack?.stop()
            ambientTrack?.release()
        } catch (_: Exception) {}
        ambientTrack = null
    }
}
