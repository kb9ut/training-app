package com.kb9ut.pror.util

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {

    fun formatWeight(weightKg: Double, useLbs: Boolean, locale: Locale = Locale.getDefault()): String {
        val value = if (useLbs) kgToLbs(weightKg) else weightKg
        val unit = if (useLbs) "lbs" else "kg"
        val formatter = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
        return "${formatter.format(value)} $unit"
    }

    /** Format weight value only (no unit), up to 2 decimal places, trailing zeros removed */
    fun formatWeightValue(weight: Double): String {
        val formatted = "%.2f".format(weight)
        return formatted.trimEnd('0').trimEnd('.')
    }

    fun kgToLbs(kg: Double): Double = kg * 2.20462

    fun lbsToKg(lbs: Double): Double = lbs / 2.20462

    fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%d:%02d", minutes, secs)
        }
    }

    fun formatDurationShort(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    /**
     * Parse a tempo string "E-P-C-T" into a list of 4 ints.
     * Returns null if the format is invalid.
     */
    fun parseTempo(tempo: String?): List<Int>? {
        if (tempo.isNullOrBlank()) return null
        val parts = tempo.split("-")
        if (parts.size != 4) return null
        return parts.mapNotNull { it.toIntOrNull() }.takeIf { it.size == 4 }
    }

    /**
     * Calculate Time Under Tension per rep from a tempo string.
     */
    fun tempoTutPerRep(tempo: String?): Int? {
        val parts = parseTempo(tempo) ?: return null
        return parts.sum()
    }

    /**
     * Calculate total TUT for a set (TUT per rep * reps).
     */
    fun totalTut(tempo: String?, reps: Int?): Int? {
        val tutPerRep = tempoTutPerRep(tempo) ?: return null
        val r = reps ?: return null
        return tutPerRep * r
    }

    /**
     * Format TUT as seconds string, e.g. "36s".
     */
    fun formatTut(tut: Int?): String {
        return tut?.let { "${it}s" } ?: "-"
    }
}
