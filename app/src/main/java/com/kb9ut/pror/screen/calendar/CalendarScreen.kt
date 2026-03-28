package com.kb9ut.pror.ui.screen.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.components.GlassCard
import com.kb9ut.pror.ui.navigation.Screen
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.AccentSubtle
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import com.kb9ut.pror.util.DateTimeUtils
import com.kb9ut.pror.util.FormatUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val currentYearMonth by viewModel.currentYearMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val workoutDates by viewModel.workoutDates.collectAsStateWithLifecycle()
    val workoutsForSelectedDay by viewModel.workoutsForSelectedDay.collectAsStateWithLifecycle()
    val today = remember { LocalDate.now() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.nav_calendar)) },
            windowInsets = WindowInsets(0),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Month navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.cd_previous_month)
                )
            }
            Text(
                text = formatYearMonth(currentYearMonth),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.cd_next_month)
                )
            }
        }

        // Weekday headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            val locale = Locale.getDefault()
            val weekDays = listOf(
                DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
            )
            weekDays.forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, locale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar grid
        val firstDayOfMonth = currentYearMonth.atDay(1)
        // DayOfWeek.SUNDAY = 7 in java.time, we want it as column 0
        val firstDayOffset = firstDayOfMonth.dayOfWeek.value % 7
        val daysInMonth = currentYearMonth.lengthOfMonth()
        val totalCells = firstDayOffset + daysInMonth
        val rows = (totalCells + 6) / 7
        val cellCount = rows * 7

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            userScrollEnabled = false,
            contentPadding = PaddingValues(0.dp)
        ) {
            items(cellCount, key = { it }) { index ->
                val dayOfMonth = index - firstDayOffset + 1
                if (dayOfMonth in 1..daysInMonth) {
                    val date = currentYearMonth.atDay(dayOfMonth)
                    val isSelected = date == selectedDate
                    val isToday = date == today
                    val hasWorkout = dayOfMonth in workoutDates

                    DayCell(
                        day = dayOfMonth,
                        isSelected = isSelected,
                        isToday = isToday,
                        hasWorkout = hasWorkout,
                        onClick = { viewModel.selectDate(date) }
                    )
                } else {
                    // Empty cell for days outside current month
                    Box(modifier = Modifier.size(48.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected day workouts
        if (selectedDate != null) {
            if (workoutsForSelectedDay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.calendar_no_workouts_on_day),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workoutsForSelectedDay, key = { it.id }) { workout ->
                        WorkoutCard(
                            workout = workout,
                            onClick = { navController.navigate(Screen.WorkoutDetail.createRoute(workout.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasWorkout: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .then(
                when {
                    isSelected -> Modifier.background(AccentSubtle, CircleShape)
                        .border(1.dp, Accent, CircleShape)
                    isToday -> Modifier
                        .background(Accent.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, Accent, CircleShape)
                    else -> Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> Accent
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
            if (hasWorkout) {
                Box(contentAlignment = Alignment.Center) {
                    // Glow layer behind the dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = CircleShape,
                                ambientColor = Accent,
                                spotColor = Accent
                            )
                            .background(
                                color = Accent.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                    // Solid dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = Accent,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: WorkoutEntity,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = true,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = DateTimeUtils.formatDateTime(workout.startedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                workout.durationSeconds?.let { seconds ->
                    Text(
                        text = FormatUtils.formatDurationShort(seconds),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = BrushedSteel,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            )
        }
    }
}

private val yearMonthFormatterJa: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy年 M月", Locale.JAPAN)
private val yearMonthFormatterDefault: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

private fun formatYearMonth(yearMonth: YearMonth): String {
    val formatter = if (Locale.getDefault().language == "ja") yearMonthFormatterJa else yearMonthFormatterDefault
    return yearMonth.atDay(1).format(formatter)
}
