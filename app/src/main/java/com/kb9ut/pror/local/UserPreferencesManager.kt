package com.kb9ut.pror.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kb9ut.pror.data.local.entity.SoundType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class WeightUnit {
    KG, LBS
}

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val DEFAULT_REST_TIMER_SECONDS = intPreferencesKey("default_rest_timer_seconds")
        val ONE_RM_FORMULA = stringPreferencesKey("one_rm_formula")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val TIMER_SOUND_TYPE = stringPreferencesKey("timer_sound_type")
        val METRONOME_SOUND_TYPE = stringPreferencesKey("metronome_sound_type")
    }

    val weightUnit: Flow<WeightUnit> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.WEIGHT_UNIT]) {
            "lbs" -> WeightUnit.LBS
            else -> WeightUnit.KG
        }
    }

    val defaultRestTimerSeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_REST_TIMER_SECONDS] ?: 90
    }

    val oneRmFormula: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONE_RM_FORMULA] ?: "median"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "system"
    }

    val timerSoundType: Flow<SoundType> = context.dataStore.data.map { prefs ->
        try {
            SoundType.valueOf(prefs[Keys.TIMER_SOUND_TYPE] ?: SoundType.DEFAULT.name)
        } catch (_: Exception) {
            SoundType.DEFAULT
        }
    }

    val metronomeSoundType: Flow<SoundType> = context.dataStore.data.map { prefs ->
        try {
            SoundType.valueOf(prefs[Keys.METRONOME_SOUND_TYPE] ?: SoundType.BEEP.name)
        } catch (_: Exception) {
            SoundType.BEEP
        }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WEIGHT_UNIT] = when (unit) {
                WeightUnit.KG -> "kg"
                WeightUnit.LBS -> "lbs"
            }
        }
    }

    suspend fun setDefaultRestTimerSeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_REST_TIMER_SECONDS] = seconds
        }
    }

    suspend fun setOneRmFormula(formula: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONE_RM_FORMULA] = formula
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    suspend fun setTimerSoundType(soundType: SoundType) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TIMER_SOUND_TYPE] = soundType.name
        }
    }

    suspend fun setMetronomeSoundType(soundType: SoundType) {
        context.dataStore.edit { prefs ->
            prefs[Keys.METRONOME_SOUND_TYPE] = soundType.name
        }
    }
}
