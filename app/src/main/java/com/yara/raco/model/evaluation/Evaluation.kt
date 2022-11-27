package com.yara.raco.model.evaluation

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.yara.raco.model.grade.Grade
import kotlinx.serialization.SerialName

/**
 * Class that represents a grade.
 *
 * @param subjectId Subject instance.
 * @param evaluation List of evaluation.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "evaluation")
data class Evaluation @JvmOverloads constructor(
    @SerialName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerialName("subjectId")
    val subjectId: String,
    @SerialName("name")
    val name: String,
    @SerialName("listOfGrade")
    @Ignore
    val listOfGrade: ArrayList<Grade> = arrayListOf()
)