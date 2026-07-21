package com.cyanogen.dailyrecord.data

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class DailyRecord(val date: LocalDate, val count: Long)

class DailyRecordRepository(private val dao: DailyRecordDao) {
    private val writeMutex = Mutex()

    val records: Flow<Map<LocalDate, Long>> = dao.observeAll().map { rows ->
        rows.associate { LocalDate.parse(it.date) to it.count }
    }

    suspend fun increment(date: LocalDate) = writeMutex.withLock {
        val current = dao.get(date.toString())?.count ?: 0L
        if (current < Long.MAX_VALUE) setCountLocked(date, current + 1)
    }

    suspend fun decrement(date: LocalDate) = writeMutex.withLock {
        val current = dao.get(date.toString())?.count ?: 0L
        setCountLocked(date, (current - 1).coerceAtLeast(0))
    }

    suspend fun setCount(date: LocalDate, count: Long) = writeMutex.withLock {
        setCountLocked(date, count.coerceAtLeast(0))
    }

    private suspend fun setCountLocked(date: LocalDate, count: Long) {
        val existing = dao.get(date.toString())
        if (count == 0L) {
            if (existing != null) dao.delete(existing)
        } else {
            dao.upsert(
                DailyRecordEntity(
                    date = date.toString(),
                    count = count,
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }
    }
}
