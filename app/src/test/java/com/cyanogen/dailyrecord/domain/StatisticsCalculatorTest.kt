package com.cyanogen.dailyrecord.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticsCalculatorTest {
    @Test fun calculatesWeekMonthAndThirtyDayValues() {
        val today = LocalDate.of(2026, 7, 22) // Wednesday
        val records = mapOf(
            LocalDate.of(2026, 7, 20) to 2L,
            LocalDate.of(2026, 7, 21) to 3L,
            today to 1L,
            LocalDate.of(2026, 7, 1) to 4L,
            LocalDate.of(2026, 6, 30) to 8L,
        )

        val result = StatisticsCalculator.calculate(records, today)

        assertEquals(18L, result.allTimeTotal)
        assertEquals(6L, result.weekTotal)
        assertEquals(10L, result.monthTotal)
        assertEquals(18.0 / 30.0, result.thirtyDayAverage, 0.0001)
        assertEquals(8L, result.highestDaily)
        assertEquals(30, result.lastThirtyDays.size)
        assertTrue(result.hasData)
    }

    @Test fun emptyRecordsReturnZeroesAndEmptyState() {
        val result = StatisticsCalculator.calculate(emptyMap(), LocalDate.of(2026, 7, 22))

        assertEquals(0L, result.allTimeTotal)
        assertEquals(0L, result.weekTotal)
        assertEquals(0L, result.monthTotal)
        assertEquals(0.0, result.thirtyDayAverage, 0.0)
        assertEquals(0L, result.highestDaily)
        assertFalse(result.hasData)
    }

    @Test fun weekBeginsOnMondayAndEndsOnSunday() {
        val sunday = LocalDate.of(2026, 7, 26)
        val records = mapOf(
            LocalDate.of(2026, 7, 19) to 9L,
            LocalDate.of(2026, 7, 20) to 2L,
            sunday to 4L,
            LocalDate.of(2026, 7, 27) to 7L,
        )

        assertEquals(6L, StatisticsCalculator.calculate(records, sunday).weekTotal)
    }

    @Test fun leapDayIsIncludedInRollingWindow() {
        val today = LocalDate.of(2028, 3, 1)
        val records = mapOf(LocalDate.of(2028, 2, 29) to 5L)

        val result = StatisticsCalculator.calculate(records, today)

        assertEquals(5L, result.highestDaily)
        assertTrue(result.lastThirtyDays.any { it.first == LocalDate.of(2028, 2, 29) && it.second == 5L })
    }
}
