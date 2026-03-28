package com.kb9ut.pror.ui.screen.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kb9ut.pror.data.backup.BackupManager
import com.kb9ut.pror.data.backup.CsvImportManager
import com.kb9ut.pror.data.backup.CsvImportResult
import com.kb9ut.pror.data.local.UserPreferencesManager
import com.kb9ut.pror.data.local.WeightUnit
import com.kb9ut.pror.data.local.entity.SoundType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val userPreferencesManager: UserPreferencesManager,
    private val backupManager: BackupManager,
    private val csvImportManager: CsvImportManager
) : ViewModel() {

    private val _userMessage = MutableStateFlow<UserMessage?>(null)
    val userMessage: StateFlow<UserMessage?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonString = backupManager.exportToJson()
                application.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("Could not open output stream")
                _userMessage.value = UserMessage.ExportSuccess
            } catch (e: Exception) {
                _userMessage.value = UserMessage.ExportError(e.message ?: "Unknown error")
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonString = application.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                } ?: throw IllegalStateException("Could not open input stream")
                backupManager.importFromJson(jsonString)
                _userMessage.value = UserMessage.ImportSuccess
            } catch (e: Exception) {
                _userMessage.value = UserMessage.ImportError(e.message ?: "Unknown error")
            }
        }
    }

    fun importCsvData(uri: Uri) {
        viewModelScope.launch {
            try {
                val csvContent = application.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                } ?: throw IllegalStateException("Could not open input stream")
                val result = csvImportManager.importFromCsv(csvContent)
                _userMessage.value = UserMessage.CsvImportSuccess(result)
            } catch (e: Exception) {
                val errorMessage = if (e.message == "UNKNOWN_FORMAT") {
                    "UNKNOWN_FORMAT"
                } else {
                    e.message ?: "Unknown error"
                }
                _userMessage.value = UserMessage.CsvImportError(errorMessage)
            }
        }
    }

    val weightUnit: StateFlow<WeightUnit> =
        userPreferencesManager.weightUnit
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUnit.KG)

    val defaultRestTimerSeconds: StateFlow<Int> =
        userPreferencesManager.defaultRestTimerSeconds
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 90)

    val oneRmFormula: StateFlow<String> =
        userPreferencesManager.oneRmFormula
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "median")

    val themeMode: StateFlow<String> =
        userPreferencesManager.themeMode
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val timerSoundType: StateFlow<SoundType> =
        userPreferencesManager.timerSoundType
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SoundType.DEFAULT)

    val metronomeSoundType: StateFlow<SoundType> =
        userPreferencesManager.metronomeSoundType
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SoundType.BEEP)

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            userPreferencesManager.setWeightUnit(unit)
        }
    }

    fun setDefaultRestTimerSeconds(seconds: Int) {
        viewModelScope.launch {
            userPreferencesManager.setDefaultRestTimerSeconds(seconds)
        }
    }

    fun setOneRmFormula(formula: String) {
        viewModelScope.launch {
            userPreferencesManager.setOneRmFormula(formula)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferencesManager.setThemeMode(mode)
        }
    }

    fun setTimerSoundType(soundType: SoundType) {
        viewModelScope.launch {
            userPreferencesManager.setTimerSoundType(soundType)
        }
    }

    fun setMetronomeSoundType(soundType: SoundType) {
        viewModelScope.launch {
            userPreferencesManager.setMetronomeSoundType(soundType)
        }
    }
}

sealed class UserMessage {
    data object ExportSuccess : UserMessage()
    data class ExportError(val message: String) : UserMessage()
    data object ImportSuccess : UserMessage()
    data class ImportError(val message: String) : UserMessage()
    data class CsvImportSuccess(val result: CsvImportResult) : UserMessage()
    data class CsvImportError(val message: String) : UserMessage()
}
