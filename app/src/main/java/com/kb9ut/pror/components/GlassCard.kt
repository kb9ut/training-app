package com.kb9ut.pror.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kb9ut.pror.ui.theme.MetalBackground
import com.kb9ut.pror.ui.theme.MetalBackgroundElevated
import com.kb9ut.pror.ui.theme.MetalBorder

/**
 * Industrial Neo-Brutalism card with dark metallic background
 * and steel border for depth on OLED dark backgrounds.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    elevated: Boolean = false,
    gradient: Boolean = false,
    borderColor: Color = MetalBorder,
    shape: Shape = RoundedCornerShape(cornerRadius),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val bgModifier = if (gradient) {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(MetalBackgroundElevated, MetalBackground)
            )
        )
    } else {
        val bgColor = if (elevated) MetalBackgroundElevated else MetalBackground
        Modifier.background(bgColor)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(bgModifier)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        content = content
    )
}

/**
 * Accent-bordered industrial card, used for primary actions / CTAs.
 * Uses Blood Orange accent with dark metallic background.
 */
@Composable
fun AccentGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(cornerRadius),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val accentGradient = Brush.linearGradient(
        colors = listOf(
            Color(0x33FF4500),  // 20% Blood Orange
            Color(0x1AFF4500)   // 10% Blood Orange
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(accentGradient)
            .border(
                width = 1.dp,
                color = Color(0x4DFF4500),  // 30% Blood Orange border
                shape = shape
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        content = content
    )
}
