package com.cyanogen.dailyrecord.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_records ORDER BY date ASC")
    fun observeAll(): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    suspend fun get(date: String): DailyRecordEntity?

    @Upsert
    suspend fun upsert(record: DailyRecordEntity)

    @Delete
    suspend fun delete(record: DailyRecordEntity)
}
