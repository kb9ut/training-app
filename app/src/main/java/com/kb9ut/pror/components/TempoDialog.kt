package com.kb9ut.pror.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kb9ut.pror.R
import com.kb9ut.pror.util.FormatUtils

@Composable
fun TempoDialog(
    currentTempo: String?,
    onTempoSet: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = FormatUtils.parseTempo(currentTempo) ?: listOf(0, 0, 0, 0)
    var eccentric by remember { mutableStateOf(if (parts[0] > 0) parts[0].toString() else "") }
    var pauseBottom by remember { mutableStateOf(if (parts[1] > 0) parts[1].toString() else "") }
    var concentric by remember { mutableStateOf(if (parts[2] > 0) parts[2].toString() else "") }
    var pauseTop by remember { mutableStateOf(if (parts[3] > 0) parts[3].toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tempo_set)) },
        text = {
            Column {
                Text(
                    text = "E - P - C - T",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TempoField(
                        value = eccentric,
                        onValueChange = { eccentric = it },
                        label = stringResource(R.string.tempo_eccentric),
                        modifier = Modifier.weight(1f)
                    )
                    Text("-", style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(16.dp), textAlign = TextAlign.Center)
                    TempoField(
                        value = pauseBottom,
                        onValueChange = { pauseBottom = it },
                        label = stringResource(R.string.tempo_pause_bottom),
                        modifier = Modifier.weight(1f)
                    )
                    Text("-", style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(16.dp), textAlign = TextAlign.Center)
                    TempoField(
                        value = concentric,
                        onValueChange = { concentric = it },
                        label = stringResource(R.string.tempo_concentric),
                        modifier = Modifier.weight(1f)
                    )
                    Text("-", style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(16.dp), textAlign = TextAlign.Center)
                    TempoField(
                        value = pauseTop,
                        onValueChange = { pauseTop = it },
                        label = stringResource(R.string.tempo_pause_top),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preview TUT
                val e = eccentric.toIntOrNull() ?: 0
                val p = pauseBottom.toIntOrNull() ?: 0
                val c = concentric.toIntOrNull() ?: 0
                val t = pauseTop.toIntOrNull() ?: 0
                val tutPerRep = e + p + c + t
                if (tutPerRep > 0) {
                    Text(
                        text = "${stringResource(R.string.tempo_tut)}: ${tutPerRep}s / rep",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val e = eccentric.toIntOrNull() ?: 0
                val p = pauseBottom.toIntOrNull() ?: 0
                val c = concentric.toIntOrNull() ?: 0
                val t = pauseTop.toIntOrNull() ?: 0
                if (e == 0 && p == 0 && c == 0 && t == 0) {
                    onTempoSet(null)
                } else {
                    onTempoSet("$e-$p-$c-$t")
                }
            }) {
                Text(stringResource(R.string.done))
            }
        },
        dismissButton = {
            Row {
                if (currentTempo != null) {
                    TextButton(onClick = { onTempoSet(null) }) {
                        Text(stringResource(R.string.tempo_clear))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}

@Composable
private fun TempoField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow single digit 0-9
            val filtered = newValue.filter { it.isDigit() }.take(1)
            onValueChange(filtered)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier,
        textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    )
}
