package com.kb9ut.pror.util

import kotlin.math.exp
import kotlin.math.pow

object OneRepMaxCalculator {

    fun brzycki1(w: Double, r: Int): Double =
        if (r == 1) w else w * (36.0 / (37.0 - r))

    fun brzycki2(w: Double, r: Int): Double =
        if (r == 1) w else w * (1.0278 - 0.0278 * r).pow(-1)

    fun epley(w: Double, r: Int): Double =
        if (r == 1) w else w * (1 + r / 30.0)

    fun lander(w: Double, r: Int): Double =
        if (r == 1) w else w * 100 / (101.3 - 2.67123 * r)

    fun lombardi(w: Double, r: Int): Double =
        if (r == 1) w else w * r.toDouble().pow(0.10)

    fun mayhew(w: Double, r: Int): Double =
        if (r == 1) w else w * 100 / (52.2 + 41.9 * exp(-0.055 * r))

    fun oconner(w: Double, r: Int): Double =
        if (r == 1) w else w * (1 + r / 40.0)

    fun wathan(w: Double, r: Int): Double =
        if (r == 1) w else w * 100 / (48.8 + 53.8 * exp(-0.075 * r))

    data class EstimationResult(
        val formulaResults: Map<String, Double>,
        val average: Double,
        val median: Double,
        val min: Double,
        val max: Double
    )

    fun calculateAll(weight: Double, reps: Int): EstimationResult? {
        if (reps <= 0 || reps > 30 || weight <= 0) return null

        val results = mapOf(
            "Brzycki 1" to brzycki1(weight, reps),
            "Brzycki 2" to brzycki2(weight, reps),
            "Epley" to epley(weight, reps),
            "Lander" to lander(weight, reps),
            "Lombardi" to lombardi(weight, reps),
            "Mayhew" to mayhew(weight, reps),
            "O'Conner" to oconner(weight, reps),
            "Wathan" to wathan(weight, reps)
        )

        val values = results.values.sorted()
        val avg = values.average()
        val median = if (values.size % 2 == 0) {
            (values[values.size / 2 - 1] + values[values.size / 2]) / 2.0
        } else {
            values[values.size / 2]
        }

        return EstimationResult(
            formulaResults = results,
            average = avg,
            median = median,
            min = values.first(),
            max = values.last()
        )
    }
}
