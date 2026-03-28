package com.kb9ut.pror.ui.screen.progress

import com.kb9ut.pror.util.FormatUtils
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.pointer.pointerInput
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.components.GlassCard
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.ui.theme.GoldPR
import com.kb9ut.pror.ui.theme.OnAccent
import com.kb9ut.pror.data.local.entity.MuscleGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_progress)) },
                windowInsets = WindowInsets(0)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Exercise selector
            ExerciseSelector(
                exercises = uiState.allExercises,
                selectedExercise = uiState.selectedExercise,
                onExerciseSelected = { viewModel.selectExercise(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.selectedExercise == null) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.progress_select_exercise),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // PR Summary Cards
                PRSummaryRow(
                    bestWeight = uiState.bestWeight,
                    best1RM = uiState.best1RM,
                    bestVolume = uiState.bestVolume
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 1RM Trend Section
                Text(
                    text = stringResource(R.string.progress_estimated_1rm_trend).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = BrushedSteel,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Period selector
                PeriodSelector(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = { viewModel.selectPeriod(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Chart
                val volumeDataPoints by remember {
                    derivedStateOf { uiState.progressData.map { it.date to it.totalVolume } }
                }

                if (uiState.progressData.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.progress_no_data),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    InspectableLineChart(
                        progressDataPoints = uiState.progressData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        lineColor = Accent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Volume per workout
                if (uiState.progressData.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.progress_best_volume).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = BrushedSteel,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    VolumeList(dataPoints = uiState.progressData)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Volume Trend Chart
                    Text(
                        text = stringResource(R.string.progress_volume_trend).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = BrushedSteel,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LineChart(
                        dataPoints = volumeDataPoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        lineColor = Accent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Weekly Muscle Group Volume (shown always, independent of exercise selection)
            if (uiState.weeklyMuscleVolume.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.progress_weekly_muscle_volume).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = BrushedSteel,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                WeeklyMuscleVolumeChart(
                    muscleVolume = uiState.weeklyMuscleVolume
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseSelector(
    exercises: List<com.kb9ut.pror.data.local.entity.ExerciseEntity>,
    selectedExercise: com.kb9ut.pror.data.local.entity.ExerciseEntity?,
    onExerciseSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedExercise?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.select_exercise)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                focusedLabelColor = Accent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            exercises.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(exercise.name) },
                    onClick = {
                        onExerciseSelected(exercise.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PRSummaryRow(
    bestWeight: PersonalRecord?,
    best1RM: PersonalRecord?,
    bestVolume: PersonalRecord?
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PRCard(
            label = stringResource(R.string.progress_best_weight),
            value = bestWeight?.let { "%.1f".format(it.value) } ?: "-",
            unit = "kg",
            date = bestWeight?.let { dateFormat.format(Date(it.date)) } ?: "",
            modifier = Modifier.weight(1f)
        )
        PRCard(
            label = stringResource(R.string.progress_best_1rm),
            value = best1RM?.let { "%.1f".format(it.value) } ?: "-",
            unit = "kg",
            date = best1RM?.let { dateFormat.format(Date(it.date)) } ?: "",
            modifier = Modifier.weight(1f)
        )
        PRCard(
            label = stringResource(R.string.progress_best_volume),
            value = bestVolume?.let { "%.0f".format(it.value) } ?: "-",
            unit = "kg",
            date = bestVolume?.let { dateFormat.format(Date(it.date)) } ?: "",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PRCard(
    label: String,
    value: String,
    unit: String,
    date: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (value == "-") value else "$value $unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = GoldPR,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (date.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: ProgressPeriod,
    onPeriodSelected: (ProgressPeriod) -> Unit
) {
    val periods = listOf(
        ProgressPeriod.ONE_MONTH to stringResource(R.string.progress_period_1m),
        ProgressPeriod.THREE_MONTHS to stringResource(R.string.progress_period_3m),
        ProgressPeriod.SIX_MONTHS to stringResource(R.string.progress_period_6m),
        ProgressPeriod.ONE_YEAR to stringResource(R.string.progress_period_1y),
        ProgressPeriod.ALL to stringResource(R.string.progress_period_all)
    )

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        periods.forEachIndexed { index, (period, label) ->
            SegmentedButton(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Accent,
                    activeContentColor = OnAccent
                )
            ) {
                Text(text = label, maxLines = 1, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun LineChart(
    dataPoints: List<Pair<Long, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color,
    labelColor: Color
) {
    if (dataPoints.size < 2) {
        // For a single data point, just show the value
        if (dataPoints.size == 1) {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(
                    text = "%.1f kg".format(dataPoints.first().second),
                    style = MaterialTheme.typography.headlineMedium,
                    color = lineColor
                )
            }
        }
        return
    }

    val dateFormat = remember { SimpleDateFormat("M/d", Locale.getDefault()) }
    val yAxisPaint = remember(labelColor) {
        android.graphics.Paint().apply {
            color = labelColor.hashCode()
            textSize = 28f
            textAlign = android.graphics.Paint.Align.RIGHT
        }
    }
    val xAxisPaint = remember(labelColor) {
        android.graphics.Paint().apply {
            color = labelColor.hashCode()
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    Canvas(modifier = modifier) {
        val paddingLeft = 50f
        val paddingRight = 16f
        val paddingTop = 16f
        val paddingBottom = 40f

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val minX = dataPoints.minOf { it.first }.toFloat()
        val maxX = dataPoints.maxOf { it.first }.toFloat()
        val xRange = if (maxX - minX > 0) maxX - minX else 1f

        val minY = dataPoints.minOf { it.second }.toFloat()
        val maxY = dataPoints.maxOf { it.second }.toFloat()
        val yPadding = ((maxY - minY) * 0.1f).coerceAtLeast(1f)
        val adjustedMinY = minY - yPadding
        val adjustedMaxY = maxY + yPadding
        val yRange = adjustedMaxY - adjustedMinY

        fun toScreenX(value: Long): Float =
            paddingLeft + ((value.toFloat() - minX) / xRange) * chartWidth

        fun toScreenY(value: Double): Float =
            paddingTop + chartHeight - ((value.toFloat() - adjustedMinY) / yRange) * chartHeight

        // Draw Y-axis labels
        val ySteps = 4
        for (i in 0..ySteps) {
            val yVal = adjustedMinY + (yRange / ySteps) * i
            val y = toScreenY(yVal.toDouble())
            drawContext.canvas.nativeCanvas.drawText(
                "%.0f".format(yVal),
                paddingLeft - 8f,
                y + 10f,
                yAxisPaint
            )
            // Grid line
            drawLine(
                color = labelColor.copy(alpha = 0.15f),
                start = Offset(paddingLeft, y),
                end = Offset(size.width - paddingRight, y),
                strokeWidth = 1f
            )
        }

        // Draw X-axis labels (show a few dates)
        val labelCount = (dataPoints.size).coerceAtMost(5)
        val step = (dataPoints.size - 1).coerceAtLeast(1) / labelCount.coerceAtLeast(1)
        for (i in 0 until labelCount) {
            val idx = (i * step).coerceAtMost(dataPoints.lastIndex)
            val dp = dataPoints[idx]
            val x = toScreenX(dp.first)
            drawContext.canvas.nativeCanvas.drawText(
                dateFormat.format(Date(dp.first)),
                x,
                size.height - 4f,
                xAxisPaint
            )
        }
        // Always show last label
        val lastDp = dataPoints.last()
        drawContext.canvas.nativeCanvas.drawText(
            dateFormat.format(Date(lastDp.first)),
            toScreenX(lastDp.first),
            size.height - 4f,
            xAxisPaint
        )

        // Draw line
        val path = Path()
        dataPoints.forEachIndexed { index, (x, y) ->
            val sx = toScreenX(x)
            val sy = toScreenY(y)
            if (index == 0) {
                path.moveTo(sx, sy)
            } else {
                path.lineTo(sx, sy)
            }
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3f)
        )

        // Draw fill area with 10% opacity
        val fillPath = Path()
        dataPoints.forEachIndexed { index, (x, y) ->
            val sx = toScreenX(x)
            val sy = toScreenY(y)
            if (index == 0) {
                fillPath.moveTo(sx, sy)
            } else {
                fillPath.lineTo(sx, sy)
            }
        }
        // Close the path down to the bottom of the chart
        val lastX = toScreenX(dataPoints.last().first)
        val firstX = toScreenX(dataPoints.first().first)
        val bottomY = paddingTop + chartHeight
        fillPath.lineTo(lastX, bottomY)
        fillPath.lineTo(firstX, bottomY)
        fillPath.close()
        drawPath(
            path = fillPath,
            color = lineColor.copy(alpha = 0.1f),
            style = Fill
        )

        // Draw dots (4dp = ~5.5f radius)
        dataPoints.forEach { (x, y) ->
            val sx = toScreenX(x)
            val sy = toScreenY(y)
            drawCircle(
                color = lineColor,
                radius = 5.5f,
                center = Offset(sx, sy)
            )
        }
    }
}

@Composable
private fun InspectableLineChart(
    progressDataPoints: List<ProgressDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color,
    labelColor: Color
) {
    val dataPoints = remember(progressDataPoints) {
        progressDataPoints.map { it.date to it.estimated1RM }
    }

    if (dataPoints.size < 2) {
        if (dataPoints.size == 1) {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(
                    text = "%.1f kg".format(dataPoints.first().second),
                    style = MaterialTheme.typography.headlineMedium,
                    color = lineColor
                )
            }
        }
        return
    }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val tooltipFormat1RM = stringResource(R.string.progress_chart_1rm_tooltip)
    val tooltipFormatSet = stringResource(R.string.progress_chart_set_detail)
    val tapHint = stringResource(R.string.progress_chart_tap_hint)

    // Chart layout constants (must match LineChart's padding)
    val paddingLeft = 50f
    val paddingRight = 16f
    val paddingTop = 16f
    val paddingBottom = 40f

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Reset selection when data changes
    LaunchedEffect(progressDataPoints) {
        selectedIndex = null
    }

    Box(modifier = modifier) {
        // Render the base chart with Canvas
        LineChart(
            dataPoints = dataPoints,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(progressDataPoints) {
                    detectTapGestures { tapOffset ->
                        val chartWidth = size.width - paddingLeft - paddingRight
                        val chartHeight = size.height - paddingTop - paddingBottom

                        val minX = dataPoints.minOf { it.first }.toFloat()
                        val maxX = dataPoints.maxOf { it.first }.toFloat()
                        val xRange = if (maxX - minX > 0) maxX - minX else 1f

                        val minY = dataPoints.minOf { it.second }.toFloat()
                        val maxY = dataPoints.maxOf { it.second }.toFloat()
                        val yPadding = ((maxY - minY) * 0.1f).coerceAtLeast(1f)
                        val adjustedMinY = minY - yPadding
                        val adjustedMaxY = maxY + yPadding
                        val yRange = adjustedMaxY - adjustedMinY

                        fun toScreenX(value: Long): Float =
                            paddingLeft + ((value.toFloat() - minX) / xRange) * chartWidth

                        fun toScreenY(value: Double): Float =
                            paddingTop + chartHeight -
                                ((value.toFloat() - adjustedMinY) / yRange) * chartHeight

                        // Find nearest data point within tap radius
                        val tapRadius = 60f
                        var bestIdx: Int? = null
                        var bestDist = Float.MAX_VALUE

                        dataPoints.forEachIndexed { index, (x, y) ->
                            val sx = toScreenX(x)
                            val sy = toScreenY(y)
                            val dx = tapOffset.x - sx
                            val dy = tapOffset.y - sy
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist < tapRadius && dist < bestDist) {
                                bestDist = dist
                                bestIdx = index
                            }
                        }

                        selectedIndex = if (bestIdx == selectedIndex) null else bestIdx
                    }
                },
            lineColor = lineColor,
            labelColor = labelColor
        )

        // Draw selected point highlight and tooltip
        val idx = selectedIndex
        if (idx != null && idx in progressDataPoints.indices) {
            val dp = progressDataPoints[idx]

            // We use a secondary Canvas overlay for the highlight ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartWidth = size.width - paddingLeft - paddingRight
                val chartHeight = size.height - paddingTop - paddingBottom

                val minX = dataPoints.minOf { it.first }.toFloat()
                val maxX = dataPoints.maxOf { it.first }.toFloat()
                val xRange = if (maxX - minX > 0) maxX - minX else 1f

                val minY = dataPoints.minOf { it.second }.toFloat()
                val maxY = dataPoints.maxOf { it.second }.toFloat()
                val yPadding = ((maxY - minY) * 0.1f).coerceAtLeast(1f)
                val adjustedMinY = minY - yPadding
                val adjustedMaxY = maxY + yPadding
                val yRange = adjustedMaxY - adjustedMinY

                val sx = paddingLeft + ((dp.date.toFloat() - minX) / xRange) * chartWidth
                val sy = paddingTop + chartHeight -
                    ((dp.estimated1RM.toFloat() - adjustedMinY) / yRange) * chartHeight

                // Vertical guide line
                drawLine(
                    color = lineColor.copy(alpha = 0.4f),
                    start = Offset(sx, paddingTop),
                    end = Offset(sx, paddingTop + chartHeight),
                    strokeWidth = 1.5f
                )

                // Highlight ring around selected point
                drawCircle(
                    color = lineColor,
                    radius = 9f,
                    center = Offset(sx, sy),
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = Color.Black,
                    radius = 5f,
                    center = Offset(sx, sy)
                )
                drawCircle(
                    color = lineColor,
                    radius = 3.5f,
                    center = Offset(sx, sy)
                )
            }

            // Tooltip card - positioned above the chart
            val tooltipDate = dateFormat.format(Date(dp.date))
            val tooltip1RM = tooltipFormat1RM.format(dp.estimated1RM)
            val tooltipSet = if (dp.best1RMWeight > 0 && dp.best1RMReps > 0) {
                tooltipFormatSet.format(dp.best1RMWeight, dp.best1RMReps)
            } else {
                null
            }

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 0.dp)
                    .border(
                        width = 1.dp,
                        color = lineColor.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tooltipDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrushedSteel
                    )
                    Text(
                        text = tooltip1RM,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldPR
                    )
                    if (tooltipSet != null) {
                        Text(
                            text = tooltipSet,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Show tap hint when nothing is selected
            Text(
                text = tapHint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp)
            )
        }
    }
}

@Composable
private fun VolumeList(
    dataPoints: List<ProgressDataPoint>
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val maxVolume = dataPoints.maxOf { it.totalVolume }.coerceAtLeast(1.0)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        dataPoints.takeLast(10).reversed().forEach { dp ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(dp.date)),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(80.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    val fraction = (dp.totalVolume / maxVolume).toFloat()
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(20.dp)
                    ) {
                        drawRect(
                            color = Accent.copy(alpha = 0.6f),
                            size = size
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "%.0f kg".format(dp.totalVolume),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(72.dp)
                )
            }
        }
    }
}

@Composable
private fun muscleGroupDisplayName(muscleGroup: MuscleGroup): String {
    return when (muscleGroup) {
        MuscleGroup.CHEST -> stringResource(R.string.muscle_chest)
        MuscleGroup.BACK -> stringResource(R.string.muscle_back)
        MuscleGroup.SHOULDERS -> stringResource(R.string.muscle_shoulders)
        MuscleGroup.BICEPS -> stringResource(R.string.muscle_biceps)
        MuscleGroup.TRICEPS -> stringResource(R.string.muscle_triceps)
        MuscleGroup.FOREARMS -> stringResource(R.string.muscle_forearms)
        MuscleGroup.QUADRICEPS -> stringResource(R.string.muscle_quadriceps)
        MuscleGroup.HAMSTRINGS -> stringResource(R.string.muscle_hamstrings)
        MuscleGroup.GLUTES -> stringResource(R.string.muscle_glutes)
        MuscleGroup.CALVES -> stringResource(R.string.muscle_calves)
        MuscleGroup.ABS -> stringResource(R.string.muscle_abs)
        MuscleGroup.OBLIQUES -> stringResource(R.string.muscle_obliques)
        MuscleGroup.TRAPS -> stringResource(R.string.muscle_traps)
        MuscleGroup.LATS -> stringResource(R.string.muscle_lats)
        MuscleGroup.FULL_BODY -> stringResource(R.string.muscle_full_body)
        MuscleGroup.CARDIO -> stringResource(R.string.muscle_cardio)
        MuscleGroup.OTHER -> stringResource(R.string.muscle_other)
    }
}

@Composable
private fun WeeklyMuscleVolumeChart(
    muscleVolume: Map<MuscleGroup, Double>
) {
    val sortedEntries = remember(muscleVolume) { muscleVolume.entries.sortedByDescending { it.value } }
    val maxVolume = remember(sortedEntries) { sortedEntries.maxOfOrNull { it.value }?.coerceAtLeast(1.0) ?: 1.0 }
    val barColor = MaterialTheme.colorScheme.primary

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        sortedEntries.forEach { (muscleGroup, volume) ->
            val displayName = muscleGroupDisplayName(muscleGroup)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(88.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    val fraction = (volume / maxVolume).toFloat().coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor.copy(alpha = 0.7f))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "%.0f kg".format(volume),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(72.dp)
                )
            }
        }
    }
}
