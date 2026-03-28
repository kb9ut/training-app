package com.kb9ut.pror.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.CountDownTimer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kb9ut.pror.MainActivity
import com.kb9ut.pror.data.local.dataStore
import com.kb9ut.pror.data.local.entity.SoundType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

data class RestTimerState(
    val isRunning: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0
)

class RestTimerService : Service() {

    companion object {
        private const val CHANNEL_ID = "rest_timer"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.kb9ut.pror.REST_TIMER_START"
        const val ACTION_STOP = "com.kb9ut.pror.REST_TIMER_STOP"
        const val ACTION_ADD_TIME = "com.kb9ut.pror.REST_TIMER_ADD_TIME"
        const val ACTION_PAUSE = "com.kb9ut.pror.REST_TIMER_PAUSE"
        const val ACTION_RESUME = "com.kb9ut.pror.REST_TIMER_RESUME"

        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
        const val EXTRA_ADD_SECONDS = "extra_add_seconds"

        private val _timerState = MutableStateFlow(RestTimerState())
        val timerState: StateFlow<RestTimerState> = _timerState.asStateFlow()
    }

    private var countDownTimer: CountDownTimer? = null
    private var remainingMillis: Long = 0L
    private var totalSeconds: Int = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val durationSeconds = intent.getIntExtra(EXTRA_DURATION_SECONDS, 90)
                startTimer(durationSeconds)
            }
            ACTION_ADD_TIME -> {
                val seconds = intent.getIntExtra(EXTRA_ADD_SECONDS, 30)
                addTime(seconds)
            }
            ACTION_STOP -> {
                stopTimer()
            }
            ACTION_PAUSE -> {
                pauseTimer()
            }
            ACTION_RESUME -> {
                resumeTimer()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rest Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Rest timer countdown notifications"
            setShowBadge(false)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun startTimer(durationSeconds: Int) {
        countDownTimer?.cancel()
        totalSeconds = durationSeconds
        remainingMillis = durationSeconds * 1000L

        _timerState.value = RestTimerState(
            isRunning = true,
            remainingSeconds = durationSeconds,
            totalSeconds = durationSeconds
        )

        startForeground(NOTIFICATION_ID, buildNotification(durationSeconds))
        startCountDown(remainingMillis)
    }

    private fun startCountDown(millis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                val seconds = (millisUntilFinished / 1000).toInt() + 1
                _timerState.value = RestTimerState(
                    isRunning = true,
                    remainingSeconds = seconds,
                    totalSeconds = totalSeconds
                )
                updateNotification(seconds)
            }

            override fun onFinish() {
                _timerState.value = RestTimerState(
                    isRunning = false,
                    remainingSeconds = 0,
                    totalSeconds = totalSeconds
                )
                val soundType = readTimerSoundType()
                when (soundType) {
                    SoundType.SILENT -> { /* no sound, no vibration */ }
                    SoundType.VIBRATE_ONLY -> vibrateCompletion()
                    else -> {
                        vibrateCompletion()
                        playCompletionSound(soundType)
                    }
                }
                stopSelf()
            }
        }.start()
    }

    private fun addTime(seconds: Int) {
        val currentState = _timerState.value
        if (!currentState.isRunning) return

        countDownTimer?.cancel()
        remainingMillis = (remainingMillis + seconds * 1000L).coerceAtLeast(1000L)
        val newTotalSeconds = (currentState.totalSeconds + seconds).coerceAtLeast(1)
        totalSeconds = newTotalSeconds

        _timerState.value = currentState.copy(
            remainingSeconds = (remainingMillis / 1000).toInt() + 1,
            totalSeconds = newTotalSeconds
        )
        startCountDown(remainingMillis)
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        val currentState = _timerState.value
        _timerState.value = currentState.copy(isRunning = false)
        updateNotification(currentState.remainingSeconds)
    }

    private fun resumeTimer() {
        _timerState.value = _timerState.value.copy(isRunning = true)
        startCountDown(remainingMillis)
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        _timerState.value = RestTimerState()
        stopSelf()
    }

    private fun buildNotification(remainingSeconds: Int): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val addTimeIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, RestTimerService::class.java).apply { action = ACTION_ADD_TIME },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, RestTimerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setContentTitle("Rest Timer")
            .setContentText(formatTime(remainingSeconds))
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(0, "+30s", addTimeIntent)
            .addAction(0, "Skip", stopIntent)
            .build()
    }

    private fun updateNotification(remainingSeconds: Int) {
        val notification = buildNotification(remainingSeconds)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return "%d:%02d".format(minutes, secs)
    }

    private fun vibrateCompletion() {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // Short-Long-Short pattern
        val timings = longArrayOf(0, 100, 100, 400, 100, 100)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator.vibrate(effect)
    }

    /**
     * Read the timer sound type from DataStore synchronously.
     * Called from the timer's onFinish callback.
     */
    private fun readTimerSoundType(): SoundType {
        return try {
            val dataStore = applicationContext.let { ctx ->
                val key = stringPreferencesKey("timer_sound_type")
                runBlocking {
                    ctx.dataStore.data.map { prefs ->
                        try {
                            SoundType.valueOf(prefs[key] ?: SoundType.DEFAULT.name)
                        } catch (_: Exception) {
                            SoundType.DEFAULT
                        }
                    }.first()
                }
            }
            dataStore
        } catch (_: Exception) {
            SoundType.DEFAULT
        }
    }

    /**
     * Play a short notification sound without interrupting music apps.
     * Uses AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK so other audio (Spotify etc.)
     * is only ducked (volume lowered briefly), never paused.
     * If audio focus is denied, the vibration alone serves as the alert.
     */
    private fun playCompletionSound(soundType: SoundType = SoundType.DEFAULT) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(audioAttributes)
            .build()

        val focusResult = audioManager.requestAudioFocus(focusRequest)
        if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return
        }

        when (soundType) {
            SoundType.DEFAULT -> {
                try {
                    val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(audioAttributes)
                        setDataSource(this@RestTimerService, notificationUri)
                        prepare()
                        setOnCompletionListener { mp ->
                            mp.release()
                            audioManager.abandonAudioFocusRequest(focusRequest)
                        }
                    }
                    mediaPlayer.start()
                } catch (_: Exception) {
                    audioManager.abandonAudioFocusRequest(focusRequest)
                }
            }
            SoundType.BEEP -> {
                playToneAndReleaseFocus(ToneGenerator.TONE_PROP_BEEP, audioManager, focusRequest)
            }
            SoundType.BELL -> {
                playToneAndReleaseFocus(ToneGenerator.TONE_SUP_RADIO_NOTAVAIL, audioManager, focusRequest)
            }
            SoundType.CHIME -> {
                playToneAndReleaseFocus(ToneGenerator.TONE_PROP_ACK, audioManager, focusRequest)
            }
            else -> {
                audioManager.abandonAudioFocusRequest(focusRequest)
            }
        }
    }

    private fun playToneAndReleaseFocus(
        toneType: Int,
        audioManager: AudioManager,
        focusRequest: AudioFocusRequest
    ) {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
            toneGenerator.startTone(toneType, 300)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                toneGenerator.release()
                audioManager.abandonAudioFocusRequest(focusRequest)
            }, 400)
        } catch (_: Exception) {
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        _timerState.value = RestTimerState()
        super.onDestroy()
    }
}
