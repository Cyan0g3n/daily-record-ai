package com.cyanogen.dailyrecord

import android.app.Application
import com.cyanogen.dailyrecord.data.AppDatabase
import com.cyanogen.dailyrecord.data.DailyRecordRepository

class DailyRecordApplication : Application() {
    val repository: DailyRecordRepository by lazy {
        DailyRecordRepository(AppDatabase.getInstance(this).dailyRecordDao())
    }
}
