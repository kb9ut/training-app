package com.kb9ut.pror.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Binder
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kb9ut.pror.data.local.dataStore
import com.kb9ut.pror.data.local.entity.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Represents a single phase of the tempo cycle.
 */
enum class TempoPhase {
    ECCENTRIC,
    PAUSE_BOTTOM,
    CONCENTRIC,
    PAUSE_TOP
}

/**
 * State exposed to the UI for displaying metronome progress.
 */
data class MetronomeState(
    val isPlaying: Boolean = false,
    val currentPhase: TempoPhase = TempoPhase.ECCENTRIC,
    val phaseSecondsRemaining: Int = 0,
    val currentRep: Int = 0
)

/**
 * Bound service that plays audible metronome tick sounds to guide the user
 * through a tempo-controlled rep cadence (E-P-C-T).
 *
 * Uses [ToneGenerator] for sounds and requests transient audio focus so that
 * music apps duck rather than pause.
 */
class MetronomeService : Service() {

    inner class MetronomeBinder : Binder() {
        fun getService(): MetronomeService = this@MetronomeService
    }

    private val binder = MetronomeBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var metronomeJob: Job? = null

    private var toneGenerator: ToneGenerator? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var currentSoundType: SoundType = SoundType.BEEP

    private val _state = MutableStateFlow(MetronomeState())
    val state: StateFlow<MetronomeState> = _state.asStateFlow()

    val isPlaying: Boolean get() = _state.value.isPlaying

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
    }

    /**
     * Start the metronome for the given tempo string (e.g. "3-1-2-0").
     * The cycle repeats indefinitely until [stopMetronome] is called.
     */
    fun startMetronome(tempo: String) {
        val parts = parseTempo(tempo) ?: return
        if (parts.all { it == 0 }) return

        stopMetronome()
        currentSoundType = readMetronomeSoundType()
        if (currentSoundType != SoundType.SILENT && currentSoundType != SoundType.VIBRATE_ONLY) {
            requestAudioFocus()
            initToneGenerator()
        }

        _state.value = MetronomeState(
            isPlaying = true,
            currentPhase = TempoPhase.ECCENTRIC,
            phaseSecondsRemaining = parts[0],
            currentRep = 1
        )

        metronomeJob = serviceScope.launch {
            var rep = 1
            while (true) {
                val phases = listOf(
                    TempoPhase.ECCENTRIC to parts[0],
                    TempoPhase.PAUSE_BOTTOM to parts[1],
                    TempoPhase.CONCENTRIC to parts[2],
                    TempoPhase.PAUSE_TOP to parts[3]
                )

                for ((phase, duration) in phases) {
                    if (duration <= 0) continue

                    // Phase start beep (higher pitch)
                    playTone(TONE_PHASE_START)

                    for (second in duration downTo 1) {
                        _state.value = MetronomeState(
                            isPlaying = true,
                            currentPhase = phase,
                            phaseSecondsRemaining = second,
                            currentRep = rep
                        )
                        delay(1000L)
                        // Tick for each elapsed second (except the last one where
                        // either the next phase beep or cycle-complete will sound)
                        if (second > 1) {
                            playTone(TONE_TICK)
                        }
                    }
                }

                // Cycle complete tone
                playTone(TONE_CYCLE_COMPLETE)
                rep++
            }
        }
    }

    fun stopMetronome() {
        metronomeJob?.cancel()
        metronomeJob = null
        _state.value = MetronomeState()
        releaseToneGenerator()
        abandonAudioFocus()
    }

    // ---- Audio helpers ----

    private fun requestAudioFocus() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener { /* no-op */ }
            .build()

        audioFocusRequest = request
        audioManager?.requestAudioFocus(request)
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        audioFocusRequest = null
    }

    private fun initToneGenerator() {
        releaseToneGenerator()
        toneGenerator = try {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, TONE_VOLUME)
        } catch (_: Exception) {
            null
        }
    }

    private fun releaseToneGenerator() {
        toneGenerator?.release()
        toneGenerator = null
    }

    private fun playTone(toneType: Int) {
        when (currentSoundType) {
            SoundType.SILENT -> return
            SoundType.VIBRATE_ONLY -> {
                vibrateShort()
                return
            }
            else -> { /* play sound below */ }
        }

        val mappedTone = when (currentSoundType) {
            SoundType.DEFAULT, SoundType.BEEP -> toneType
            SoundType.BELL -> when (toneType) {
                TONE_PHASE_START -> ToneGenerator.TONE_SUP_RADIO_NOTAVAIL
                TONE_CYCLE_COMPLETE -> ToneGenerator.TONE_SUP_RADIO_NOTAVAIL
                else -> ToneGenerator.TONE_SUP_RADIO_NOTAVAIL
            }
            SoundType.CHIME -> when (toneType) {
                TONE_PHASE_START -> ToneGenerator.TONE_PROP_ACK
                TONE_CYCLE_COMPLETE -> ToneGenerator.TONE_PROP_ACK
                else -> ToneGenerator.TONE_PROP_PROMPT
            }
            else -> toneType
        }

        try {
            val duration = when (toneType) {
                TONE_PHASE_START -> 150
                TONE_CYCLE_COMPLETE -> 250
                else -> 80
            }
            toneGenerator?.startTone(mappedTone, duration)
        } catch (_: Exception) {
            // Ignore tone generation failures silently
        }
    }

    private fun vibrateShort() {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) { }
    }

    private fun readMetronomeSoundType(): SoundType {
        return try {
            val key = stringPreferencesKey("metronome_sound_type")
            runBlocking {
                applicationContext.dataStore.data.map { prefs ->
                    try {
                        SoundType.valueOf(prefs[key] ?: SoundType.BEEP.name)
                    } catch (_: Exception) {
                        SoundType.BEEP
                    }
                }.first()
            }
        } catch (_: Exception) {
            SoundType.BEEP
        }
    }

    // ---- Lifecycle ----

    override fun onDestroy() {
        stopMetronome()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        /** Higher pitch beep for phase transitions */
        private const val TONE_PHASE_START = ToneGenerator.TONE_PROP_BEEP2
        /** Short tick for each second within a phase */
        private const val TONE_TICK = ToneGenerator.TONE_PROP_BEEP
        /** Distinct tone when a full E-P-C-T cycle completes */
        private const val TONE_CYCLE_COMPLETE = ToneGenerator.TONE_PROP_ACK
        /** Volume 0-100 mapped to ToneGenerator scale */
        private const val TONE_VOLUME = 75

        /**
         * Parse a tempo string "E-P-C-T" into a list of 4 ints.
         */
        fun parseTempo(tempo: String?): List<Int>? {
            if (tempo.isNullOrBlank()) return null
            val parts = tempo.split("-")
            if (parts.size != 4) return null
            return parts.mapNotNull { it.toIntOrNull() }.takeIf { it.size == 4 }
        }
    }
}
