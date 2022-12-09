package com.yara.raco.model.evaluation

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.yara.raco.model.grade.Grade

/**
 * Class that represents a grade.
 *
 * @param subjectId Subject instance.
 * @param evaluation List of evaluation.
 */
@kotlinx.serialization.Serializable
@Entity(tableName = "evaluation")
data class Evaluation @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var subjectId: String,
    var name: String,
    @Ignore
    val listOfGrade: ArrayList<Grade> = arrayListOf()
)