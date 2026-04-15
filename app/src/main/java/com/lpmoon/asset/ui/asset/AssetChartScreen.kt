package com.lpmoon.asset.ui.asset

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntSize
import com.lpmoon.asset.domain.model.TimeDimension
import java.text.DecimalFormat
import kotlin.math.abs
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetChartScreen(
    getTotalAssetHistory: (TimeDimension) -> List<Pair<String, Double>>,
    onBack: () -> Unit
) {
    var selectedDimension by remember { mutableStateOf(TimeDimension.DAY) }
    val history by remember(selectedDimension) {
        derivedStateOf { getTotalAssetHistory(selectedDimension) }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 计算每个维度应该显示的默认数据点数
    val defaultWindowSize = when (selectedDimension) {
        TimeDimension.DAY -> 30   // 30日
        TimeDimension.WEEK -> 50  // 50周
        TimeDimension.MONTH -> 12 // 12个月
        TimeDimension.YEAR -> 10  // 10年
    }

    // 可显示的数据点数
    val totalPoints = history.size
    val windowSize = if (totalPoints > defaultWindowSize) defaultWindowSize else totalPoints

    // 滑动偏移状态（起始索引）
    var startIndex by remember(selectedDimension) {
        mutableStateOf(maxOf(0, totalPoints - windowSize))
    }

    // 拖动累计距离状态
    var dragAccumulated by remember(selectedDimension) { mutableStateOf(0f) }

    // 确保 startIndex 在有效范围内
    if (totalPoints > 0) {
        startIndex = startIndex.coerceIn(0, maxOf(0, totalPoints - windowSize))
    }

    // 当前窗口数据
    val windowData = if (totalPoints > 0 && windowSize > 0) {
        history.subList(startIndex, startIndex + windowSize)
    } else {
        emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("总资产历史趋势") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 时间维度选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeDimension.entries.forEach { dimension ->
                    FilterChip(
                        selected = selectedDimension == dimension,
                        onClick = {
                            selectedDimension = dimension
                        },
                        label = { Text(getDimensionDisplayName(dimension)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center

                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {


                        Text(
                            text = "暂无历史数据",

                            style = MaterialTheme.typography.bodyMedium,

                            color = MaterialTheme.colorScheme.onSurfaceVariant


                        )
                        Text(
                            text = "请先添加或修改资产以生成历史记录",

                            style = MaterialTheme.typography.bodySmall,

                            color = MaterialTheme.colorScheme.onSurfaceVariant,

                            modifier = Modifier.padding(top = 8.dp)


                        )
                    }
                }
            } else {
                // 折线图 - 使用窗口数据
                val dragSensitivity = 50f // 每50像素滚动一个数据点

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .pointerInput(selectedDimension, startIndex) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    // 拖动结束，根据累计距离滚动数据点
                                    val pointsToScroll = (dragAccumulated / dragSensitivity).toInt()
                                    if (pointsToScroll != 0) {
                                        val newStartIndex = startIndex - pointsToScroll
                                        startIndex = newStartIndex.coerceIn(0, maxOf(0, totalPoints - windowSize))
                                    }
                                    dragAccumulated = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    // 累计拖动距离
                                    dragAccumulated += dragAmount
                                    change.consume()
                                }
                            )
                        }
                ) {
                    AssetLineChart(
                        history = windowData,
                        modifier = Modifier.fillMaxSize(),
                        onPointClick = { index, date, value ->
                            scope.launch {
                                snackbarHostState.showSnackbar("$date: ¥${DecimalFormat("#,##0.00").format(value)}")
                            }
                        }
                    )

                    // 添加拖动提示
                    if (abs(dragAccumulated) > 10f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = if (dragAccumulated > 0) Alignment.CenterStart else Alignment.CenterEnd
                        ) {
                            Text(
                                text = if (dragAccumulated > 0) "← 查看更早数据" else "查看更新数据 →",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetLineChart(
    history: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    onPointClick: ((index: Int, date: String, value: Double) -> Unit)? = null
) {
    val values = history.map { it.second }
    val maxValue = values.maxOrNull() ?: 0.0
    val minValue = values.minOrNull() ?: 0.0
    val valueRange = maxValue - minValue
    val primaryColor = MaterialTheme.colorScheme.primary
    val canvasSize = remember { mutableStateOf(IntSize.Zero) }
    val textMeasurer = rememberTextMeasurer()
    val chartModifier = if (onPointClick != null) {
        modifier
            .onSizeChanged { canvasSize.value = it }
            .pointerInput(history, canvasSize.value) {
                detectTapGestures { offset ->
                    // 计算点击位置
                    val paddingLeft = 40f
                    val paddingRight = 60f  // 与绘图时的paddingRight一致
                    val paddingTop = 40f
                    val paddingBottom = 40f
                    val canvasWidth = canvasSize.value.width - paddingLeft - paddingRight
                    val canvasHeight = canvasSize.value.height - paddingTop - paddingBottom
                    // 找到最近的数据点
                    var minDistance = Float.MAX_VALUE
                    var selectedIndex = -1
                    history.forEachIndexed { index, (_, value) ->
                        val x = if (history.size > 1) {
                            paddingLeft + canvasWidth * index.toFloat() / (history.size - 1).toFloat()
                        } else {
                            paddingLeft + canvasWidth / 2
                        }
                        val y = if (valueRange > 0) {
                            paddingTop + canvasHeight * (1 - (value - minValue).toFloat() / valueRange.toFloat())
                        } else if (maxValue == 0.0) {
                            // 如果全是0的数据，贴着x轴显示
                            paddingTop + canvasHeight
                        } else {
                            paddingTop + canvasHeight / 2
                        }
                        val distance = (offset.x - x) * (offset.x - x) + (offset.y - y) * (offset.y - y)
                        if (distance < minDistance) {
                            minDistance = distance
                            selectedIndex = index
                        }
                    }
                    // 如果点击在点半径范围内（例如20像素）
                    if (selectedIndex >= 0 && minDistance < 20 * 20) {
                        val (date, value) = history[selectedIndex]
                        onPointClick(selectedIndex, date, value)
                    }
                }
            }
    } else {
        modifier
    }

    // 添加一些边距，使折线不紧贴边界
    val paddingTop = 40f
    val paddingBottom = 40f
    val paddingLeft = 40f
    val paddingRight = 60f  // 增加右侧边距以显示Y轴标签

    Canvas(modifier = chartModifier) {
        val canvasWidth = size.width - paddingLeft - paddingRight
        val canvasHeight = size.height - paddingTop - paddingBottom

        // 绘制Y轴
        drawLine(
            color = Color.Gray,
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, paddingTop + canvasHeight),
            strokeWidth = 2f
        )

        // 绘制X轴
        drawLine(
            color = Color.Gray,
            start = Offset(paddingLeft, paddingTop + canvasHeight),
            end = Offset(paddingLeft + canvasWidth, paddingTop + canvasHeight),
            strokeWidth = 2f
        )

        // 绘制Y轴箭头（顶部）
        val arrowSize = 16f
        drawPath(
            path = Path().apply {
                moveTo(paddingLeft - arrowSize, paddingTop + arrowSize)
                lineTo(paddingLeft, paddingTop)
                lineTo(paddingLeft + arrowSize, paddingTop + arrowSize)
                close()
            },
            color = Color.Gray
        )

        // 绘制X轴箭头（右侧）
        drawPath(
            path = Path().apply {
                moveTo(paddingLeft + canvasWidth - arrowSize, paddingTop + canvasHeight - arrowSize)
                lineTo(paddingLeft + canvasWidth, paddingTop + canvasHeight)
                lineTo(paddingLeft + canvasWidth - arrowSize, paddingTop + canvasHeight + arrowSize)
                close()
            },
            color = Color.Gray
        )

        // 绘制Y轴刻度
        val yTickCount = 5
        for (i in 0..yTickCount) {
            val y = paddingTop + canvasHeight * (1 - i.toFloat() / yTickCount)

            // 刻度线
            drawLine(
                color = Color.LightGray,
                start = Offset(paddingLeft - 5, y),
                end = Offset(paddingLeft, y),
                strokeWidth = 1f
            )
        }

        // 绘制Y轴刻度标签（只显示最大值和最小值）
        val decimalFormat = DecimalFormat("#,##0.00")
        val textStyle = TextStyle(color = Color.Gray, fontSize = 12.sp)

        // 绘制最小值标签（底部）
        val minValueText = decimalFormat.format(minValue)
        val minTextLayoutResult = textMeasurer.measure(minValueText, textStyle)
        val minY = paddingTop + canvasHeight  // 底部位置
        drawText(
            textLayoutResult = minTextLayoutResult,
            topLeft = Offset(paddingLeft + 15, minY - minTextLayoutResult.size.height / 2)
        )

        // 绘制最大值标签（顶部）
        val maxValueText = decimalFormat.format(maxValue)
        val maxTextLayoutResult = textMeasurer.measure(maxValueText, textStyle)
        val maxY = paddingTop  // 顶部位置
        drawText(
            textLayoutResult = maxTextLayoutResult,
            topLeft = Offset(paddingLeft + 15, maxY - maxTextLayoutResult.size.height / 2)
        )

        // 绘制X轴刻度
        val xTickCount = minOf(5, history.size)
        for (i in 0 until xTickCount) {
            val x = if (xTickCount > 1) {
                paddingLeft + canvasWidth * i.toFloat() / (xTickCount - 1).toFloat()
            } else {
                paddingLeft + canvasWidth / 2
            }

            // 刻度线
            drawLine(
                color = Color.LightGray,
                start = Offset(x, paddingTop + canvasHeight),
                end = Offset(x, paddingTop + canvasHeight + 5),
                strokeWidth = 1f
            )
        }

        // 绘制X轴起点和终点时间标签
        if (history.isNotEmpty()) {
            val startDate = history.first().first
            val endDate = history.last().first

            // 起点时间标签
            val startTextLayoutResult = textMeasurer.measure(startDate, textStyle)
            drawText(
                textLayoutResult = startTextLayoutResult,
                topLeft = Offset(paddingLeft, paddingTop + canvasHeight + 10)
            )

            // 终点时间标签
            val endTextLayoutResult = textMeasurer.measure(endDate, textStyle)
            drawText(
                textLayoutResult = endTextLayoutResult,
                topLeft = Offset(paddingLeft + canvasWidth - endTextLayoutResult.size.width, paddingTop + canvasHeight + 10)
            )
        }

        // 绘制数据点和折线
        val pointRadius = 6f
        val path = Path()
        var firstPoint = true

        history.forEachIndexed { index, (_, value) ->
            val x = if (history.size > 1) {
                paddingLeft + canvasWidth * index.toFloat() / (history.size - 1).toFloat()
            } else {
                paddingLeft + canvasWidth / 2 // 只有一个点时居中显示
            }

            val y = if (valueRange > 0) {
                paddingTop + canvasHeight * (1 - (value - minValue).toFloat() / valueRange.toFloat())
            } else if (maxValue == 0.0) {
                // 如果全是0的数据，贴着x轴显示
                paddingTop + canvasHeight
            } else {
                paddingTop + canvasHeight / 2 // 所有值相同时居中显示
            }

            if (firstPoint) {
                path.moveTo(x, y)
                firstPoint = false
            } else {
                path.lineTo(x, y)
            }

            // 绘制数据点
            drawCircle(
                color = primaryColor,
                radius = pointRadius,
                center = Offset(x, y)
            )
        }

        // 绘制折线（只有多个点时才绘制）
        if (history.size > 1) {
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3f)
            )
        }
    }
}

private fun getDimensionDisplayName(dimension: TimeDimension): String {
    return when (dimension) {
        TimeDimension.DAY -> "日"
        TimeDimension.WEEK -> "周"
        TimeDimension.MONTH -> "月"
        TimeDimension.YEAR -> "年"
    }
}