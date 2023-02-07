package com.yara.raco.model.event

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import com.yara.raco.ui.components.ScheduleEvent
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

@kotlinx.serialization.Serializable
@Entity(tableName = "events", primaryKeys = ["nom", "inici"])
data class Event(
    val nom: String,
    val inici: String,
    val fi: String,
    val categoria: String
) {
    fun toScheduleEvent(): ScheduleEvent {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss", Locale.getDefault())

        val dateStart = dateFormat.parse(inici) ?: Date(System.currentTimeMillis())
        val dateEnd = dateFormat.parse(fi) ?: Date(System.currentTimeMillis() + 3600000L)

        return ScheduleEvent(
            name = nom,
            color = Color(0xfffbecc6),
            start = dateStart.toInstant().atZone(ZoneId.of("Europe/Madrid")).toLocalDateTime(),
            end = dateEnd.toInstant().atZone(ZoneId.of("Europe/Madrid")).toLocalDateTime(),
        )
    }
}