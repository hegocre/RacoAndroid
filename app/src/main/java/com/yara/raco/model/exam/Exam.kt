package com.yara.raco.model.exam

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import com.yara.raco.ui.components.ScheduleEvent
import kotlinx.serialization.SerialName
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

@kotlinx.serialization.Serializable
@Entity(tableName = "exams", primaryKeys = ["id"])
data class Exam(
    val id: Int,
    val assig: String,
    @SerialName("codi_upc")
    val codiUpc: String,
    val aules: String,
    val inici: String,
    val fi: String,
    val quatr: Int,
    val curs: Int,
    val pla: String,
    val tipus: String,
    val comentaris: String,
    val eslaboratori: String
) {
    fun toScheduleEvent(colorSubject: HashMap<String, Color>): ScheduleEvent {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss", Locale.getDefault())

        val dateStart = dateFormat.parse(inici) ?: Date(System.currentTimeMillis())
        val dateEnd = dateFormat.parse(fi) ?: Date(System.currentTimeMillis() + 3600000L)

        return ScheduleEvent(
            name = "[$assig] Examen ${
                when (tipus) {
                    "P" -> "Parcial"; "F" -> "Final"; else -> ""
                }
            }",
            color = colorSubject.getValue(assig),
            start = dateStart.toInstant().atZone(ZoneId.of("Europe/Madrid")).toLocalDateTime(),
            end = dateEnd.toInstant().atZone(ZoneId.of("Europe/Madrid")).toLocalDateTime(),
            description = aules
        )
    }
}