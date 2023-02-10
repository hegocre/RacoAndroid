package com.yara.raco.model.schedule

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import com.yara.raco.ui.components.ScheduleEvent
import kotlinx.serialization.SerialName
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Class that represents a class in the schedule.
 *
 * @param codiAssig Acronym of the subject.
 * @param grup Enrolled group of the subject.
 * @param diaSetmana Number of the week.
 * @param inici Start hour.
 * @param durada How many hours the class lasts.
 * @param tipus Kind of class: T->Teoric L->Laboratory
 * @param aules Classrooms.
 * @param idioma Language indicated in the teaching guide. The teacher will ignore this and speak whatever language he chooses.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "schedule", primaryKeys = ["codiAssig", "diaSetmana", "inici"])
data class Schedule (
    @SerialName("codi_assig")
    val codiAssig: String,
    @SerialName("grup")
    val grup: String,
    @SerialName("dia_setmana")
    val diaSetmana: Int,
    @SerialName("inici")
    val inici: String,
    @SerialName("durada")
    val durada: Int,
    @SerialName("tipus")
    val tipus: String,
    @SerialName("aules")
    val aules: String,
    @SerialName("idioma")
    val idioma: String
) {
    fun toScheduleEvent(
        colorSubject: HashMap<String, Color>,
        firstDay: LocalDateTime
    ): ScheduleEvent {
        val dateWithoutTime =
            firstDay.plusDays((8L - firstDay.dayOfWeek.value) % 7 + diaSetmana - 1L)
        val dateStart = dateWithoutTime.with(
            LocalTime.of(
                inici.split(":")[0].toInt(),
                inici.split(":")[1].toInt()
            )
        )

        return ScheduleEvent(
            name = "GRAU-$codiAssig $grup $tipus",
            color = colorSubject.getOrDefault(codiAssig, Color(0xfff3b0c3)),
            start = dateStart,
            end = dateStart.plusHours(durada.toLong()),
            location = aules
        )
    }
}