package com.kb9ut.pror.ui.screen.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kb9ut.pror.R
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import com.kb9ut.pror.data.local.WeightUnit
import com.kb9ut.pror.data.local.entity.SoundType
import com.kb9ut.pror.ui.components.GlassCard
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.ui.theme.DarkSteel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()
    val restTimerSeconds by viewModel.defaultRestTimerSeconds.collectAsStateWithLifecycle()
    val oneRmFormula by viewModel.oneRmFormula.collectAsStateWithLifecycle()
    val timerSoundType by viewModel.timerSoundType.collectAsStateWithLifecycle()
    val metronomeSoundType by viewModel.metronomeSoundType.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val exportSuccessText = stringResource(R.string.export_success)
    val exportErrorText = stringResource(R.string.export_error)
    val importSuccessText = stringResource(R.string.import_success)
    val importErrorText = stringResource(R.string.import_error)
    val csvImportErrorText = stringResource(R.string.csv_import_error)
    val csvImportUnknownFormatText = stringResource(R.string.csv_import_unknown_format)

    LaunchedEffect(userMessage) {
        val message = userMessage ?: return@LaunchedEffect
        val text = when (message) {
            is UserMessage.ExportSuccess -> exportSuccessText
            is UserMessage.ExportError -> "$exportErrorText: ${message.message}"
            is UserMessage.ImportSuccess -> importSuccessText
            is UserMessage.ImportError -> "$importErrorText: ${message.message}"
            is UserMessage.CsvImportSuccess -> {
                val r = message.result
                context.getString(
                    R.string.csv_import_success,
                    r.workoutsImported,
                    r.exercisesCreated,
                    r.setsImported
                )
            }
            is UserMessage.CsvImportError -> {
                if (message.message == "UNKNOWN_FORMAT") {
                    csvImportUnknownFormatText
                } else {
                    "$csvImportErrorText: ${message.message}"
                }
            }
        }
        snackbarHostState.showSnackbar(text)
        viewModel.clearUserMessage()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportData(uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importData(uri)
        }
    }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importCsvData(uri)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // General section
                item {
                    SectionHeader(stringResource(R.string.settings_general))
                }

                // Weight unit
                item {
                    DropdownSettingItem(
                        title = stringResource(R.string.unit_setting),
                        currentValue = when (weightUnit) {
                            WeightUnit.KG -> stringResource(R.string.kg)
                            WeightUnit.LBS -> stringResource(R.string.lbs)
                        },
                        icon = Icons.Filled.FitnessCenter,
                        options = listOf(
                            stringResource(R.string.kg) to WeightUnit.KG,
                            stringResource(R.string.lbs) to WeightUnit.LBS
                        ),
                        onSelect = { viewModel.setWeightUnit(it) }
                    )
                }

                // Default rest timer
                item {
                    val restTimerOptions = listOf(30, 60, 90, 120, 180, 300)
                    DropdownSettingItem(
                        title = stringResource(R.string.rest_timer_default),
                        currentValue = stringResource(R.string.rest_timer_seconds, restTimerSeconds),
                        icon = Icons.Filled.Timer,
                        options = restTimerOptions.map { seconds ->
                            stringResource(R.string.rest_timer_seconds, seconds) to seconds
                        },
                        onSelect = { viewModel.setDefaultRestTimerSeconds(it) }
                    )
                }

                // 1RM formula
                item {
                    val formulaOptions = listOf(
                        "Brzycki (1)" to "brzycki1",
                        "Brzycki (2)" to "brzycki2",
                        "Epley" to "epley",
                        "Lander" to "lander",
                        "Lombardi" to "lombardi",
                        "Mayhew" to "mayhew",
                        "O'Conner" to "oconner",
                        "Wathan" to "wathan",
                        stringResource(R.string.settings_1rm_median) to "median",
                        stringResource(R.string.settings_1rm_average) to "average"
                    )
                    val currentLabel = formulaOptions.firstOrNull { it.second == oneRmFormula }?.first ?: oneRmFormula
                    DropdownSettingItem(
                        title = stringResource(R.string.one_rm_formula),
                        currentValue = currentLabel,
                        icon = Icons.Filled.Functions,
                        options = formulaOptions,
                        onSelect = { viewModel.setOneRmFormula(it) }
                    )
                }

                // Timer sound
                item {
                    SoundSettingItem(
                        title = stringResource(R.string.settings_timer_sound),
                        currentSoundType = timerSoundType,
                        icon = Icons.Filled.VolumeUp,
                        context = context,
                        onSelect = { viewModel.setTimerSoundType(it) }
                    )
                }

                // Metronome sound
                item {
                    SoundSettingItem(
                        title = stringResource(R.string.settings_metronome_sound),
                        currentSoundType = metronomeSoundType,
                        icon = Icons.Filled.MusicNote,
                        context = context,
                        onSelect = { viewModel.setMetronomeSoundType(it) }
                    )
                }

                // Data section
                item {
                    SectionHeader(stringResource(R.string.settings_data))
                }

                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevated = true
                    ) {
                        Column {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.export_data)) },
                                leadingContent = {
                                    Icon(
                                        Icons.Outlined.FileUpload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.clickable {
                                    val timestamp = LocalDateTime.now()
                                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                                    exportLauncher.launch("reppen_backup_$timestamp.json")
                                }
                            )

                            ListItem(
                                headlineContent = { Text(stringResource(R.string.import_data)) },
                                leadingContent = {
                                    Icon(
                                        Icons.Outlined.FileDownload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.clickable {
                                    importLauncher.launch(arrayOf("application/json"))
                                }
                            )

                            ListItem(
                                headlineContent = { Text(stringResource(R.string.import_csv)) },
                                leadingContent = {
                                    Icon(
                                        Icons.Outlined.TableChart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.clickable {
                                    csvImportLauncher.launch(arrayOf("text/*"))
                                }
                            )
                        }
                    }
                }

                // About section
                item {
                    SectionHeader(stringResource(R.string.settings_about))
                }

                item {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_version)) },
                        supportingContent = {
                            Text(
                                text = "0.1.0",
                                color = Accent
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 4.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = BrushedSteel,
            letterSpacing = 1.5.sp
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 0.5.dp,
            color = DarkSteel
        )
    }
}

@Composable
private fun SoundSettingItem(
    title: String,
    currentSoundType: SoundType,
    icon: ImageVector,
    context: android.content.Context,
    onSelect: (SoundType) -> Unit
) {
    val soundOptions = listOf(
        stringResource(R.string.sound_default) to SoundType.DEFAULT,
        stringResource(R.string.sound_beep) to SoundType.BEEP,
        stringResource(R.string.sound_bell) to SoundType.BELL,
        stringResource(R.string.sound_chime) to SoundType.CHIME,
        stringResource(R.string.sound_vibrate_only) to SoundType.VIBRATE_ONLY,
        stringResource(R.string.sound_silent) to SoundType.SILENT
    )
    val currentLabel = soundOptions.firstOrNull { it.second == currentSoundType }?.first
        ?: currentSoundType.name

    var expanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Text(
                text = currentLabel,
                color = Accent
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable { expanded = true }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        soundOptions.forEach { (label, value) ->
            DropdownMenuItem(
                text = { Text(label) },
                onClick = {
                    onSelect(value)
                    expanded = false
                    previewSound(context, value)
                }
            )
        }
    }
}

private fun previewSound(context: android.content.Context, soundType: SoundType) {
    when (soundType) {
        SoundType.DEFAULT -> {
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val mediaPlayer = MediaPlayer().apply {
                    val attrs = android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setAudioAttributes(attrs)
                    setDataSource(context, uri)
                    prepare()
                    setOnCompletionListener { mp -> mp.release() }
                }
                mediaPlayer.start()
            } catch (_: Exception) { }
        }
        SoundType.BEEP -> {
            playPreviewTone(ToneGenerator.TONE_PROP_BEEP)
        }
        SoundType.BELL -> {
            playPreviewTone(ToneGenerator.TONE_SUP_RADIO_NOTAVAIL)
        }
        SoundType.CHIME -> {
            playPreviewTone(ToneGenerator.TONE_PROP_ACK)
        }
        SoundType.VIBRATE_ONLY -> {
            try {
                val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                    vm.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                }
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } catch (_: Exception) { }
        }
        SoundType.SILENT -> {
            // No preview for silent
        }
    }
}

private fun playPreviewTone(toneType: Int) {
    try {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
        toneGenerator.startTone(toneType, 200)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            toneGenerator.release()
        }, 300)
    } catch (_: Exception) { }
}

@Composable
private fun <T> DropdownSettingItem(
    title: String,
    currentValue: String,
    icon: ImageVector,
    options: List<Pair<String, T>>,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Text(
                text = currentValue,
                color = Accent
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable { expanded = true }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEach { (label, value) ->
            DropdownMenuItem(
                text = { Text(label) },
                onClick = {
                    onSelect(value)
                    expanded = false
                }
            )
        }
    }
}
