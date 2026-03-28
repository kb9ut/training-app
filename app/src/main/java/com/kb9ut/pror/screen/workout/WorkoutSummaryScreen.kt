package com.kb9ut.pror.ui.screen.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.ui.theme.GoldPR
import com.kb9ut.pror.ui.theme.OnAccent
import com.kb9ut.pror.util.FormatUtils

@Composable
fun WorkoutSummaryScreen(
    navController: NavController,
    viewModel: WorkoutSummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Celebratory icon
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = stringResource(R.string.cd_workout_complete),
            modifier = Modifier.size(72.dp),
            tint = GoldPR
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = stringResource(R.string.workout_summary_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = GoldPR,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Workout name
        val workoutName = uiState.workout?.name ?: ""
        if (workoutName.isNotBlank()) {
            Text(
                text = workoutName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Stats card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryStatRow(
                    icon = Icons.Filled.Timer,
                    label = stringResource(R.string.workout_detail_duration),
                    value = FormatUtils.formatDuration(uiState.durationSeconds)
                )

                HorizontalDivider(color = BrushedSteel.copy(alpha = 0.5f))

                SummaryStatRow(
                    icon = Icons.Filled.FitnessCenter,
                    label = stringResource(R.string.workout_detail_total_volume),
                    value = FormatUtils.formatWeight(uiState.totalVolume, useLbs = false)
                )

                HorizontalDivider(color = BrushedSteel.copy(alpha = 0.5f))

                SummaryStatRow(
                    icon = null,
                    label = stringResource(R.string.workout_summary_sets_completed),
                    value = uiState.setsCompleted.toString()
                )

                HorizontalDivider(color = BrushedSteel.copy(alpha = 0.5f))

                SummaryStatRow(
                    icon = null,
                    label = stringResource(R.string.workout_summary_exercises),
                    value = uiState.exerciseCount.toString()
                )
            }
        }

        // PRs section
        if (uiState.prs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = GoldPR.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = stringResource(R.string.cd_pr_achievement),
                            tint = GoldPR,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.pr_new),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPR
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.prs.forEach { pr ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = pr.exerciseName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = pr.prLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldPR
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Back to Home button
        Button(
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = OnAccent
            )
        ) {
            Icon(
                Icons.Filled.Home,
                contentDescription = null,
                tint = OnAccent
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.workout_summary_back_home),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryStatRow(
    icon: ImageVector?,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = BrushedSteel
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Accent
        )
    }
}
