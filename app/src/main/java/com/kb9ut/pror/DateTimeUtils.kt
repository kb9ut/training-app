package com.kb9ut.pror.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object DateTimeUtils {

    fun startOfDay(date: LocalDate = LocalDate.now(), zoneId: ZoneId = ZoneId.systemDefault()): Long =
        date.atStartOfDay(zoneId).toInstant().toEpochMilli()

    fun endOfDay(date: LocalDate = LocalDate.now(), zoneId: ZoneId = ZoneId.systemDefault()): Long =
        date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

    fun millisToLocalDate(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()

    fun millisToLocalDateTime(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDateTime()

    fun formatDate(millis: Long, locale: Locale = Locale.getDefault()): String {
        val dateTime = millisToLocalDateTime(millis)
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        return dateTime.format(formatter)
    }

    fun formatTime(millis: Long, locale: Locale = Locale.getDefault()): String {
        val dateTime = millisToLocalDateTime(millis)
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
        return dateTime.format(formatter)
    }

    fun formatDateTime(millis: Long, locale: Locale = Locale.getDefault()): String {
        val dateTime = millisToLocalDateTime(millis)
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(locale)
        return dateTime.format(formatter)
    }
}
