package com.yara.raco.model.grades

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Relation
import com.yara.raco.model.subject.Subject
import kotlinx.serialization.SerialName

/**
 * Class that represents a grade.
 *
 * @param subject Subject instance.
 * @param subjectPercentatges List of pairs that contains a name and a percentage.
 * @param subjectNotes List of pairs that contains a name and a note.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "grades",
        primaryKeys = ["id"])
data class Grades @JvmOverloads constructor(
    @SerialName("id")
    val id: String,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = Subject::class
    )
    val subject: Subject,
    @SerialName("grades")
    val grades: List<Grade>
)