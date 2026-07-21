package com.cyanogen.dailyrecord.domain

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordRulesTest {
    private val today = LocalDate.of(2026, 7, 22)

    @Test fun todayIsEditable() {
        assertTrue(RecordRules.isEditable(today, today))
    }

    @Test fun sixDaysAgoIsEditable() {
        assertTrue(RecordRules.isEditable(today.minusDays(6), today))
    }

    @Test fun sevenDaysAgoIsReadOnly() {
        assertFalse(RecordRules.isEditable(today.minusDays(7), today))
    }

    @Test fun futureIsReadOnly() {
        assertFalse(RecordRules.isEditable(today.plusDays(1), today))
    }

    @Test fun dateMathWorksAcrossYearBoundary() {
        val newYear = LocalDate.of(2027, 1, 2)
        assertTrue(RecordRules.isEditable(LocalDate.of(2026, 12, 27), newYear))
        assertFalse(RecordRules.isEditable(LocalDate.of(2026, 12, 26), newYear))
    }
}
