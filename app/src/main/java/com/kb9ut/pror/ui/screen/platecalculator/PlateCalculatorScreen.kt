package com.kb9ut.pror.ui.screen.platecalculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.components.GlassCard
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.ui.theme.OnAccent
import androidx.compose.material3.OutlinedTextFieldDefaults

private data class PlateSpec(
    val weight: Double,
    val color: Color,
    val height: Dp
)

private val plateSpecs = listOf(
    PlateSpec(25.0, Color(0xFFC62828), 120.dp),   // Red
    PlateSpec(20.0, Color(0xFF1565C0), 112.dp),   // Blue
    PlateSpec(15.0, Color(0xFFF9A825), 104.dp),    // Yellow
    PlateSpec(10.0, Color(0xFF2E7D32), 96.dp),     // Green
    PlateSpec(5.0, Color(0xFFF5F5F5), 86.dp),     // White
    PlateSpec(2.5, Color(0xFFB71C1C), 68.dp),      // Dark Red
    PlateSpec(1.25, Color(0xFFFBC02D), 56.dp),     // Yellow
    PlateSpec(0.5, Color(0xFF388E3C), 44.dp)       // Green
)

private val barWeightOptions = listOf(10.0, 15.0, 20.0, 25.0)

private fun calculatePlates(targetWeight: Double, barWeight: Double): List<Double> {
    val availablePlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25, 0.5)
    var remaining = (targetWeight - barWeight) / 2.0
    if (remaining <= 0) return emptyList()
    val plates = mutableListOf<Double>()
    for (plate in availablePlates) {
        while (remaining >= plate - 0.001) {
            plates.add(plate)
            remaining -= plate
        }
    }
    return plates
}

private fun formatPlateWeight(weight: Double): String {
    return if (weight == weight.toLong().toDouble()) {
        weight.toLong().toString()
    } else {
        weight.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlateCalculatorScreen(
    navController: NavController
) {
    var targetWeightText by remember { mutableStateOf("60") }
    var barWeight by remember { mutableDoubleStateOf(20.0) }

    val targetWeight = targetWeightText.toDoubleOrNull() ?: 0.0
    val plates = if (targetWeight > barWeight) {
        calculatePlates(targetWeight, barWeight)
    } else {
        emptyList()
    }
    val achievedWeight = barWeight + plates.sum() * 2.0
    val isExact = targetWeight > 0 && kotlin.math.abs(achievedWeight - targetWeight) < 0.01

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.plate_calculator)) },
            windowInsets = WindowInsets(0),
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Target weight input
            item(key = "target_weight") {
                OutlinedTextField(
                    value = targetWeightText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            targetWeightText = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.plate_calculator_target_weight)) },
                    suffix = { Text(stringResource(R.string.kg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        cursorColor = Accent,
                        focusedLabelColor = Accent
                    )
                )
            }

            // Bar weight selector
            item(key = "bar_weight") {
                Column {
                    Text(
                        text = stringResource(R.string.plate_calculator_bar_weight).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrushedSteel,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        barWeightOptions.forEach { weight ->
                            FilterChip(
                                selected = barWeight == weight,
                                onClick = { barWeight = weight },
                                label = {
                                    Text("${formatPlateWeight(weight)}kg")
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Accent,
                                    selectedLabelColor = OnAccent
                                )
                            )
                        }
                    }
                }
            }

            // Visual barbell representation
            item(key = "barbell_visual") {
                if (plates.isNotEmpty()) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.plate_calculator_per_side).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrushedSteel,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            BarbellVisual(plates = plates)
                        }
                    }
                }
            }

            // Status message or plate list
            item(key = "plate_info") {
                if (targetWeight <= 0 || targetWeightText.isEmpty()) {
                    // No input yet - show nothing
                } else if (targetWeight <= barWeight) {
                    Text(
                        text = stringResource(R.string.plate_calculator_no_plates),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!isExact) {
                            Text(
                                text = stringResource(R.string.plate_calculator_unreachable),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "${formatPlateWeight(achievedWeight)}kg",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Group plates by weight for display
                        val grouped = plates.groupBy { it }
                            .map { (weight, list) -> list.size to weight }

                        grouped.forEach { (count, weight) ->
                            val spec = plateSpecs.first { it.weight == weight }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Color swatch
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(spec.color)
                                        .then(
                                            if (spec.color == Color(0xFFEEEEEE)) {
                                                Modifier.border(
                                                    1.dp,
                                                    Color(0xFF757575),
                                                    RoundedCornerShape(4.dp)
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                )
                                Text(
                                    text = "${count}x ${formatPlateWeight(weight)}kg",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarbellVisual(plates: List<Double>) {
    val barColor = Color(0xFF757575)
    val barSleeveColor = Color(0xFF9E9E9E)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Bar collar (left side)
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(16.dp)
                .background(barSleeveColor, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
        )

        // Bar shaft (left)
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(8.dp)
                .background(barColor)
        )

        // Plates (heaviest closest to center, so reverse for visual)
        plates.reversed().forEach { plateWeight ->
            val spec = plateSpecs.first { it.weight == plateWeight }
            val plateWidth = when {
                plateWeight >= 10.0 -> 16.dp
                plateWeight >= 2.5 -> 12.dp
                else -> 8.dp
            }
            val plateHeight = (spec.height.value * 0.5f).dp

            Box(
                modifier = Modifier
                    .width(plateWidth)
                    .height(plateHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(spec.color)
                    .then(
                        if (spec.color == Color(0xFFEEEEEE)) {
                            Modifier.border(1.dp, Color(0xFF757575), RoundedCornerShape(2.dp))
                        } else {
                            Modifier
                        }
                    )
            )
            Spacer(modifier = Modifier.width(1.dp))
        }

        // Bar shaft (center to right)
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(8.dp)
                .background(barColor)
        )

        // Mirror plates on right side
        plates.forEach { plateWeight ->
            val spec = plateSpecs.first { it.weight == plateWeight }
            val plateWidth = when {
                plateWeight >= 10.0 -> 16.dp
                plateWeight >= 2.5 -> 12.dp
                else -> 8.dp
            }
            val plateHeight = (spec.height.value * 0.5f).dp

            Spacer(modifier = Modifier.width(1.dp))
            Box(
                modifier = Modifier
                    .width(plateWidth)
                    .height(plateHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(spec.color)
                    .then(
                        if (spec.color == Color(0xFFEEEEEE)) {
                            Modifier.border(1.dp, Color(0xFF757575), RoundedCornerShape(2.dp))
                        } else {
                            Modifier
                        }
                    )
            )
        }

        // Bar shaft (right)
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(8.dp)
                .background(barColor)
        )

        // Bar collar (right side)
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(16.dp)
                .background(barSleeveColor, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
        )
    }
}
