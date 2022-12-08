package com.yara.raco.ui.components

import androidx.compose.runtime.Composable
import com.yara.raco.model.schedule.Schedule
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.himanshoe.kalendar.Kalendar
import com.himanshoe.kalendar.color.KalendarThemeColor
import com.himanshoe.kalendar.model.KalendarType


@Composable
fun RacoScheduleWeek(
    schedules: List<Schedule>
) {
    val colorSubject = HashMap<String, Color>()
    for (subject in schedules.distinctBy{ it.codiAssig }.withIndex()){
        colorSubject[subject.value.codiAssig] = predefinedColors[subject.index % predefinedColors.size]
    }

    val events = ArrayList<Event>()

    for (schedule in schedules){
        events.add(convertScheduleToEvent(schedule, colorSubject))
    }
    Schedule(events, daySize = ScheduleSize.Adaptive(0.dp))
}