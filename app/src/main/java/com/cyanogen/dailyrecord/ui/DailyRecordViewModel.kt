package com.cyanogen.dailyrecord.ui

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cyanogen.dailyrecord.data.DailyRecordRepository
import com.cyanogen.dailyrecord.domain.RecordRules
import com.cyanogen.dailyrecord.domain.Statistics
import com.cyanogen.dailyrecord.domain.StatisticsCalculator
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DailyRecordUiState(
    val today: LocalDate = LocalDate.now(),
    val records: Map<LocalDate, Long> = emptyMap(),
    val showPrivacyNotice: Boolean = false,
    val statistics: Statistics = StatisticsCalculator.calculate(emptyMap(), LocalDate.now()),
) {
    val todayCount: Long get() = records[today] ?: 0L
}

class DailyRecordViewModel(
    private val repository: DailyRecordRepository,
    private val preferences: SharedPreferences,
) : ViewModel() {
    private val today = MutableStateFlow(LocalDate.now())
    private val privacyNotice = MutableStateFlow(!preferences.getBoolean(PRIVACY_SEEN, false))

    val uiState: StateFlow<DailyRecordUiState> = combine(
        repository.records,
        today,
        privacyNotice,
    ) { records, currentToday, showNotice ->
        DailyRecordUiState(
            today = currentToday,
            records = records,
            showPrivacyNotice = showNotice,
            statistics = StatisticsCalculator.calculate(records, currentToday),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DailyRecordUiState(showPrivacyNotice = privacyNotice.value),
    )

    init {
        viewModelScope.launch {
            while (isActive) {
                refreshToday()
                val now = ZonedDateTime.now()
                val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
                delay(Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1_000))
            }
        }
    }

    fun refreshToday() {
        today.value = LocalDate.now()
    }

    fun incrementToday() {
        val date = today.value
        viewModelScope.launch { repository.increment(date) }
    }

    fun decrementToday() {
        val date = today.value
        viewModelScope.launch { repository.decrement(date) }
    }

    fun setCount(date: LocalDate, count: Long) {
        if (!RecordRules.isEditable(date, today.value)) return
        viewModelScope.launch { repository.setCount(date, count) }
    }

    fun dismissPrivacyNotice() {
        preferences.edit { putBoolean(PRIVACY_SEEN, true) }
        privacyNotice.value = false
    }

    companion object {
        private const val PRIVACY_SEEN = "privacy_notice_seen"

        fun factory(
            repository: DailyRecordRepository,
            preferences: SharedPreferences,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DailyRecordViewModel(repository, preferences) as T
        }
    }
}
