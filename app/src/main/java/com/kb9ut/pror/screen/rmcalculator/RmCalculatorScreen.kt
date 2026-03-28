package com.kb9ut.pror.ui.screen.rmcalculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.components.GlassCard
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.ui.theme.GlassBorder
import com.kb9ut.pror.ui.theme.SurfaceDim
import com.kb9ut.pror.util.OneRepMaxCalculator
import androidx.compose.ui.unit.sp

private data class NrmCoefficient(
    val rm: Int,
    val coefficient: Double
)

private val nrmCoefficients = listOf(
    NrmCoefficient(1, 1.00),
    NrmCoefficient(2, 0.95),
    NrmCoefficient(3, 0.93),
    NrmCoefficient(4, 0.90),
    NrmCoefficient(5, 0.87),
    NrmCoefficient(6, 0.85),
    NrmCoefficient(7, 0.825),
    NrmCoefficient(8, 0.80),
    NrmCoefficient(9, 0.77),
    NrmCoefficient(10, 0.75),
    NrmCoefficient(12, 0.72),
    NrmCoefficient(15, 0.68)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RmCalculatorScreen(
    navController: NavController
) {
    var weightText by remember { mutableStateOf("") }
    var repsText by remember { mutableStateOf("") }

    val weight = weightText.toDoubleOrNull()
    val reps = repsText.toIntOrNull()
    val result = if (weight != null && reps != null) {
        OneRepMaxCalculator.calculateAll(weight, reps)
    } else {
        null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.rm_calculator),
                    fontWeight = FontWeight.Bold
                )
            },
            windowInsets = WindowInsets(0),
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input fields
            item(key = "inputs") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text(stringResource(R.string.weight) + " (" + stringResource(R.string.kg) + ")") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            cursorColor = Accent,
                            focusedLabelColor = Accent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { repsText = it },
                        label = { Text(stringResource(R.string.reps)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            cursorColor = Accent,
                            focusedLabelColor = Accent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (result != null) {
                // Section 1: Formula Results
                item(key = "formula_results") {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // Header row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.rm_calculator_formula).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrushedSteel,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = stringResource(R.string.rm_calculator_estimated_1rm).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrushedSteel,
                                    letterSpacing = 1.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Formula rows
                            result.formulaResults.forEach { (name, value) ->
                                FormulaRow(
                                    name = name,
                                    value = value,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 0.5.dp,
                                color = GlassBorder
                            )

                            // Statistics rows
                            FormulaRow(
                                name = stringResource(R.string.rm_calculator_median),
                                value = result.median,
                                color = Accent,
                                bold = true
                            )
                            FormulaRow(
                                name = stringResource(R.string.rm_calculator_average),
                                value = result.average,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            FormulaRow(
                                name = stringResource(R.string.rm_calculator_min),
                                value = result.min,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FormulaRow(
                                name = stringResource(R.string.rm_calculator_max),
                                value = result.max,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Section 2: nRM Conversion Table
                item(key = "nrm_table") {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.rm_calculator_nrm_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Header row
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.rm_calculator_rm).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrushedSteel,
                                    letterSpacing = 1.5.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = stringResource(R.string.rm_calculator_coefficient).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrushedSteel,
                                    letterSpacing = 1.5.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = stringResource(R.string.rm_calculator_est_weight).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrushedSteel,
                                    letterSpacing = 1.5.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            nrmCoefficients.forEachIndexed { index, entry ->
                                val estWeight = result.median * entry.coefficient
                                val isOneRm = entry.rm == 1
                                NrmRow(
                                    rm = entry.rm,
                                    coefficient = entry.coefficient,
                                    estWeight = estWeight,
                                    highlight = isOneRm,
                                    alternateBackground = index % 2 == 0
                                )
                            }
                        }
                    }
                }

                // Bottom spacer
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FormulaRow(
    name: String,
    value: Double,
    color: Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
        Text(
            text = "%.1f".format(value),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun NrmRow(
    rm: Int,
    coefficient: Double,
    estWeight: Double,
    highlight: Boolean,
    alternateBackground: Boolean = false
) {
    val textColor = if (highlight) Accent else MaterialTheme.colorScheme.onSurface
    val fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Normal
    val valueFontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Bold
    val rowBackground = if (alternateBackground) SurfaceDim else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground, RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Text(
            text = "${rm}RM",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = fontWeight,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "%.3f".format(coefficient),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = fontWeight,
            color = if (highlight) textColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "%.1f".format(estWeight),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = valueFontWeight,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}
