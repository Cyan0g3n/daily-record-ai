package com.cyanogen.dailyrecord.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object RecordRules {
    fun isEditable(date: LocalDate, today: LocalDate): Boolean =
        !date.isAfter(today) && !date.isBefore(today.minusDays(6))

    fun editMessage(date: LocalDate, today: LocalDate): String? = when {
        date.isAfter(today) -> "未来日期不可记录"
        date.isBefore(today.minusDays(6)) -> "仅支持修改最近 7 天的记录"
        else -> null
    }
}

data class Statistics(
    val allTimeTotal: Long,
    val weekTotal: Long,
    val monthTotal: Long,
    val thirtyDayAverage: Double,
    val highestDaily: Long,
    val lastThirtyDays: List<Pair<LocalDate, Long>>,
) {
    val hasData: Boolean get() = lastThirtyDays.any { it.second > 0 }
}

object StatisticsCalculator {
    fun calculate(records: Map<LocalDate, Long>, today: LocalDate): Statistics {
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)
        val monthStart = today.withDayOfMonth(1)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())
        val thirtyDayStart = today.minusDays(29)

        val weekTotal = records.filterKeys { !it.isBefore(weekStart) && !it.isAfter(weekEnd) }
            .values.sumSafely()
        val monthTotal = records.filterKeys { !it.isBefore(monthStart) && !it.isAfter(monthEnd) }
            .values.sumSafely()
        val lastThirtyDays = (0L..29L).map { offset ->
            val date = thirtyDayStart.plusDays(offset)
            date to (records[date] ?: 0L)
        }
        val total = lastThirtyDays.map { it.second }.sumSafely()

        return Statistics(
            allTimeTotal = records.values.sumSafely(),
            weekTotal = weekTotal,
            monthTotal = monthTotal,
            thirtyDayAverage = total.toDouble() / 30.0,
            highestDaily = lastThirtyDays.maxOfOrNull { it.second } ?: 0L,
            lastThirtyDays = lastThirtyDays,
        )
    }

    private fun Iterable<Long>.sumSafely(): Long = fold(0L) { total, value ->
        if (Long.MAX_VALUE - total < value) Long.MAX_VALUE else total + value
    }
}
