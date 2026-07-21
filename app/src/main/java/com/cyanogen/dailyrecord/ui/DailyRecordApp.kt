package com.cyanogen.dailyrecord.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cyanogen.dailyrecord.domain.RecordRules
import com.cyanogen.dailyrecord.domain.Statistics
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.floor

private enum class Destination(val route: String, val label: String) {
    Today("today", "今日"), Calendar("calendar", "日历"), Statistics("statistics", "统计")
}

private val chineseLocale = Locale.SIMPLIFIED_CHINESE
private val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", chineseLocale)

@Composable
fun DailyRecordApp(viewModel: DailyRecordViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.showPrivacyNotice) {
        if (state.showPrivacyNotice) {
            snackbarHostState.showSnackbar("所有数据仅保存在本机")
            viewModel.dismissPrivacyNotice()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                Destination.entries.forEach { destination ->
                    val selected = backStack?.destination?.route == destination.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (destination) {
                                    Destination.Today -> Icons.Default.Home
                                    Destination.Calendar -> Icons.Default.CalendarMonth
                                    Destination.Statistics -> Icons.Default.BarChart
                                },
                                contentDescription = destination.label,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Today.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destination.Today.route) {
                TodayScreen(
                    today = state.today,
                    count = state.todayCount,
                    onIncrement = viewModel::incrementToday,
                    onDecrement = viewModel::decrementToday,
                )
            }
            composable(Destination.Calendar.route) {
                CalendarScreen(
                    today = state.today,
                    records = state.records,
                    onSetCount = viewModel::setCount,
                )
            }
            composable(Destination.Statistics.route) {
                StatisticsScreen(state.statistics)
            }
        }
    }
}

@Composable
private fun TodayScreen(
    today: LocalDate,
    count: Long,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("撸管记录", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(today.format(dateFormatter), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(.45f))
        DeerDecoration(Modifier.width(180.dp).height(112.dp))
        Spacer(Modifier.height(14.dp))
        Text("今日次数", style = MaterialTheme.typography.titleMedium)
        Text(
            text = count.toString(),
            fontSize = 88.sp,
            lineHeight = 96.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { contentDescription = "今日次数 $count" },
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onIncrement()
            },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp),
        ) {
            Text("撸一次", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onDecrement,
            enabled = count > 0,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(Icons.Default.Remove, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("撤销一次")
        }
        Spacer(Modifier.weight(.55f))
        Text(
            "记录仅保存在这台设备上",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeerDecoration(modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.secondary
    val detailColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val stroke = size.minDimension * .035f

        // Body, neck and head in side profile.
        drawOval(
            color = lineColor.copy(alpha = .13f),
            topLeft = Offset(size.width * .12f, size.height * .40f),
            size = androidx.compose.ui.geometry.Size(size.width * .50f, size.height * .32f),
        )
        drawOval(
            color = lineColor,
            topLeft = Offset(size.width * .12f, size.height * .40f),
            size = androidx.compose.ui.geometry.Size(size.width * .50f, size.height * .32f),
            style = Stroke(stroke),
        )
        drawLine(lineColor, Offset(size.width * .56f, size.height * .48f), Offset(size.width * .72f, size.height * .23f), stroke, StrokeCap.Round)
        drawLine(lineColor, Offset(size.width * .61f, size.height * .63f), Offset(size.width * .78f, size.height * .34f), stroke, StrokeCap.Round)
        drawOval(
            color = lineColor,
            topLeft = Offset(size.width * .70f, size.height * .19f),
            size = androidx.compose.ui.geometry.Size(size.width * .19f, size.height * .17f),
            style = Stroke(stroke),
        )
        drawLine(lineColor, Offset(size.width * .87f, size.height * .25f), Offset(size.width * .97f, size.height * .29f), stroke, StrokeCap.Round)
        drawLine(lineColor, Offset(size.width * .97f, size.height * .29f), Offset(size.width * .87f, size.height * .33f), stroke, StrokeCap.Round)

        // Ear, eye and branching antler.
        drawLine(lineColor, Offset(size.width * .74f, size.height * .22f), Offset(size.width * .66f, size.height * .11f), stroke, StrokeCap.Round)
        drawLine(lineColor, Offset(size.width * .66f, size.height * .11f), Offset(size.width * .79f, size.height * .18f), stroke, StrokeCap.Round)
        drawCircle(detailColor, radius = stroke * .62f, center = Offset(size.width * .83f, size.height * .26f))
        drawLine(lineColor, Offset(size.width * .75f, size.height * .20f), Offset(size.width * .68f, size.height * .04f), stroke, StrokeCap.Round)
        drawLine(lineColor, Offset(size.width * .70f, size.height * .10f), Offset(size.width * .61f, size.height * .04f), stroke, StrokeCap.Round)
        drawLine(lineColor, Offset(size.width * .71f, size.height * .11f), Offset(size.width * .76f, size.height * .01f), stroke, StrokeCap.Round)

        // Tail and four slender legs.
        drawLine(lineColor, Offset(size.width * .14f, size.height * .48f), Offset(size.width * .04f, size.height * .37f), stroke, StrokeCap.Round)
        drawLine(lineColor, Offset(size.width * .04f, size.height * .37f), Offset(size.width * .08f, size.height * .53f), stroke, StrokeCap.Round)
        listOf(.24f, .35f, .51f, .59f).forEachIndexed { index, legX ->
            val hoofX = legX + if (index % 2 == 0) -.035f else .035f
            drawLine(lineColor, Offset(size.width * legX, size.height * .68f), Offset(size.width * legX, size.height * .86f), stroke, StrokeCap.Round)
            drawLine(lineColor, Offset(size.width * legX, size.height * .86f), Offset(size.width * hoofX, size.height * .97f), stroke, StrokeCap.Round)
            drawLine(lineColor, Offset(size.width * hoofX, size.height * .97f), Offset(size.width * (hoofX + .035f), size.height * .97f), stroke, StrokeCap.Round)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarScreen(
    today: LocalDate,
    records: Map<LocalDate, Long>,
    onSetCount: (LocalDate, Long) -> Unit,
) {
    var shownMonthText by rememberSaveable { mutableStateOf(YearMonth.from(today).toString()) }
    val shownMonth = YearMonth.parse(shownMonthText)
    var selectedDateText by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedDate = selectedDateText?.let(LocalDate::parse)

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 20.dp)) {
        Text("日历", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { shownMonthText = shownMonth.minusMonths(1).toString() }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
            }
            Text(
                "${shownMonth.year}年${shownMonth.monthValue}月",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { shownMonthText = shownMonth.plusMonths(1).toString() }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
            }
            TextButton(onClick = { shownMonthText = YearMonth.from(today).toString() }) { Text("今天") }
        }
        Spacer(Modifier.height(8.dp))
        CalendarGrid(
            month = shownMonth,
            today = today,
            records = records,
            onSelectDate = { selectedDateText = it.toString() },
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }

    if (selectedDate != null) {
        EditRecordSheet(
            date = selectedDate,
            today = today,
            currentCount = records[selectedDate] ?: 0L,
            onDismiss = { selectedDateText = null },
            onSave = {
                onSetCount(selectedDate, it)
                selectedDateText = null
            },
        )
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    today: LocalDate,
    records: Map<LocalDate, Long>,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
    val leadingEmpty = month.atDay(1).dayOfWeek.value - 1
    val totalCells = ((leadingEmpty + month.lengthOfMonth() + 6) / 7) * 7

    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            weekdays.forEach { day ->
                Text(
                    day,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                )
            }
        }
        Column(Modifier.fillMaxWidth().weight(1f)) {
            repeat(totalCells / 7) { week ->
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    repeat(7) { weekday ->
                        val index = week * 7 + weekday
                        val dayNumber = index - leadingEmpty + 1
                        if (dayNumber in 1..month.lengthOfMonth()) {
                            val date = month.atDay(dayNumber)
                            CalendarDay(
                                date = date,
                                count = records[date] ?: 0L,
                                isToday = date == today,
                                isFuture = date.isAfter(today),
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onSelectDate(date) },
                            )
                        } else {
                            Spacer(Modifier.weight(1f).fillMaxHeight())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    count: Long,
    isToday: Boolean,
    isFuture: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val background = if (isToday) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val alphaColor = if (isFuture) MaterialTheme.colorScheme.onSurface.copy(alpha = .35f)
    else MaterialTheme.colorScheme.onSurface
    Column(
        modifier = modifier
            .padding(3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(date.dayOfMonth.toString(), color = alphaColor, fontWeight = if (isToday) FontWeight.Bold else null)
        Spacer(Modifier.height(5.dp))
        if (count > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(.88f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "$count 次",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFuture) alphaColor else MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        } else {
            Text(
                "—",
                style = MaterialTheme.typography.labelMedium,
                color = alphaColor.copy(alpha = .45f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRecordSheet(
    date: LocalDate,
    today: LocalDate,
    currentCount: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
) {
    val editable = RecordRules.isEditable(date, today)
    var value by remember(date, currentCount) { mutableStateOf(currentCount.toString()) }
    val parsed = value.toLongOrNull()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(date.format(dateFormatter), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(20.dp))
            if (editable) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = { value = ((parsed ?: 0L) - 1).coerceAtLeast(0).toString() },
                        enabled = (parsed ?: 0L) > 0,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(52.dp),
                    ) { Text("−", fontSize = 24.sp) }
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all(Char::isDigit)) value = input
                        },
                        label = { Text("次数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = parsed == null,
                        modifier = Modifier.width(150.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    FilledTonalButton(
                        onClick = { if (parsed != null && parsed < Long.MAX_VALUE) value = (parsed + 1).toString() },
                        enabled = parsed != null && parsed < Long.MAX_VALUE,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(52.dp),
                    ) { Text("+", fontSize = 24.sp) }
                }
                if (parsed == null) {
                    Text("请输入有效的非负整数", color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { parsed?.let(onSave) },
                    enabled = parsed != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) { Text("保存") }
            } else {
                Text(
                    "记录次数：$currentCount",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    RecordRules.editMessage(date, today).orEmpty(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("关闭") }
            }
        }
    }
}

@Composable
private fun StatisticsScreen(statistics: Statistics) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("统计", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            StatCard("累计总次数", statistics.allTimeTotal.toString(), Modifier.fillMaxWidth())
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("本周", statistics.weekTotal.toString(), Modifier.weight(1f))
                StatCard("本月", statistics.monthTotal.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("近30天日均", String.format(chineseLocale, "%.1f", statistics.thirtyDayAverage), Modifier.weight(1f))
                StatCard("单日最高", statistics.highestDaily.toString(), Modifier.weight(1f))
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("近 30 天趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    if (statistics.hasData) {
                        ThirtyDayChart(statistics.lastThirtyDays)
                    } else {
                        Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "还没有记录\n完成记录后，这里会显示趋势",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ThirtyDayChart(data: List<Pair<LocalDate, Long>>) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxValue = (data.maxOfOrNull { it.second } ?: 1L).coerceAtLeast(1L)
    val barColor = MaterialTheme.colorScheme.secondary
    val selectedColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outlineVariant

    Column {
        Box(Modifier.fillMaxWidth().height(220.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val index = floor(offset.x / size.width * data.size).toInt()
                                .coerceIn(data.indices)
                            selectedIndex = index
                        }
                    }
                    .semantics { contentDescription = "近30天次数柱状趋势图" },
            ) {
                drawLine(axisColor, Offset(0f, size.height - 1f), Offset(size.width, size.height - 1f), strokeWidth = 2f)
                val slot = size.width / data.size
                val width = slot * .62f
                data.forEachIndexed { index, (_, count) ->
                    if (count > 0) {
                        val height = size.height * .88f * (count.toDouble() / maxValue.toDouble()).toFloat()
                        drawRoundRect(
                            color = if (selectedIndex == index) selectedColor else barColor,
                            topLeft = Offset(index * slot + (slot - width) / 2f, size.height - height),
                            size = androidx.compose.ui.geometry.Size(width, height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(width / 2f, width / 2f),
                        )
                    } else if (selectedIndex == index) {
                        drawCircle(selectedColor, radius = width / 3f, center = Offset(index * slot + slot / 2f, size.height - width / 2f), style = Stroke(2f))
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        val selected = selectedIndex?.let(data::get)
        Text(
            selected?.let { (date, count) -> "${date.monthValue}月${date.dayOfMonth}日 · $count 次" }
                ?: "点击柱子查看当天数据",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}
