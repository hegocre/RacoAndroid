package com.yara.raco.model.schedule

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import com.yara.raco.ui.components.Event
import kotlinx.serialization.SerialName
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

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
    fun toEvent(colorSubject: HashMap<String, Color>): Event {
        val dateWithoutTime =
            LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.of(diaSetmana)))
        val dateStart = dateWithoutTime.with(
            LocalTime.of(
                inici.split(":")[0].toInt(),
                inici.split(":")[1].toInt()
            )
        )

        return Event(
            name = codiAssig.plus(" ").plus(grup).plus("-").plus(tipus),
            color = colorSubject.getValue(codiAssig),
            start = dateStart,
            end = dateStart.plusHours(durada.toLong()),
            description = aules
        )
    }
}