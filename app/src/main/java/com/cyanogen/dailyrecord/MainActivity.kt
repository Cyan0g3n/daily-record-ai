package com.cyanogen.dailyrecord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.cyanogen.dailyrecord.ui.DailyRecordApp
import com.cyanogen.dailyrecord.ui.DailyRecordViewModel
import com.cyanogen.dailyrecord.ui.theme.DailyRecordTheme

class MainActivity : ComponentActivity() {
    private val viewModel: DailyRecordViewModel by viewModels {
        val application = application as DailyRecordApplication
        DailyRecordViewModel.factory(
            repository = application.repository,
            preferences = getSharedPreferences("daily_record_preferences", MODE_PRIVATE),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyRecordTheme { DailyRecordApp(viewModel) }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshToday()
    }
}
