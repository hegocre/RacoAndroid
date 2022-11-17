package com.yara.raco.model.grades

import androidx.room.Entity
import androidx.room.Ignore
import com.yara.raco.model.files.File
import com.yara.raco.model.grade.Grade
import kotlinx.serialization.SerialName

/**
 * Class that represents a grade.
 *
 * @param subjectId Subject instance.
 * @param grades List of grades.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "grades", primaryKeys = ["id"])
data class Grades @JvmOverloads constructor(
    @SerialName("id")
    val id: Int,
    @SerialName("subjectId")
    val subjectId: String,
    @SerialName("listOfGrade")
    val listOfGrade: ArrayList<Grade> = arrayListOf()
)