package com.rakuishi.rrr.service

import android.content.Context
import android.speech.tts.TextToSpeech
import com.rakuishi.rrr.R
import java.util.Locale

class VoiceCoach(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var lastAnnouncedKm = 0

    fun start() {
        lastAnnouncedKm = 0
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val locale = if (Locale.getDefault().language == "ja") Locale.JAPANESE else Locale.ENGLISH
        val result = tts?.setLanguage(locale)
        isReady = result != TextToSpeech.LANG_MISSING_DATA
                && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun checkMilestone(totalDistanceM: Double, elapsedMs: Long) {
        if (!isReady) return
        val currentKm = (totalDistanceM / 1000).toInt()
        if (currentKm > lastAnnouncedKm) {
            announce(currentKm, elapsedMs)
            lastAnnouncedKm = currentKm
        }
    }

    private fun announce(km: Int, elapsedMs: Long) {
        val totalSeconds = elapsedMs / 1000
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()

        val kmText = context.resources.getQuantityString(R.plurals.voice_km, km, km)
        val timeText = buildString {
            if (minutes > 0) append(context.getString(R.string.voice_minutes, minutes))
            if (minutes > 0 && seconds > 0) append(" ")
            if (seconds > 0 || minutes == 0) append(context.getString(R.string.voice_seconds, seconds))
        }
        val speech = "$kmText. $timeText"

        tts?.speak(speech, TextToSpeech.QUEUE_FLUSH, null, "milestone_$km")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
