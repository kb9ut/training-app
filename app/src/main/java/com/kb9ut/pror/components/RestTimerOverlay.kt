package com.kb9ut.pror.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kb9ut.pror.R
import com.kb9ut.pror.service.RestTimerState
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.DarkSteel
import com.kb9ut.pror.ui.theme.SurfaceBright
import com.kb9ut.pror.ui.theme.Error as ErrorRed

@Composable
fun RestTimerOverlay(
    timerState: RestTimerState,
    onAdjustTime: (Int) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = timerState.isRunning || timerState.remainingSeconds > 0,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Surface(
            color = SurfaceBright,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = DarkSteel,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                .animateContentSize()
                .clickable { isExpanded = !isExpanded }
        ) {
            if (isExpanded) {
                ExpandedTimerContent(
                    timerState = timerState,
                    onAdjustTime = onAdjustTime,
                    onSkip = onSkip
                )
            } else {
                CollapsedTimerContent(
                    timerState = timerState
                )
            }
        }
    }
}

@Composable
private fun ExpandedTimerContent(
    timerState: RestTimerState,
    onAdjustTime: (Int) -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.rest_timer),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(contentAlignment = Alignment.Center) {
            val progress = if (timerState.totalSeconds > 0) {
                timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()
            } else {
                0f
            }

            val isUrgent = timerState.remainingSeconds < 10
            val timerColor = if (isUrgent) ErrorRed else Accent

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(100.dp),
                color = timerColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 8.dp
            )

            Text(
                text = formatTimerDisplay(timerState.remainingSeconds),
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 45.sp),
                fontWeight = FontWeight.Bold,
                color = timerColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { onAdjustTime(-60) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("-1m")
            }
            FilledTonalButton(
                onClick = { onAdjustTime(-30) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("-30s")
            }
            FilledTonalButton(
                onClick = { onAdjustTime(30) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text("+30s")
            }
            FilledTonalButton(
                onClick = { onAdjustTime(60) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text("+1m")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FilledTonalButton(
            onClick = onSkip,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(stringResource(R.string.rest_timer_skip))
        }
    }
}

@Composable
private fun CollapsedTimerContent(
    timerState: RestTimerState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val progress = if (timerState.totalSeconds > 0) {
            timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()
        } else {
            0f
        }

        val isUrgent = timerState.remainingSeconds < 10
        val timerColor = if (isUrgent) ErrorRed else Accent

        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(24.dp),
            color = timerColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 3.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(R.string.rest_timer),
            style = MaterialTheme.typography.labelMedium,
            color = timerColor
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = formatTimerDisplay(timerState.remainingSeconds),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = timerColor
        )
    }
}

private fun formatTimerDisplay(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(minutes, secs)
}
