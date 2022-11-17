package com.yara.raco.model.evaluation

import androidx.room.Entity
import androidx.room.Ignore
import com.yara.raco.model.grade.Grade
import kotlinx.serialization.SerialName

/**
 * Class that represents a grade.
 *
 * @param subjectId Subject instance.
 * @param evaluation List of evaluation.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "evaluation", primaryKeys = ["id"])
data class Evaluation @JvmOverloads constructor(
    @SerialName("id")
    val id: Int,
    @SerialName("subjectId")
    val subjectId: String,
    @SerialName("listOfGrade")
    @Ignore
    val listOfGrade: ArrayList<Grade> = arrayListOf()
)