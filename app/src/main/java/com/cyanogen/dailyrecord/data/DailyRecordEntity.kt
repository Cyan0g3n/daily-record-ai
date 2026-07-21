package com.cyanogen.dailyrecord.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecordEntity(
    @PrimaryKey val date: String,
    val count: Long,
    val updatedAt: Long,
)
