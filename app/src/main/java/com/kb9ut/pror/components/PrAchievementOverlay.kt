package com.kb9ut.pror.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.screen.activeworkout.PrAchievement
import com.kb9ut.pror.ui.screen.activeworkout.PrType
import com.kb9ut.pror.ui.theme.GoldPR
import com.kb9ut.pror.ui.theme.GoldPRContainer
import kotlinx.coroutines.delay

@Composable
fun PrAchievementOverlay(
    achievement: PrAchievement?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(achievement) {
        if (achievement != null) {
            visibleState.targetState = true
            delay(3000)
            visibleState.targetState = false
            delay(500) // Wait for exit animation
            onDismiss()
        } else {
            visibleState.targetState = false
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(400)
        ) + fadeOut(animationSpec = tween(300)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(400)
        ),
        modifier = modifier
    ) {
        if (achievement != null) {
            PrBanner(achievement = achievement)
        }
    }
}

@Composable
private fun PrBanner(achievement: PrAchievement) {
    val shape = RoundedCornerShape(16.dp)
    val prTypeText = when (achievement.type) {
        PrType.WEIGHT -> stringResource(R.string.pr_weight_record)
        PrType.REPS -> stringResource(R.string.pr_reps_record)
        PrType.ESTIMATED_1RM -> stringResource(R.string.pr_1rm_record)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = GoldPR.copy(alpha = 0.4f),
                spotColor = GoldPR.copy(alpha = 0.4f)
            )
            .clip(shape)
            .border(width = 2.5.dp, color = GoldPR, shape = shape)
            .background(GoldPRContainer.copy(alpha = 0.95f))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.pr_new),
                style = MaterialTheme.typography.headlineSmall.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = GoldPR.copy(alpha = 0.6f),
                        blurRadius = 16f
                    )
                ),
                fontWeight = FontWeight.ExtraBold,
                color = GoldPR,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$prTypeText: ${achievement.value}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = GoldPR.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}
