package com.yara.raco.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.yara.raco.R
import com.yara.raco.model.event.Event
import com.yara.raco.model.exam.Exam
import com.yara.raco.model.schedule.Schedule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.roundToInt

data class ScheduleEvent(
    val name: String,
    val color: Color,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val description: String? = null,
    val location: String? = null,
)

@JvmInline
value class SplitType private constructor(val value: Int) {
    companion object {
        val None = SplitType(0)
        val Start = SplitType(1)
        val End = SplitType(2)
        val Both = SplitType(3)
    }
}

data class PositionedEvent(
    val scheduleEvent: ScheduleEvent,
    val splitType: SplitType,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val col: Int = 0,
    val colSpan: Int = 1,
    val colTotal: Int = 1,
)

@Composable
fun BasicEvent(
    positionedEvent: PositionedEvent,
    modifier: Modifier = Modifier,
    onEventClick: ((ScheduleEvent) -> Unit)? = null,
) {
    val event = positionedEvent.scheduleEvent
    val topRadius =
        if (positionedEvent.splitType == SplitType.Start || positionedEvent.splitType == SplitType.Both) 0.dp else 4.dp
    val bottomRadius =
        if (positionedEvent.splitType == SplitType.End || positionedEvent.splitType == SplitType.Both) 0.dp else 4.dp
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                end = 2.dp,
                bottom = if (positionedEvent.splitType == SplitType.End) 0.dp else 2.dp
            )
            .clipToBounds()
            .background(
                event.color,
                shape = RoundedCornerShape(
                    topStart = topRadius,
                    topEnd = topRadius,
                    bottomEnd = bottomRadius,
                    bottomStart = bottomRadius,
                )
            )
            .then(if (onEventClick != null) Modifier.clickable { onEventClick(positionedEvent.scheduleEvent) } else Modifier)
            .padding(4.dp)
    ) {
        Text(
            text = event.name,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
        )

        if (event.location != null) {
            Text(
                text = event.location,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun HeaderEvent(
    positionedEvent: PositionedEvent,
    modifier: Modifier = Modifier,
    onEventClick: ((ScheduleEvent) -> Unit)? = null,
) {
    val event = positionedEvent.scheduleEvent
    val topRadius =
        if (positionedEvent.splitType == SplitType.Start || positionedEvent.splitType == SplitType.Both) 0.dp else 4.dp
    val bottomRadius =
        if (positionedEvent.splitType == SplitType.End || positionedEvent.splitType == SplitType.Both) 0.dp else 4.dp
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                end = 2.dp,
                bottom = if (positionedEvent.splitType == SplitType.End) 0.dp else 2.dp
            )
            .clipToBounds()
            .background(
                event.color,
                shape = RoundedCornerShape(
                    topStart = topRadius,
                    topEnd = topRadius,
                    bottomEnd = bottomRadius,
                    bottomStart = bottomRadius,
                )
            )
            .then(if (onEventClick != null) Modifier.clickable { onEventClick(positionedEvent.scheduleEvent) } else Modifier)
            .padding(4.dp)
    ) {
        Text(
            text = event.name,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}


private class EventDataModifier(
    val positionedEvent: PositionedEvent,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = positionedEvent
}

private fun Modifier.eventData(positionedEvent: PositionedEvent) =
    this.then(EventDataModifier(positionedEvent))

//private val DayFormatter = DateTimeFormatter.ofPattern("EE, MMM d")
private val DayFormatter = DateTimeFormatter.ofPattern("EE")

@Composable
fun BasicDayHeader(
    day: LocalDate,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = day.format(DayFormatter),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )

        val selected = day == LocalDate.now()

        Box(
            modifier = Modifier
                .clip(shape = CircleShape)
                .size(size = 36.dp)
                .background(color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent),
            contentAlignment = Center,
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                fontSize = 16.sp,
                color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
fun ScheduleHeader(
    minDate: LocalDate,
    maxDate: LocalDate,
    dayWidth: Dp,
    modifier: Modifier = Modifier,
    dayHeader: @Composable (day: LocalDate) -> Unit = { BasicDayHeader(day = it) },
) {
    Column {
        Row(modifier = modifier) {
            val numDays = ChronoUnit.DAYS.between(minDate, maxDate).toInt() + 1
            repeat(numDays) { i ->
                Box(modifier = Modifier.width(dayWidth), contentAlignment = Center) {
                    dayHeader(minDate.plusDays(i.toLong()))
                }
            }
        }
    }
}

private val HourFormatter = DateTimeFormatter.ofPattern("HH:00")

@Composable
fun BasicSidebarLabel(
    time: LocalTime,
    modifier: Modifier = Modifier,
) {
    Text(
        text = time.format(HourFormatter),
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
            .fillMaxHeight()
            .padding(6.dp)
            .padding(horizontal = 2.dp)
    )
}

@Composable
fun ScheduleSidebar(
    hourHeight: Dp,
    modifier: Modifier = Modifier,
    minTime: LocalTime = LocalTime.MIN,
    maxTime: LocalTime = LocalTime.MAX,
    label: @Composable (time: LocalTime) -> Unit = { BasicSidebarLabel(time = it) },
) {
    val numMinutes = ChronoUnit.MINUTES.between(minTime, maxTime).toInt() + 1
    val numHours = numMinutes / 60
    val firstHour = minTime.truncatedTo(ChronoUnit.HOURS)
    val firstHourOffsetMinutes = if (firstHour == minTime) 0 else ChronoUnit.MINUTES.between(minTime, firstHour.plusHours(1))
    val firstHourOffset = hourHeight * (firstHourOffsetMinutes / 60f)
    val startTime = if (firstHour == minTime) firstHour else firstHour.plusHours(1)
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(firstHourOffset))
        repeat(numHours) { i ->
            Box(modifier = Modifier.height(hourHeight)) {
                label(startTime.plusHours(i.toLong()))
            }
        }
    }
}


private fun splitEvents(scheduleEvents: List<ScheduleEvent>): List<PositionedEvent> {
    return scheduleEvents
        .map { event ->
            val startDate = event.start.toLocalDate()
            val endDate = event.end.toLocalDate()
            if (startDate == endDate) {
                listOf(
                    PositionedEvent(
                        event,
                        SplitType.None,
                        event.start.toLocalDate(),
                        event.start.toLocalTime(),
                        event.end.toLocalTime()
                    )
                )
            } else {
                val days = ChronoUnit.DAYS.between(startDate, endDate)
                val splitEvents = mutableListOf<PositionedEvent>()
                for (i in 0..days) {
                    val date = startDate.plusDays(i)
                    splitEvents += PositionedEvent(
                        event,
                        splitType = if (date == startDate) SplitType.End else if (date == endDate) SplitType.Start else SplitType.Both,
                        date = date,
                        start = if (date == startDate) event.start.toLocalTime() else LocalTime.MIN,
                        end = if (date == endDate) event.end.toLocalTime() else LocalTime.MAX,
                    )
                }
                splitEvents
            }
        }
        .flatten()
}

private fun PositionedEvent.overlapsWith(other: PositionedEvent): Boolean {
    return date == other.date && start < other.end && end > other.start
}

private fun List<PositionedEvent>.timesOverlapWith(event: PositionedEvent): Boolean {
    return any { it.overlapsWith(event) }
}

private fun arrangeEvents(events: List<PositionedEvent>): List<PositionedEvent> {
    val positionedEvents = mutableListOf<PositionedEvent>()
    val groupEvents: MutableList<MutableList<PositionedEvent>> = mutableListOf()

    fun resetGroup() {
        groupEvents.forEachIndexed { colIndex, col ->
            col.forEach { e ->
                positionedEvents.add(e.copy(col = colIndex, colTotal = groupEvents.size))
            }
        }
        groupEvents.clear()
    }

    events.forEach { event ->
        var firstFreeCol = -1
        var numFreeCol = 0
        for (i in 0 until groupEvents.size) {
            val col = groupEvents[i]
            if (col.timesOverlapWith(event)) {
                if (firstFreeCol < 0) continue else break
            }
            if (firstFreeCol < 0) firstFreeCol = i
            numFreeCol++
        }

        when {
            // Overlaps with all, add a new column
            firstFreeCol < 0 -> {
                groupEvents += mutableListOf(event)
                // Expand anything that spans into the previous column and doesn't overlap with this event
                for (ci in 0 until groupEvents.size - 1) {
                    val col = groupEvents[ci]
                    col.forEachIndexed { ei, e ->
                        if (ci + e.colSpan == groupEvents.size - 1 && !e.overlapsWith(event)) {
                            col[ei] = e.copy(colSpan = e.colSpan + 1)
                        }
                    }
                }
            }
            // No overlap with any, start a new group
            numFreeCol == groupEvents.size -> {
                resetGroup()
                groupEvents += mutableListOf(event)
            }
            // At least one column free, add to first free column and expand to as many as possible
            else -> {
                groupEvents[firstFreeCol] += event.copy(colSpan = numFreeCol)
            }
        }
    }
    resetGroup()
    return positionedEvents
}

sealed class ScheduleSize {
    class FixedSize(val size: Dp) : ScheduleSize()
    class FixedCount(val count: Float) : ScheduleSize()
    class Adaptive(val minSize: Dp) : ScheduleSize()
}

@Composable
fun Schedule(
    scheduleEvents: List<ScheduleEvent>,
    headerEvents: List<ScheduleEvent>,
    modifier: Modifier = Modifier,
    onEventClick: ((ScheduleEvent) -> Unit)? = null,
    eventContent: @Composable (positionedEvent: PositionedEvent) -> Unit = {
        BasicEvent(
            positionedEvent = it,
            onEventClick = onEventClick
        )
    },
    headerEventContent: @Composable (positionedEvent: PositionedEvent) -> Unit = {
        HeaderEvent(
            positionedEvent = it,
            onEventClick = onEventClick
        )
    },
    dayHeader: @Composable (day: LocalDate) -> Unit = { BasicDayHeader(day = it) },
    timeLabel: @Composable (time: LocalTime) -> Unit = { BasicSidebarLabel(time = it) },
    minDate: LocalDate = scheduleEvents.plus(headerEvents)
        .minByOrNull(ScheduleEvent::start)?.start?.toLocalDate()
        ?: LocalDate.now(),
    maxDate: LocalDate = scheduleEvents.plus(headerEvents)
        .maxByOrNull(ScheduleEvent::end)?.end?.toLocalDate()
        ?: LocalDate.now(),
    minTime: LocalTime = LocalTime.MIN,
    maxTime: LocalTime = LocalTime.MAX,
    daySize: ScheduleSize = ScheduleSize.FixedSize(256.dp),
    hourSize: ScheduleSize = ScheduleSize.FixedSize(64.dp),
) {
    val numDays = ChronoUnit.DAYS.between(minDate, maxDate).toInt() + 1
    val numMinutes = ChronoUnit.MINUTES.between(minTime, maxTime).toInt() + 1
    val numHours = numMinutes.toFloat() / 60f
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    var sidebarWidth by remember { mutableStateOf(0) }
    var headerHeight by remember { mutableStateOf(0) }
    BoxWithConstraints(modifier = modifier) {
        val dayWidth: Dp = when (daySize) {
            is ScheduleSize.FixedSize -> daySize.size
            is ScheduleSize.FixedCount -> with(LocalDensity.current) { ((constraints.maxWidth - sidebarWidth) / daySize.count).toDp() }
            is ScheduleSize.Adaptive -> with(LocalDensity.current) { maxOf(((constraints.maxWidth - sidebarWidth) / numDays).toDp(), daySize.minSize) }
        }
        val hourHeight: Dp = when (hourSize) {
            is ScheduleSize.FixedSize -> hourSize.size
            is ScheduleSize.FixedCount -> with(LocalDensity.current) { ((constraints.maxHeight - headerHeight) / hourSize.count).toDp() }
            is ScheduleSize.Adaptive -> with(LocalDensity.current) { maxOf(((constraints.maxHeight - headerHeight) / numHours).toDp(), hourSize.minSize) }
        }
        Column(modifier = modifier) {
            if (daySize !is ScheduleSize.FixedCount || daySize.count != 1f) {
                ScheduleHeader(
                    minDate = minDate,
                    maxDate = maxDate,
                    dayWidth = dayWidth,
                    dayHeader = dayHeader,
                    modifier = Modifier
                        .padding(start = with(LocalDensity.current) { sidebarWidth.toDp() })
                        .then(
                            if (daySize !is ScheduleSize.FixedCount)
                                Modifier.horizontalScroll(horizontalScrollState) else Modifier
                        )
                        .onGloballyPositioned { headerHeight = it.size.height }
                )
            }
            if (headerEvents.isNotEmpty()) {
                Row(modifier = Modifier.align(Start)) {
                    HeaderDaySchedule(
                        scheduleEvents = headerEvents,
                        dayWidth = dayWidth,
                        dayHeight = 26.dp,
                        minDate = minDate,
                        maxDate = maxDate,
                        eventContent = headerEventContent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (daySize !is ScheduleSize.FixedCount)
                                    Modifier.horizontalScroll(horizontalScrollState) else Modifier
                            )
                            .padding(
                                start = with(LocalDensity.current) { sidebarWidth.toDp() },
                                bottom = 4.dp
                            )
                    )
                }
            }
            Divider()
            Row(
                modifier = Modifier
                    .weight(1f)
                    .align(Start)
            ) {
                ScheduleSidebar(
                    hourHeight = hourHeight,
                    minTime = minTime,
                    maxTime = maxTime,
                    label = timeLabel,
                    modifier = Modifier
                        .verticalScroll(verticalScrollState)
                        .onGloballyPositioned { sidebarWidth = it.size.width }
                )
                BasicSchedule(
                    scheduleEvents = scheduleEvents,
                    eventContent = eventContent,
                    minDate = minDate,
                    maxDate = maxDate,
                    minTime = minTime,
                    maxTime = maxTime,
                    dayWidth = dayWidth,
                    hourHeight = hourHeight,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(verticalScrollState)
                        .then(
                            if (daySize !is ScheduleSize.FixedCount)
                                Modifier.horizontalScroll(horizontalScrollState) else Modifier
                        )
                )
            }
        }
    }
}

@Composable
fun BasicSchedule(
    scheduleEvents: List<ScheduleEvent>,
    modifier: Modifier = Modifier,
    eventContent: @Composable (positionedEvent: PositionedEvent) -> Unit = {
        BasicEvent(
            positionedEvent = it
        )
    },
    minDate: LocalDate = scheduleEvents.minByOrNull(ScheduleEvent::start)?.start?.toLocalDate()
        ?: LocalDate.now(),
    maxDate: LocalDate = scheduleEvents.maxByOrNull(ScheduleEvent::end)?.end?.toLocalDate()
        ?: LocalDate.now(),
    minTime: LocalTime = LocalTime.MIN,
    maxTime: LocalTime = LocalTime.MAX,
    dayWidth: Dp,
    hourHeight: Dp,
) {
    val numDays = ChronoUnit.DAYS.between(minDate, maxDate).toInt() + 1
    val numMinutes = ChronoUnit.MINUTES.between(minTime, maxTime).toInt() + 1
    val numHours = numMinutes / 60
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val positionedEvents =
        remember(scheduleEvents) { arrangeEvents(splitEvents(scheduleEvents.sortedBy(ScheduleEvent::start))).filter { it.end > minTime && it.start < maxTime } }
    Layout(
        content = {
            positionedEvents.forEach { positionedEvent ->
                Box(
                    modifier = Modifier.eventData(positionedEvent),
                    contentAlignment = Alignment.TopCenter
                ) {
                    eventContent(positionedEvent)
                }
            }
        },
        modifier = modifier
            .drawBehind {
                val firstHour = minTime.truncatedTo(ChronoUnit.HOURS)
                val firstHourOffsetMinutes = if (firstHour == minTime) 0 else ChronoUnit.MINUTES.between(minTime, firstHour.plusHours(1))
                val firstHourOffset = (firstHourOffsetMinutes / 60f) * hourHeight.toPx()
                repeat(numHours) {
                    drawLine(
                        dividerColor,
                        start = Offset(0f, it * hourHeight.toPx() + firstHourOffset),
                        end = Offset(size.width, it * hourHeight.toPx() + firstHourOffset),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                repeat(numDays - 1) {
                    drawLine(
                        dividerColor,
                        start = Offset((it + 1) * dayWidth.toPx(), 0f),
                        end = Offset((it + 1) * dayWidth.toPx(), size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
    ) { measureables, constraints ->
        val height = (hourHeight.toPx() * (numMinutes / 60f)).roundToInt()
        val width = dayWidth.roundToPx() * numDays
        val placeablesWithEvents = measureables.map { measurable ->
            val splitEvent = measurable.parentData as PositionedEvent
            val eventDurationMinutes = ChronoUnit.MINUTES.between(splitEvent.start, minOf(splitEvent.end, maxTime))
            val eventHeight = ((eventDurationMinutes / 60f) * hourHeight.toPx()).roundToInt()
            val eventWidth = ((splitEvent.colSpan.toFloat() / splitEvent.colTotal.toFloat()) * dayWidth.toPx()).roundToInt()
            val placeable = measurable.measure(constraints.copy(minWidth = eventWidth, maxWidth = eventWidth, minHeight = eventHeight, maxHeight = eventHeight))
            Pair(placeable, splitEvent)
        }
        layout(width, height) {
            placeablesWithEvents.forEach { (placeable, splitEvent) ->
                val eventOffsetMinutes = if (splitEvent.start > minTime) ChronoUnit.MINUTES.between(
                    minTime,
                    splitEvent.start
                ) else 0
                val eventY = ((eventOffsetMinutes / 60f) * hourHeight.toPx()).roundToInt()
                val eventOffsetDays = ChronoUnit.DAYS.between(minDate, splitEvent.date).toInt()
                val eventX =
                    eventOffsetDays * dayWidth.roundToPx() + (splitEvent.col * (dayWidth.toPx() / splitEvent.colTotal.toFloat())).roundToInt()
                placeable.place(eventX, eventY)
            }
        }
    }
}

@Composable
fun HeaderDaySchedule(
    scheduleEvents: List<ScheduleEvent>,
    modifier: Modifier = Modifier,
    eventContent: @Composable (positionedEvent: PositionedEvent) -> Unit = {
        HeaderEvent(
            positionedEvent = it
        )
    },
    minDate: LocalDate = scheduleEvents.minByOrNull(ScheduleEvent::start)?.start?.toLocalDate()
        ?: LocalDate.now(),
    maxDate: LocalDate = scheduleEvents.maxByOrNull(ScheduleEvent::end)?.end?.toLocalDate()
        ?: LocalDate.now(),
    dayWidth: Dp,
    dayHeight: Dp
) {
    val numDays = ChronoUnit.DAYS.between(minDate, maxDate).toInt() + 1
    val positionedEvents =
        remember(scheduleEvents) { arrangeEvents(splitEvents(scheduleEvents.sortedBy(ScheduleEvent::start))) }
    Layout(
        content = {
            positionedEvents.forEach { positionedEvent ->
                Box(
                    modifier = Modifier.eventData(positionedEvent),
                    contentAlignment = Alignment.TopCenter
                ) {
                    eventContent(positionedEvent)
                }
            }
        },
        modifier = modifier,
    ) { measureables, constraints ->
        val eventHeight = dayHeight.roundToPx()
        val width = dayWidth.roundToPx() * numDays
        val placeablesWithEvents = measureables.map { measurable ->
            val splitEvent = measurable.parentData as PositionedEvent
            val eventWidth =
                ((splitEvent.colSpan.toFloat() / splitEvent.colTotal.toFloat()) * dayWidth.toPx()).roundToInt()
            val placeable = measurable.measure(
                constraints.copy(
                    minWidth = eventWidth,
                    maxWidth = eventWidth,
                    minHeight = eventHeight,
                    maxHeight = eventHeight
                )
            )
            Pair(placeable, splitEvent)
        }
        layout(width, eventHeight) {
            placeablesWithEvents.forEach { (placeable, splitEvent) ->
                val eventOffsetDays = ChronoUnit.DAYS.between(minDate, splitEvent.date).toInt()
                val eventX =
                    eventOffsetDays * dayWidth.roundToPx() + (splitEvent.col * (dayWidth.toPx() / splitEvent.colTotal.toFloat())).roundToInt()
                placeable.place(eventX, 0)
            }
        }
    }
}

var predefinedColors = listOf(
    Color(0xffcddeef),
    Color(0xfff3dbbe),
    Color(0xffb5cbcc),
    Color(0xffffc9cc),
    Color(0xffdbe6db),
    Color(0xffffffdb),
    Color(0xfff0ecec),
    Color(0xfffee1e8),
    Color(0xffbec6eb),
    Color(0xfffbecc6),
    Color(0xfffcf2f3),
    Color(0xfff3b0c3)
)

val EventTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
val EventDateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@Composable
fun ScheduleEventDetailsDialog(
    scheduleEvent: ScheduleEvent,
    onDismissRequest: () -> Unit,
) {
    val locationInlineContent = mapOf(
        Pair(
            "location_icon",
            InlineTextContent(
                placeholder = Placeholder(
                    width = LocalTextStyle.current.fontSize,
                    height = LocalTextStyle.current.fontSize,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = stringResource(
                        id = R.string.location
                    )
                )
            }
        )
    )

    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(all = 24.dp)) {
                Text(
                    text = scheduleEvent.name,
                    style = MaterialTheme.typography.headlineSmall,
                )

                Text(
                    text = scheduleEvent.start.format(EventDateFormatter) +
                            if (scheduleEvent.start.toLocalTime() != scheduleEvent.end.toLocalTime())
                                " ⦁ ${scheduleEvent.start.format(EventTimeFormatter)}-${
                                    scheduleEvent.end.format(
                                        EventTimeFormatter
                                    )
                                }"
                            else ""
                )

                if (scheduleEvent.location != null) {
                    Text(
                        text = buildAnnotatedString {
                            appendInlineContent("location_icon")
                            append(" ${scheduleEvent.location}")
                        },
                        inlineContent = locationInlineContent
                    )
                }

                if (scheduleEvent.description != null && scheduleEvent.description.isNotBlank()) {
                    Text(text = scheduleEvent.description, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RacoScheduleDay(
    schedules: List<Schedule>,
    exams: List<Exam>,
    events: List<Event>,
    setTitle: (String) -> Unit,
    pagerState: PagerState,
    onEventClick: (ScheduleEvent) -> Unit
) {
    val today = remember {
        LocalDate.now().atTime(0, 0)
    }

    val firstCalendarDay = remember {
        today.minusMonths(4L).with(
            TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
        )
    }

    val lastCalendarDay = remember {
        today.plusMonths(6L).with(
            TemporalAdjusters.nextOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
        ).minusDays(1L)
    }

    val colorSubject = HashMap<String, Color>()
    for (subject in schedules.distinctBy { it.codiAssig }.withIndex()) {
        colorSubject[subject.value.codiAssig] =
            predefinedColors[subject.index % predefinedColors.size]
    }

    val examEvents = ArrayList<ScheduleEvent>()
    for (exam in exams) {
        examEvents.add(exam.toScheduleEvent(colorSubject))
    }

    val eventEvents = ArrayList<ScheduleEvent>()
    for (event in events) {
        eventEvents.add(event.toScheduleEvent())
    }

    val pages = ChronoUnit.DAYS.between(firstCalendarDay, lastCalendarDay).toInt()

    LaunchedEffect(key1 = pagerState.currentPage) {
        val currentDay = firstCalendarDay.plusDays(pagerState.currentPage.toLong())
        setTitle(currentDay.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
    }

    HorizontalPager(count = pages, state = pagerState) { page ->
        Column(horizontalAlignment = Start) {
            val currentDay = firstCalendarDay.plusDays(page.toLong())

            val weekExams =
                remember { examEvents.filter { it.start.toLocalDate() == currentDay.toLocalDate() } }
            val scheduleEvents = ArrayList<ScheduleEvent>()
            val firstWeekDay = currentDay.with(
                TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
            )
            for (schedule in schedules) {
                val scheduleEvent = schedule.toScheduleEvent(colorSubject, firstWeekDay)
                if (scheduleEvent.start.toLocalDate() == currentDay.toLocalDate())
                    scheduleEvents.add(schedule.toScheduleEvent(colorSubject, firstWeekDay))
            }
            scheduleEvents.addAll(weekExams)

            val headerEvents = ArrayList<ScheduleEvent>()
            for (headerEvent in eventEvents) {
                if (headerEvent.start.toLocalDate() == currentDay.toLocalDate() ||
                    (headerEvent.start.toLocalDate() < currentDay.toLocalDate() && headerEvent.end.toLocalDate() >= currentDay.toLocalDate())
                ) {
                    headerEvents.add(headerEvent.copy(start = currentDay, end = currentDay))
                }
            }

            BasicDayHeader(day = currentDay.toLocalDate(), modifier = Modifier.align(Start))

            Schedule(
                scheduleEvents = scheduleEvents,
                headerEvents = headerEvents,
                daySize = ScheduleSize.FixedCount(1f),
                minTime = minOf(
                    scheduleEvents.minByOrNull { it.start }?.start?.toLocalTime()
                        ?: LocalTime.of(8, 0), LocalTime.of(8, 0)
                ),
                maxTime = maxOf(
                    scheduleEvents.maxByOrNull { it.end }?.end?.toLocalTime()
                        ?: LocalTime.of(20, 0), LocalTime.of(20, 0)
                ),
                onEventClick = onEventClick
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RacoScheduleWeek(
    schedules: List<Schedule>,
    exams: List<Exam>,
    events: List<Event>,
    setTitle: (String) -> Unit,
    pagerState: PagerState,
    onEventClick: (ScheduleEvent) -> Unit
) {
    val today = remember {
        LocalDate.now().atTime(0, 0)
    }

    val firstCalendarDay = remember {
        today.minusMonths(4L).with(
            TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
        )
    }

    val lastCalendarDay = remember {
        today.plusMonths(6L).with(
            TemporalAdjusters.nextOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
        ).minusDays(1L)
    }

    val colorSubject = HashMap<String, Color>()
    for (subject in schedules.distinctBy { it.codiAssig }.withIndex()) {
        colorSubject[subject.value.codiAssig] =
            predefinedColors[subject.index % predefinedColors.size]
    }

    val examEvents = ArrayList<ScheduleEvent>()
    for (exam in exams) {
        examEvents.add(exam.toScheduleEvent(colorSubject))
    }

    val eventEvents = ArrayList<ScheduleEvent>()
    for (event in events) {
        eventEvents.add(event.toScheduleEvent())
    }

    val pages = ChronoUnit.WEEKS.between(firstCalendarDay, lastCalendarDay).toInt()

    LaunchedEffect(key1 = pagerState.currentPage) {
        val firstWeekDay = firstCalendarDay.plusWeeks(pagerState.currentPage.toLong())
        setTitle(firstWeekDay.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
    }

    HorizontalPager(count = pages, state = pagerState) { page ->
        val firstWeekDay = firstCalendarDay.plusWeeks(page.toLong())
        val lastWeekDay = firstCalendarDay.plusWeeks(page.toLong() + 1).minusDays(1L)

        val weekExams =
            remember { examEvents.filter { it.start >= firstWeekDay && it.start < lastWeekDay } }
        val scheduleEvents = ArrayList<ScheduleEvent>()
        for (schedule in schedules) {
            scheduleEvents.add(schedule.toScheduleEvent(colorSubject, firstWeekDay))
        }
        scheduleEvents.addAll(weekExams)

        val headerEvents = ArrayList<ScheduleEvent>()
        for (headerEvent in eventEvents) {
            if ((headerEvent.start >= firstWeekDay && headerEvent.start <= lastWeekDay) ||
                (headerEvent.start < firstWeekDay && headerEvent.end >= firstWeekDay)
            ) {
                val firstEventDay = maxOf(headerEvent.start, firstWeekDay)
                val lastEventDay = minOf(headerEvent.end, lastWeekDay)
                for (day in 0..ChronoUnit.DAYS.between(firstEventDay, lastEventDay)) {
                    headerEvents.add(
                        headerEvent.copy(
                            start = firstEventDay.plusDays(day),
                            end = firstEventDay.plusDays(day)
                        )
                    )
                }
            }
        }

        Schedule(
            scheduleEvents = scheduleEvents, daySize = ScheduleSize.FixedCount(7f),
            headerEvents = headerEvents,
            minTime = minOf(
                scheduleEvents.minByOrNull { it.start.hour }?.start?.toLocalTime()
                    ?: LocalTime.of(8, 0), LocalTime.of(8, 0)
            ),
            maxTime = maxOf(
                scheduleEvents.maxByOrNull { it.end.hour }?.end?.toLocalTime()
                    ?: LocalTime.of(20, 0), LocalTime.of(20, 0)
            ),
            minDate = firstWeekDay.toLocalDate(),
            maxDate = lastWeekDay.toLocalDate(),
            onEventClick = onEventClick
        )
    }
}
