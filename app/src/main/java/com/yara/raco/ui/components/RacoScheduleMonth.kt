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
fun RacoScheduleMonth(
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

    Column() {
        Kalendar(
            kalendarType = KalendarType.Oceanic(),
            kalendarThemeColor = KalendarThemeColor(
                backgroundColor = MaterialTheme.colorScheme.surface,
                dayBackgroundColor = MaterialTheme.colorScheme.primary,
                headerTextColor = MaterialTheme.colorScheme.primary
            )
        )

        Schedule(events, daySize = ScheduleSize.Adaptive(0.dp))
    }




/*val currentDate = remember { mutableStateOf(LocalDate.now()) }
    com.mabn.calendarlibrary.ExpandableCalendar(onDayClick = {
        currentDate.value = it
    })*/
    /*val currentDate = remember { mutableStateOf(LocalDate.now()) }
    ExpandableCalendar(
         onDayClick = {
            currentDate.value = it
        })
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text("Selected date: ${currentDate.value}")
    }*/
    //Schedule(events = sampleEvents)



    /*val kalendarEvent = KalendarEvent(LocalDate(2022,12,5), "TEST EVENT")
    val eventList = ArrayList<KalendarEvent>()
    eventList.add(kalendarEvent)

    Kalendar(kalendarType = KalendarType.Firey, kalendarEvents = eventList)



    val scheduleState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scheduleState,
    ) {

        /*Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {

            //AndroidView(factory = { CalendarView(it) }, update = { it.setOnDateChangeListener{ calendarView, year, month, day }})
        }*/
    }*/
}